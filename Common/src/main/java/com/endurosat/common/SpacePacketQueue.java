package com.endurosat.common;

import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class SpacePacketQueue {
    private BlockingQueue<PacketAndTimestamp> buffer;
    private long timeout;

    public SpacePacketQueue(Map properties){
        this.timeout = Integer.parseInt((String) properties.get("Buffer.PacketTimeout"));
        int bufferLength = Integer.parseInt((String) properties.get("Buffer.BufferLength"));
        buffer = new ArrayBlockingQueue<PacketAndTimestamp>(bufferLength);
    }
    public SpacePacket poll(){
        PacketAndTimestamp queueReturn = buffer.poll();
        if(queueReturn==null){
            return null;
        }
        else{
            if(checkTimestampExpired(queueReturn.getTimestamp())){
                return poll();
            }
            else{
                return queueReturn.getPacket();
            }
        }
    }

    public SpacePacket take() throws InterruptedException {
        PacketAndTimestamp queueReturn = buffer.take();
        if(checkTimestampExpired(queueReturn.getTimestamp())){
            return take();
        }
        else{
            return queueReturn.getPacket();
        }

    }

    public void put(SpacePacket packet) throws InterruptedException {
        buffer.put(new PacketAndTimestamp(packet,System.currentTimeMillis()));
    }

    public SpacePacket peek(){
        PacketAndTimestamp queueReturn = buffer.peek();
        if(queueReturn==null){
            return null;
        }
        else{
            if(checkTimestampExpired(queueReturn.getTimestamp())){
                buffer.poll();
                return peek();
            }
            else{
                return queueReturn.getPacket();
            }
        }
    }

    private boolean checkTimestampExpired(long time){
        return (System.currentTimeMillis() - time)>timeout;
    }

    public void addAll(List<SpacePacket> list) {
        buffer.addAll(list.stream().map(x -> new PacketAndTimestamp(x,System.currentTimeMillis())).collect(Collectors.toList()));
    }


    public class PacketAndTimestamp{
        SpacePacket packet;
        long timestamp;
        public PacketAndTimestamp(SpacePacket packet,long timestamp){
            this.packet = packet;
            this.timestamp = timestamp;
        }

        public SpacePacket getPacket() {
            return packet;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }




}
