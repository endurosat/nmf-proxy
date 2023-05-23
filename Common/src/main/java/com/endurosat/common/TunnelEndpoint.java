package com.endurosat.common;

import org.ccsds.moims.mo.testbed.util.spp.SPPSocket;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;
import org.ccsds.moims.mo.testbed.util.sppimpl.tcp.TCPSPPSocketFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class TunnelEndpoint {
    Properties properties;
    private SPPSocket endpoint;
    private TunnelChannel tunnelChannel;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public TunnelEndpoint(Properties properties,TunnelChannel tunnelChannel) {
        this.properties = properties;
        this.tunnelChannel = tunnelChannel;
    }

    public void init() throws Exception {
        tunnelChannel.init(properties);
        System.out.println(properties.getProperty("org.ccsds.moims.mo.malspp.test.sppimpl.tcp.isServer"));
        endpoint = TCPSPPSocketFactory.newInstance().createSocket(properties);
    }

    public void start() {
        OutgoingThread ot = new OutgoingThread();
        IncomingThread it = new IncomingThread();
        Thread incomingThread = new Thread(it);
        Thread outgoingThread = new Thread(ot);
        isRunning.set(true);
        incomingThread.start();
        outgoingThread.start();
    }

    public void close() {
        isRunning.set(false);
        try {
            endpoint.close();
            tunnelChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class OutgoingThread implements Runnable {
        @Override
        public void run() {
            SpacePacket currentPacket;
            while (isRunning.get()) {
                try {
                    System.out.println("Recieving Space Packets");
                    currentPacket = endpoint.receive();
                    System.out.println("packet recieved from endpoint" + currentPacket.getLength() + " " + currentPacket.getHeader().getApid());
                    tunnelChannel.send(currentPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class IncomingThread implements Runnable {
        @Override
        public void run() {
            SpacePacket currentPacket;
            while (isRunning.get()) {
                try {
                    currentPacket = tunnelChannel.receive();
                    endpoint.send(currentPacket);
                    System.out.println("broadcast packet to apps");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
