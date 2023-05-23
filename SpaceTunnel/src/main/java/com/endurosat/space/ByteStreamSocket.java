package com.endurosat.space;

import com.endurosat.common.SPPConverter;
import com.endurosat.common.SpacePacketQueue;
import com.endurosat.common.TunnelChannel;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ByteStreamSocket extends TunnelChannel {

    ServerSocket inputSocket;
    ServerSocket outputSocket;
    SpacePacketQueue inputQueue;
    SpacePacketQueue outputQueue;
    InputThread inputThread;
    OutputThread outputThread;
    AtomicBoolean running = new AtomicBoolean(true);
    int MAXBYTES;
    @Override
    public void init(Map properties) throws Exception {
        inputQueue = new SpacePacketQueue(properties);
        outputQueue= new SpacePacketQueue(properties);
        int inputPort = Integer.parseInt((String) properties.get("ESPS1.inputPort"));
        int outputPort = Integer.parseInt((String) properties.get("ESPS1.outputPort"));
        this.MAXBYTES = Integer.parseInt((String) properties.get("ESPS1.maxPayloadSize"));
        inputSocket = new ServerSocket(inputPort);
        outputSocket = new ServerSocket(outputPort);
        inputThread = new InputThread();
        outputThread = new OutputThread();
        inputThread.start();
        outputThread.start();
        System.out.println("ready for command");
    }

    @Override
    public void send(SpacePacket Message) throws Exception {
        outputQueue.put(Message);
    }

    @Override
    public SpacePacket receive() throws Exception {
        return inputQueue.take();
    }

    @Override
    public void close() throws Exception {
        inputSocket.close();
        outputSocket.close();
        running.set(false);
        inputThread.interrupt();
        outputThread.interrupt();
    }

    private class InputThread extends Thread{
        public InputThread(){
            super(() -> {
                while(running.get()) {
                    try {
                        System.out.println("ready for upload");
                        Socket socket = inputSocket.accept();
                        System.out.println("received upload command");
                        LinkedList<Byte> list = new LinkedList<>();
                        InputStream inputStream = socket.getInputStream();
                        int b = inputStream.read();
                        while (b!=-1) {
                            byte byt = (byte) b;
                            list.add(byt);
                            b = inputStream.read();
                            System.out.print(byt + " ");
                        }
                        System.out.println();
                        System.out.println("finished reading");
                        socket.close();
                        list = unsign(list);
                        inputQueue.addAll(SPPConverter.massDeconvert(list));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private class OutputThread extends Thread{
        public OutputThread(){
            super(() -> {
                while(running.get()){
                    try {
                        System.out.println("ready for download");
                        Socket socket = outputSocket.accept();
                        System.out.println("recieved download command");
                        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                        LinkedList<Byte> list = SPPConverter.massConvertByteList(outputQueue,MAXBYTES);
                        byte[] allBytes = new byte[list.size()];
                        int allBytePointer = 0;
                        if(!list.isEmpty()){
                            while(!list.isEmpty()){
                                allBytes[allBytePointer] = list.pop();
                                allBytePointer++;
                            }
                            System.out.println(allBytes.length);
                            outputStream.write(allBytes);
                            outputStream.flush();

                        }


                    } catch (InterruptedException e){
                        e.printStackTrace();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static LinkedList<Byte> unsign(LinkedList<Byte> l1){
        LinkedList<Byte> l2 = new LinkedList<>();
        while(!l1.isEmpty()){
            l2.add((byte) (l1.pop()&0xFF));
        }
        return l2;
    }

    public static void printArray(byte[] a){
        for (byte b : a) {
            System.out.print(b + " ");
        }
        System.out.println();

    }



}
