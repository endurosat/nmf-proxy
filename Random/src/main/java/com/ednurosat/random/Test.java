package com.ednurosat.random;

import com.endurosat.common.SPPConverter;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacketHeader;

import java.util.*;

public class Test {
    public static void main(String[] args) throws Exception {





        ArrayList<Byte> bytlist = new ArrayList<>();
        SPPConverter converter = new SPPConverter();
        byte[] a = {1,2,3,4,5,6,7,8,9,9};
        byte[] b = {1,2,3,4,5,6,7,8,9,9,4,6,7,8};
        byte[] c = {1,2,3,4,5,6,7,8,9,9,4,6,7,8,4,5,1};

        SpacePacketHeader headA = new SpacePacketHeader();
        headA.setApid(134);
        headA.setSequenceCount(0);
        headA.setPacketType(1);
        headA.setPacketVersionNumber(1);
        headA.setSecondaryHeaderFlag(0);
        SpacePacketHeader headB = new SpacePacketHeader();
        headB.setApid(135);
        headB.setSequenceCount(1);
        headB.setPacketType(1);
        headB.setPacketVersionNumber(1);
        headB.setSecondaryHeaderFlag(0);
        SpacePacketHeader headC = new SpacePacketHeader();
        headC.setApid(136);
        headC.setSequenceCount(0);
        headC.setPacketType(1);
        headC.setPacketVersionNumber(1);
        headC.setSecondaryHeaderFlag(0);

        SpacePacket packetA = new SpacePacket(headA,a,0,a.length);
        SpacePacket packetB = new SpacePacket(headB,b,0,b.length);
        SpacePacket packetC = new SpacePacket(headC,c,0,c.length);
        byte[] bytes = SPPConverter.convertSPP(packetA);
        LinkedList<Byte> list1 = new LinkedList<Byte>();
        for(int i = 0;i<bytes.length;i++){
            list1.add(bytes[i]);
        }
        System.out.println(SPPConverter.convertByte(list1).getPacket().getHeader().getApid());
        SpacePacket[] packets = {packetA,packetB,packetC,null,null};

        byte[] pack = SPPConverter.massConvert(packets);

        byte[] pack1 = new byte[pack.length+10];
        for(int i = 0;i<pack.length;i++){
            pack1[i]=pack[i];
        }
        //for(int i = pack.length;i<pack1.length;i++){
          //  pack1[i] = (byte) new Random().nextInt(100);
        //}

        LinkedList<SpacePacket> list = SPPConverter.massDeconvert(pack1);
        System.out.println(list.size());

        while(!list.isEmpty()){
            System.out.println(list.poll().getHeader().getApid());
        }
    }
}
