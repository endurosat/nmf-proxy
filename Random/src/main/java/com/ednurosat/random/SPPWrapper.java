package com.ednurosat.random;

import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacketHeader;

import java.io.Serializable;
import java.util.Map;

public class SPPWrapper implements Serializable {
    private final int packetVersionNumber;
    private final int packetType;
    private final int secondaryHeaderFlag;
    private final int apid;
    private final int sequenceFlags;
    private final int sequenceCount;
    private final int apidQualifier;
    private final byte[] body;
    private final int offset;
    private final int length;
    private final Map qosProperties;

    public SPPWrapper(SpacePacket packet) {
        SpacePacketHeader header = packet.getHeader();
        packetVersionNumber = header.getPacketVersionNumber();
        packetType = header.getPacketType();
        secondaryHeaderFlag = header.getSecondaryHeaderFlag();
        apid = header.getApid();
        sequenceFlags = header.getSequenceFlags();
        sequenceCount = header.getSequenceCount();
        apidQualifier = packet.getApidQualifier();
        body = packet.getBody();
        offset = packet.getOffset();
        length = packet.getLength();
        qosProperties = packet.getQosProperties();
    }



    public SpacePacket readPacket() {
        SpacePacket packet = new SpacePacket(new SpacePacketHeader(packetVersionNumber, packetType, secondaryHeaderFlag, apid, sequenceFlags, sequenceCount), apidQualifier, body, offset, length);
        packet.setQosProperties(qosProperties);
        return packet;
    }


}
