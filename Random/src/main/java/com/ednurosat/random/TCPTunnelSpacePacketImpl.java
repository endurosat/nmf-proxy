package com.ednurosat.random;

import com.endurosat.common.TunnelChannel;
import org.ccsds.moims.mo.testbed.util.spp.SpacePacket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class TCPTunnelSpacePacketImpl extends TunnelChannel {
    Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;

    @Override
    public void init(Map properties) throws Exception {
        int port = Integer.parseInt((String) properties.get("Tunnel.Port"));
        if (properties.get("Tunnel.isServer").equals("true")) {
            System.out.println("waiting for other end");
            socket = new ServerSocket(port).accept();
        } else {
            String host = (String) properties.get("Tunnel.Host");
            socket = new Socket(host, port);
        }
        os = new ObjectOutputStream(socket.getOutputStream());
        is = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void send(SpacePacket Message) throws Exception {
        os.writeObject(new SPPWrapper(Message));
        os.flush();
    }

    @Override
    public SpacePacket receive() throws Exception {
        SPPWrapper sp = (SPPWrapper) is.readObject();
        return sp.readPacket();
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }


}
