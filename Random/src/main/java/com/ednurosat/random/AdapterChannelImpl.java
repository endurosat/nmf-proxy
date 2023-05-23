package com.ednurosat.random;

import com.endurosat.common.SPPConverter;
import com.endurosat.common.TunnelChannel;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;

import java.util.Map;
import java.util.Properties;

public class AdapterChannelImpl extends TunnelChannel {
    TunnelAdapterInterface adapter;
    public AdapterChannelImpl(Properties properties) throws Exception {
        this.adapter = (TunnelAdapterInterface) Class.forName(properties.getProperty("adapter.class")).newInstance();
    }

    @Override
    public void init(Map properties) throws Exception {
        adapter.init(properties);
    }

    @Override
    public void send(SpacePacket Message) throws Exception {
        adapter.send(SPPConverter.convertSPP(Message));
    }

    @Override
    public SpacePacket receive() throws Exception {
        return SPPConverter.convertByte(adapter.recieve()).getPacket();
    }

    @Override
    public void close() throws Exception {

    }
}
