package com.endurosat.space;

import com.endurosat.common.SPPConverter;
import org.ccsds.moims.mo.testbed.util.spp.SPPSocket;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;
import org.ccsds.moims.mo.testbed.util.sppimpl.tcp.TCPSPPSocketFactory;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulatorTunnel {
    SPPSocket ground;
    SPPSocket space;

    public SimulatorTunnel(SPPSocket groundFacingSocket, SPPSocket spaceFacingSocket) {
        this.ground = groundFacingSocket;
        this.space = spaceFacingSocket;
    }

    public static void main(String[] args) throws Exception {
        Properties groundSocketProps = new Properties();
        Properties spaceSocketProps = new Properties();
        groundSocketProps.setProperty("org.ccsds.moims.mo.malspp.test.sppimpl.tcp.isServer", "true");
        groundSocketProps.setProperty("org.ccsds.moims.mo.malspp.test.sppimpl.tcp.port", "25252");
        spaceSocketProps.setProperty("org.ccsds.moims.mo.malspp.test.sppimpl.tcp.isServer", "true");
        spaceSocketProps.setProperty("org.ccsds.moims.mo.malspp.test.sppimpl.tcp.port", "20560");
        SPPSocket groundSocket = TCPSPPSocketFactory.newInstance().createSocket(groundSocketProps);
        SPPSocket spaceSocket = TCPSPPSocketFactory.newInstance().createSocket(spaceSocketProps);
        SimulatorTunnel simpleVirtualTunnel = new SimulatorTunnel(groundSocket, spaceSocket);
        simpleVirtualTunnel.start();
    }

    public static void printarray(byte[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + " ");
        }
        System.out.println();
    }

    public void start() {
        AtomicInteger latency = new AtomicInteger(0);
        AtomicInteger loss = new AtomicInteger(0);
        AtomicBoolean connection = new AtomicBoolean(true);
        AtomicBoolean alphaBeta = new AtomicBoolean(false);
        AtomicBoolean discrete = new AtomicBoolean(false);
        AtomicInteger bandwidth = new AtomicInteger(2000);
        Thread th1 = new Thread(new TunnelThread(ground, space, latency, loss, connection, discrete, alphaBeta, true, bandwidth));
        Thread th2 = new Thread(new TunnelThread(space, ground, latency, loss, connection, discrete, alphaBeta, false, bandwidth));
        th1.start();
        th2.start();
        ControlPanel panel = new ControlPanel(latency, loss, connection, discrete, bandwidth);
    }

    public class TunnelThread implements Runnable {
        SPPSocket incoming;
        ArrayBlockingQueue<SpacePacket> queue = new ArrayBlockingQueue<>(400);

        public TunnelThread(SPPSocket incoming, SPPSocket outgoing, AtomicInteger latency, AtomicInteger loss, AtomicBoolean connection, AtomicBoolean discrete, AtomicBoolean alphaBeta, boolean alpha, AtomicInteger bandwidth) {
            this.incoming = incoming;
            SendingThread sender = new SendingThread(outgoing, latency, loss, connection, discrete, alphaBeta, alpha, queue, bandwidth);
            new Thread(sender).start();
        }

        public void run() {
            SpacePacket currentPacket;
            while (true) {
                try {
                    currentPacket = incoming.receive();
                    System.out.println("recieved packet from " + incoming.getDescription() + "routing to " + currentPacket.getHeader().getApid() + "," + "adding to buffer");
                    boolean accepted = queue.offer(currentPacket);
                    if (!accepted) {
                        System.out.println("BUFFER FULL ON SOCKET " + incoming.getDescription());
                    }
                } catch (Exception e) {
                    //this can be ignored sort of
                    System.out.println(e);
                }
            }
        }


        public class packetSim {
            SPPSocket outgoing;
            AtomicInteger latency;
            AtomicInteger loss;

            public packetSim(SPPSocket outgoing, AtomicInteger latency, AtomicInteger loss) {
                this.outgoing = outgoing;
                this.latency = latency;
                this.loss = loss;
            }

            public void sendPacket(SpacePacket packet) {
                if (loss.get() > 0) {
                    loss.set(loss.get() - 1);
                } else {
                    simThread simThread = new simThread(latency.get(), outgoing, packet);
                    simThread.start();
                }
            }

            public class simThread extends Thread {
                public simThread(int latency, SPPSocket outgoing, SpacePacket packet) {
                    super(() -> {
                        try {
                            sleep(latency);
                            outgoing.send(packet);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

            }
        }


        public class SendingThread implements Runnable {
            AtomicBoolean connection;
            AtomicBoolean discrete;
            AtomicBoolean alphaBeta;
            ArrayBlockingQueue<SpacePacket> queue;
            Boolean alpha;
            packetSim sim;
            AtomicInteger bandwidth;

            public SendingThread(SPPSocket outgoing, AtomicInteger latency, AtomicInteger loss, AtomicBoolean connection, AtomicBoolean discrete, AtomicBoolean alphaBeta, Boolean alpha, ArrayBlockingQueue<SpacePacket> queue, AtomicInteger bandwidth) {
                this.connection = connection;
                this.discrete = discrete;
                this.queue = queue;
                this.alphaBeta = alphaBeta;
                this.alpha = alpha;
                this.bandwidth = bandwidth;
                sim = new packetSim(outgoing, latency, loss);
            }

            @Override
            public void run() {
                while (true) {
                    if (connection.get() && !(discrete.get() && (alphaBeta.get() ^ alpha))) {
                        try {
                            SpacePacket currentPacket = queue.poll();

                            if (currentPacket == null) {
                                alphaBeta.set(!alphaBeta.get());
                                if (discrete.get()) {
                                    if (alpha) {
                                        System.out.println("Switching up");
                                    } else {
                                        System.out.println("Switching down");
                                    }
                                }
                                Thread.sleep(100);

                            }
                            if (currentPacket != null) {
                                System.out.println("took from buffer");
                                byte[] bytes = SPPConverter.convertSPP(currentPacket);
                                System.out.println("packet length = " + bytes.length);
                                //System.out.println("compressed length = "+ Compressor.compress(bytes).length);
                                printarray(bytes);
                                bytes = SPPConverter.convertByte(bytes).getLeftover();


                                Thread.sleep((1000 * bytes.length) / (bandwidth.get()));
                                sim.sendPacket(currentPacket);
                            }

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}

