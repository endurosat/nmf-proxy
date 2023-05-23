package com.endurosat.common;

import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacketHeader;
import org.ccsds.moims.mo.testbed.util.sppimpl.util.SPPHelper;

import java.util.LinkedList;


public class SPPConverter {
    final protected static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static byte[] massConvert(SpacePacket[] packets){
        byte[] bytes = SPPConverter.convertSPP(packets[0]);
        for(int i=1;i< packets.length&&packets[i]!=null;i++){
            bytes = combine(bytes,SPPConverter.convertSPP(packets[i]));
        }
        return bytes;
    }

    public static LinkedList<byte[]> massConvertBlock(SpacePacketQueue packets, int maxBytes) throws Exception {
        LinkedList<byte[]> bytes = new LinkedList<>();
        bytes.add(SPPConverter.convertSPP(packets.take()));
        bytes.addAll(massConvert(packets,(maxBytes-bytes.peek().length)));
        return bytes;
    }

    public static LinkedList<byte[]> massConvert(SpacePacketQueue packets, int maxBytes) throws Exception {
        int totalB = 0;
        LinkedList<byte[]> bytes = new LinkedList<>();
        while(packets.peek()!=null){
            byte[] packet = SPPConverter.convertSPP(packets.peek());
            if(totalB+packet.length > maxBytes){
                break;
            }
            else{
                bytes.add(packet);
                totalB = totalB+packet.length;
                packets.poll();
            }
        }
        return bytes;
    }

    public static LinkedList<Byte> massConvertByteList(SpacePacketQueue packets, int maxBytes) throws Exception {
        int totalB = 0;
        LinkedList<Byte> bytes = new LinkedList<>();
        while(packets.peek()!=null){
            byte[] packet = SPPConverter.convertSPP(packets.peek());
            if(totalB+packet.length > maxBytes){
                break;
            }
            else{
                for(int i = 0;i<packet.length;i++){
                    bytes.add(packet[i]);
                }
                totalB = totalB+packet.length;
                packets.poll();
            }
        }
        return bytes;
    }



    public static LinkedList<SpacePacket> massDeconvert(byte[] bytes) throws Exception {
        LinkedList<SpacePacket> list = new LinkedList<>();
        PacketAndLeftover conversion;
        try {
            while (bytes.length != 0) {
                conversion = SPPConverter.convertByte(bytes);
                list.add(conversion.getPacket());
                bytes = conversion.leftover;
            }
        }
        catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            //list.pollLast();
        }
        return list;
    }

    public static LinkedList<SpacePacket> massDeconvert(LinkedList<Byte> bytes) throws  Exception{
        LinkedList<SpacePacket> list = new LinkedList<>();
        PacketAndLeftover conversion;
        try {
            while (!bytes.isEmpty()) {
                conversion = SPPConverter.convertByte(bytes);
                list.add(conversion.getPacket());
            }
        }
        catch (IndexOutOfBoundsException e){
            System.out.println(e);
            //list.pollLast();
        }
        return list;
    }

    public static byte[] convertSPP(SpacePacket packet){
        byte[] output;

        byte[] apidQualifierBuffer = new byte[2];
        byte[] outHeaderBuffer = new byte[6];
        byte[] outCrcBuffer = new byte[2];

        final SpacePacketHeader sph = packet.getHeader();
        final byte[] data = packet.getBody();
        final int vers_nb = sph.getPacketVersionNumber();
        final int pkt_type = sph.getPacketType();
        final int sec_head_flag = sph.getSecondaryHeaderFlag();
        final int TCPacket_apid = sph.getApid();
        final int segt_flag = sph.getSequenceFlags();
        final int pkt_ident = (vers_nb << 13) | (pkt_type << 12) | (sec_head_flag << 11) | (TCPacket_apid);

        final int pkt_seq_ctrl = (segt_flag << 14)
                | (packet.getHeader().getSequenceCount());

        final boolean processCrc = false;
        //crcEnabled && crcApids.inRange(TCPacket_apid);
        final int pkt_length_value = (processCrc) ? packet.getLength() - 1 + 2
                : packet.getLength() - 1;
        outHeaderBuffer[0] = (byte) (pkt_ident >> 8);
        outHeaderBuffer[1] = (byte) (pkt_ident & 0xFF);
        outHeaderBuffer[2] = (byte) (pkt_seq_ctrl >> 8);
        outHeaderBuffer[3] = (byte) (pkt_seq_ctrl & 0xFF);
        outHeaderBuffer[4] = (byte) (pkt_length_value >> 8);
        outHeaderBuffer[5] = (byte) (pkt_length_value & 0xFF);

        output = outHeaderBuffer;
        output=combine(output,data);
        return output;
    }

    public static PacketAndLeftover convertByte(LinkedList<Byte> packet) throws Exception{
        if(SPPHelper.isAPIDqualifierInMessage){
            byte[] apidQualifierBuffer = new byte[2];
            readIntoArray(packet,apidQualifierBuffer);
        }

        int apidQualifier = SPPHelper.defaultAPIDqualifier;
        final SpacePacketHeader header = new SpacePacketHeader();
        final SpacePacket outPacket = new SpacePacket(header, null, 0, 0);

        outPacket.setApidQualifier(apidQualifier);
        byte[] apidQualifierBuffer = new byte[2];
        byte[] inHeaderBuffer = new byte[6];
        byte[] inCrcBuffer = new byte[2];
        readIntoArray(packet,inHeaderBuffer);
        int pk_ident = inHeaderBuffer[0] & 0xFF;
        pk_ident = (pk_ident << 8) | (inHeaderBuffer[1] & 0xFF);
        final int vers_nb = (pk_ident >> 13) & 0x0007;
        final int pkt_type = (pk_ident >> 12) & 0x0001;
        final int sec_head_flag = (pk_ident >> 11) & 0x0001;
        final int apid = pk_ident & 0x07FF;

        int pkt_seq_ctrl = inHeaderBuffer[2] & 0xFF;
        pkt_seq_ctrl = (pkt_seq_ctrl << 8) | (inHeaderBuffer[3] & 0xFF);
        final int segt_flag = (pkt_seq_ctrl >> 14) & 0x0003;
        final int seq_count = pkt_seq_ctrl & 0x3FFF;

        int pkt_length_value = inHeaderBuffer[4] & 0xFF;
        pkt_length_value = ((pkt_length_value << 8) | (inHeaderBuffer[5] & 0xFF)) + 1;

        final SpacePacketHeader sph = outPacket.getHeader();
        sph.setApid(apid);
        sph.setSecondaryHeaderFlag(sec_head_flag);
        sph.setPacketType(pkt_type);
        sph.setPacketVersionNumber(vers_nb);
        sph.setSequenceCount(seq_count);
        sph.setSequenceFlags(segt_flag);

        final int dataLength = pkt_length_value;

        outPacket.setLength(dataLength);
        final byte[] data = new byte[dataLength];
        readIntoArray(packet, data);



        outPacket.setBody(data);
        return new PacketAndLeftover(outPacket,packet);
    }



    public static PacketAndLeftover convertByte(byte[] packet) throws Exception{
        if(SPPHelper.isAPIDqualifierInMessage){
            byte[] apidQualifierBuffer = new byte[2];
            packet = readIntoArray(packet,apidQualifierBuffer);
        }

        int apidQualifier = SPPHelper.defaultAPIDqualifier;
        final SpacePacketHeader header = new SpacePacketHeader();
        final SpacePacket outPacket = new SpacePacket(header, null, 0, 0);

        outPacket.setApidQualifier(apidQualifier);
        byte[] apidQualifierBuffer = new byte[2];
        byte[] inHeaderBuffer = new byte[6];
        byte[] inCrcBuffer = new byte[2];
        packet = readIntoArray(packet,inHeaderBuffer);
        int pk_ident = inHeaderBuffer[0] & 0xFF;
        pk_ident = (pk_ident << 8) | (inHeaderBuffer[1] & 0xFF);
        final int vers_nb = (pk_ident >> 13) & 0x0007;
        final int pkt_type = (pk_ident >> 12) & 0x0001;
        final int sec_head_flag = (pk_ident >> 11) & 0x0001;
        final int apid = pk_ident & 0x07FF;

        int pkt_seq_ctrl = inHeaderBuffer[2] & 0xFF;
        pkt_seq_ctrl = (pkt_seq_ctrl << 8) | (inHeaderBuffer[3] & 0xFF);
        final int segt_flag = (pkt_seq_ctrl >> 14) & 0x0003;
        final int seq_count = pkt_seq_ctrl & 0x3FFF;

        int pkt_length_value = inHeaderBuffer[4] & 0xFF;
        pkt_length_value = ((pkt_length_value << 8) | (inHeaderBuffer[5] & 0xFF)) + 1;

        final SpacePacketHeader sph = outPacket.getHeader();
        sph.setApid(apid);
        sph.setSecondaryHeaderFlag(sec_head_flag);
        sph.setPacketType(pkt_type);
        sph.setPacketVersionNumber(vers_nb);
        sph.setSequenceCount(seq_count);
        sph.setSequenceFlags(segt_flag);

        final int dataLength = pkt_length_value;

        outPacket.setLength(dataLength);
        final byte[] data = new byte[dataLength];
        packet = readIntoArray(packet, data);



        outPacket.setBody(data);
        return new PacketAndLeftover(outPacket,packet);
    }



    private static String bytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] readIntoArray(byte[] array,byte[] emptyArray) throws Exception{
        if(emptyArray.length>array.length){
            throw new IndexOutOfBoundsException();
        }
        byte[] backArray = new byte[array.length- emptyArray.length];
        for(int i = 0;i< array.length;i++){
            if(i<emptyArray.length){
                emptyArray[i] = array[i];
            }
            else{
                backArray[i-emptyArray.length] = array[i];
            }
        }
        return backArray;
    }

    private static void readIntoArray(LinkedList<Byte> list,byte[] emptyArray) throws Exception{
        if(list.size()<emptyArray.length){
            throw new IndexOutOfBoundsException();
        }
        for(int i = 0;i<emptyArray.length;i++){
            emptyArray[i]=list.pop();
        }
    }

    private static byte[] combine(byte[] array1, byte[] array2){
        byte[] output = new byte [array1.length+array2.length];
        System.arraycopy(array1, 0, output, 0, array1.length);
        System.arraycopy(array2, 0, output, 0 + array1.length, array2.length);
        return output;
    }
    public static byte[] generateErrorMessage(SpacePacket packet){
        return null;
    }
    public static boolean doCRC(byte[] packet) throws Exception{
        if(SPPHelper.isAPIDqualifierInMessage){
            byte[] apidQualifierBuffer = new byte[2];
            packet = readIntoArray(packet,apidQualifierBuffer);
        }

        byte[] inHeaderBuffer = new byte[6];
        packet = readIntoArray(packet,inHeaderBuffer);
        int pkt_length_value = inHeaderBuffer[4] & 0xFF;
        pkt_length_value = ((pkt_length_value << 8) | (inHeaderBuffer[5] & 0xFF)) + 1;
        pkt_length_value = pkt_length_value-2;

        byte[] data = new byte[pkt_length_value];
        packet = readIntoArray(packet,data);

        byte[] inCRCbuffer = new byte[2];
        packet = readIntoArray(packet,inCRCbuffer);
        int readCRC = inCRCbuffer[0] & 0xFF;
        readCRC = (readCRC << 8) | (inCRCbuffer[1] & 0xFF);
        if(packet.length != 0){
            //if the array is either too large or too small the same exception is thrown
            throw new IndexOutOfBoundsException();
        }
        return readCRC == SPPHelper.computeCRC(inHeaderBuffer,data,0,pkt_length_value);
    }


    public static class PacketAndLeftover{
        SpacePacket packet;
        byte[] leftover;
        LinkedList<Byte> leftoverList;
        public PacketAndLeftover(SpacePacket packet, byte[] leftover){
            this.packet = packet;
            this.leftover = leftover;
        }

        public PacketAndLeftover(SpacePacket packet, LinkedList<Byte> leftoverList){
            this.packet = packet;
            this.leftoverList = leftoverList;
        }

        public SpacePacket getPacket() {
            return packet;
        }

        public byte[] getLeftover() {
            return leftover;
        }

        public LinkedList<Byte> getList(){
            return leftoverList;
        }
    }




//    protected SpacePacket createErrorMessage(final SPPMessageHeader header, final MALStandardError error) throws MALException {
//        final int type = header.getInteractionType().getOrdinal();
//        final short stage = header.getInteractionStage().getValue();
//
//        // Find out if current interaction allows returning an error message.
//        final boolean isErrorAllowed = ((type == InteractionType._SUBMIT_INDEX && stage == MALSubmitOperation._SUBMIT_STAGE)
//                || (type == InteractionType._REQUEST_INDEX && stage == MALRequestOperation._REQUEST_STAGE)
//                || (type == InteractionType._INVOKE_INDEX && stage == MALInvokeOperation._INVOKE_STAGE)
//                || (type == InteractionType._PROGRESS_INDEX && stage == MALProgressOperation._PROGRESS_STAGE)
//                || (type == InteractionType._PUBSUB_INDEX && stage == MALPubSubOperation._REGISTER_STAGE)
//                || (type == InteractionType._PUBSUB_INDEX && stage == MALPubSubOperation._PUBLISH_REGISTER_STAGE)
//                || (type == InteractionType._PUBSUB_INDEX && stage == MALPubSubOperation._PUBLISH_DEREGISTER_STAGE)
//                || (type == InteractionType._PUBSUB_INDEX && stage == MALPubSubOperation._DEREGISTER_STAGE));
//        if (!isErrorAllowed) {
//            return null;
//        }
//
//        final MALMessage errMsg = createMessage(header.getAuthenticationId(), header.getURIFrom(), // Reply to message sender.
//                new Time(System.currentTimeMillis()), // PENDING: Epoch for Time in MAL Java API unclear. Here: Use Java
//                // epoch.
//                header.getQoSlevel(), header.getPriority(), header.getDomain(), header.getNetworkZone(),
//                header.getSession(), header.getSessionName(), header.getInteractionType(),
//                new UOctet((short) (stage + 1)), // An error always replaces the next stage.
//                header.getTransactionId(), header.getServiceArea(), header.getService(), header.getOperation(),
//                header.getAreaVersion(), Boolean.TRUE, // Yes, this is an error message.
//                replyToMsg.getQoSProperties(), error.getErrorNumber(), error.getExtraInformation());
//        if (uriFrom != null) {
//            errMsg.getHeader().setURIFrom(uriFrom);
//        }
//        return errMsg;
//    }
//    public MALMessage createMessage(final Blob authenticationId, final URI uriTo, final Time timestamp,
//                                    final QoSLevel qosLevel, final UInteger priority, final IdentifierList domain, final Identifier networkZone,
//                                    final SessionType session, final Identifier sessionName, final InteractionType interactionType,
//                                    final UOctet interactionStage, final Long transactionId, final UShort serviceArea, final UShort service,
//                                    final UShort operation, final UOctet areaVersion, final Boolean isErrorMessage, final Map qosProperties,
//                                    final Object... body) throws IllegalArgumentException, MALException {
//        final SPPMessageHeader msgHeader = new SPPMessageHeader(uri, authenticationId, uriTo, timestamp, qosLevel, priority,
//                domain, networkZone, session, sessionName, interactionType, interactionStage, transactionId,
//                serviceArea, service, operation, areaVersion, isErrorMessage);
//        final MALOperation op = MALContextFactory.lookupArea(serviceArea, areaVersion).getServiceByNumber(service)
//                .getOperationByNumber(operation);
//        return createMessage(uri, uriTo, msgHeader, op, body, null, false, qosProperties);
//    }

}
