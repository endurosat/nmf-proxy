package com.ednurosat.random;

import org.ccsds.moims.mo.testbed.util.spp.SPPSocket;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacketHeader;

import java.util.Map;

public class SimulatedTCPSPPSocket implements SPPSocket {
    public void init(Map map) {
    }

    public void close() {
    }

    @Override
    public String getDescription() {
        return ("This is a simulated socket, it is not connected to anything");
    }

    public SpacePacket receive() throws InterruptedException {
        int x = 0;
        byte[] byts = {2, 2, 2, 2, 2, 2};
        SpacePacket packet = new SpacePacket(new SpacePacketHeader(x, x + 1, x + 3, 1026, x + 5, x + 6), byts, 0, 6);
        Thread.sleep(10000);
        System.out.println("Simulating receive");
        return packet;
    }

    public void send(SpacePacket currentPacket) {
        System.out.println("broadcasting packet" + currentPacket.getHeader().toString());
    }
}
