package com.endurosat.ground;

import com.endurosat.common.SPPConverter;
import com.endurosat.common.SpacePacketQueue;
import com.endurosat.common.TunnelChannel;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class GSEmulatorTunnel extends TunnelChannel {
    SpacePacketQueue upQueue;
    SpacePacketQueue downQueue;
    String INPUTFILEPATH = "";
    String OUTPUTFILEPATH = "";
    String GSEMULATORPATH = "";
    String UPLOADCOMMAND = "";
    String DOWNLOADCOMMAND = "";
    File inputFile;
    File outputFile;
    Thread commandthread;
    int MAXBYTES;
    int RETRIES;
    AtomicBoolean running = new AtomicBoolean(true);

    private static int RunAndLogProcess(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        String line;
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));


        process.waitFor();

        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = error.readLine()) != null) {
            System.out.println(line);
        }
        error.close();

        while ((line = input.readLine()) != null) {
            System.out.println(line);
        }

        input.close();
        return process.exitValue();

    }

    public static void printArray(byte[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + " ");
        }
        System.out.println();

    }

    private static byte[] generateHeader(LinkedList<byte[]> messages) {
        int count = count(messages);
        byte a = (byte) ((count / 255) & 0xFF);
        byte b = (byte) ((count % 255) & 0xFF);
        System.out.println("HEADER =======================================");
        System.out.println(b + ":" + a);
        return new byte[]{b, a};
    }

    private static int count(LinkedList<byte[]> messages) {
        int length = 0;
        for (int i = 0; i < messages.size(); i++) {
            length = length + messages.get(i).length;
        }
        return length;
    }

    public static byte[] unsign(byte[] b) {
        byte[] a = new byte[b.length];
        for (int i = 0; i < b.length; i++) {
            a[i] = (byte) (b[i] & 0xFF);
        }
        return a;
    }

    @Override
    public void init(Map properties) throws Exception {
        this.commandthread = new commandThread();
        this.INPUTFILEPATH = (String) properties.get("GSEmulator.inputFilePath");
        this.OUTPUTFILEPATH = (String) properties.get("GSEmulator.outputFilePath");
        this.GSEMULATORPATH = (String) properties.get("GSEmulator.GSEmulatorExePath");
        this.UPLOADCOMMAND = (String) properties.get("GSEmulator.uploadCommand");
        this.DOWNLOADCOMMAND = (String) properties.get("GSEmulator.downloadCommand");
        this.MAXBYTES = Integer.parseInt((String) properties.get("ESPS1.maxPayloadSize"));
        this.RETRIES = Integer.parseInt((String) properties.get("GSEmulator.noOfRetries"));
        this.inputFile = new File(INPUTFILEPATH);
        this.outputFile = new File(OUTPUTFILEPATH);
        upQueue = new SpacePacketQueue(properties);
        downQueue = new SpacePacketQueue(properties);
        if (!inputFile.exists()) {
            inputFile.createNewFile();
        }
        this.commandthread.start();
    }

    @Override
    public void send(SpacePacket Message) throws Exception {
        upQueue.put(Message);
    }

    @Override
    public SpacePacket receive() throws Exception {
        return downQueue.take();
    }

    @Override
    public void close() throws Exception {
        running.set(false);
        commandthread.interrupt();
    }

    public class commandThread extends Thread {
        public commandThread() {
            super(() -> {
                LinkedList<byte[]> massConversion = null;
                int Failures = 0;
                while (running.get()) {
                    System.out.println("running");
                    outputFile.delete();
                    try {
                        outputFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try (FileOutputStream outputStream = new FileOutputStream(inputFile)) {
                        System.out.println("starting output");
                        if (Failures == 0) {
                            System.out.println("looking for new packets");
                            massConversion = SPPConverter.massConvert(upQueue, MAXBYTES);

                        } else {
                            Failures = Failures - 1;
                        }

                        if (!massConversion.isEmpty()) {
                            System.out.println("writing packets");
                            outputStream.write(generateHeader(massConversion));
                            while (!massConversion.isEmpty()) {
                                byte[] b = massConversion.pop();
                                outputStream.write(b);

                            }
                            outputStream.flush();
                            outputStream.close();
                            printArray(Files.readAllBytes(inputFile.toPath()));
                            int exitValue = RunAndLogProcess(UPLOADCOMMAND);

                            if (exitValue == -1) {
                                System.out.println("GSEmulator error code " + exitValue);
                                if (Failures == 0) {
                                    Failures = RETRIES;
                                }
                                System.out.println("Retrying for " + Failures + " times");


                            }
                        }

                        System.out.println("starting download");
                        RunAndLogProcess(DOWNLOADCOMMAND);
                        System.out.println("finished download");


                        byte[] fileContent = Files.readAllBytes(new File(OUTPUTFILEPATH).toPath());
                        fileContent = Arrays.copyOfRange(fileContent, 2, fileContent.length);
                        fileContent = unsign(fileContent);
                        System.out.println("read " + fileContent.length + " bytes");
                        LinkedList<SpacePacket> arrivedPackets = SPPConverter.massDeconvert(fileContent);
                        while (!arrivedPackets.isEmpty()) {
                            downQueue.put(arrivedPackets.pop());
                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
