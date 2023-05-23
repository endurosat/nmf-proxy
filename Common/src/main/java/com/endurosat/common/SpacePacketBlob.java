package com.endurosat.common;

import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;

import java.util.LinkedList;

public class SpacePacketBlob {
    int maxbytes;
    int size = 0;
    LinkedList<byte[]> packets = new LinkedList<>();
    public SpacePacketBlob(int maxbytes){
        this.maxbytes = maxbytes;
    }
    public boolean add(SpacePacket p){
        byte [] packet = SPPConverter.convertSPP(p);
        if(size+packet.length>maxbytes){
            return false;
        }
        else{
            size = size+packet.length;
            packets.add(packet);
            return true;
        }
    }
    public LinkedList<byte[]> get(){
        return packets;
    }
}
