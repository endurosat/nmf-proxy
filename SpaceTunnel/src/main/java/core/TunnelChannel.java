package core;

import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;

import java.util.Map;

public abstract class TunnelChannel {
    public abstract void init(Map properties) throws Exception;

    public abstract void send(SpacePacket Message) throws Exception;

    public abstract SpacePacket receive() throws Exception;

    public abstract void close() throws Exception;
}
