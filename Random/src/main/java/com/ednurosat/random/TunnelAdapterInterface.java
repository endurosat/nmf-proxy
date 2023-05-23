package com.ednurosat.random;

import java.io.IOException;
import java.util.Map;

public interface TunnelAdapterInterface {
    public abstract void init(Map properties) throws Exception;

    public abstract void send(byte[] packet) throws IOException;

    public abstract byte[] recieve() throws IOException, ClassNotFoundException;

}
