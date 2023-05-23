package com.ednurosat.random;

import com.endurosat.common.SPPConverter;
import org.ccsds.moims.mo.testbed.util.spp.SPPSocket;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;
import org.ccsds.moims.mo.testbed.util.sppimpl.tcp.TCPSPPSocketFactory;

import java.util.Properties;

public class SimpleVirtualTunnel {
    SPPSocket ground;
    SPPSocket space;

    public SimpleVirtualTunnel(SPPSocket groundFacingSocket, SPPSocket spaceFacingSocket) {
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
        SimpleVirtualTunnel simpleVirtualTunnel = new SimpleVirtualTunnel(groundSocket, spaceSocket);
        simpleVirtualTunnel.start();
    }

    public void start() {
        Thread th1 = new Thread(new TunnelThread(ground, space));
        Thread th2 = new Thread(new TunnelThread(space, ground));
        th1.start();
        th2.start();
    }

    public class TunnelThread implements Runnable {
        SPPSocket incoming;
        SPPSocket outgoing;

        public TunnelThread(SPPSocket incoming, SPPSocket outgoing) {
            this.incoming = incoming;
            this.outgoing = outgoing;
        }

        public void run() {
            SpacePacket currentPacket;
            while (true) {
                try {
                    currentPacket = incoming.receive();
                    System.out.println("recieved packet from "+incoming.getDescription()+ "routing to "+ currentPacket.getHeader().getApid()+",");
                    byte[] bytes = SPPConverter.convertSPP(currentPacket);
                    SpacePacket newPacket = SPPConverter.convertByte(bytes).getPacket();
                    outgoing.send(newPacket);
                    System.out.println("sent packet to"+outgoing.getDescription());
                } catch (Exception e) {
                    //this can be ignored sort of
                    e.printStackTrace();
                }
            }
        }
    }

}
