package com.ednurosat.random;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class TcpAdapterImpl implements TunnelAdapterInterface {

    Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;

    public void init(Map properties) throws Exception {
        int port = Integer.parseInt((String) properties.get("Tunnel.Port"));
        if (properties.get("Tunnel.isServer").equals("true")) {
            System.out.println("waiting for other end");
            socket = new ServerSocket(port).accept();
        } else {
            String host = (String) properties.get("Tunnel.Host");
            boolean working = false;
            System.out.println("setting up");
            while (!working){
                try {
                    socket = new Socket(host, port);
                    working=true;
                    System.out.println("connected to serverSocket");
                }
                catch (Exception e){
                    e.printStackTrace();
                    System.out.println("connection failed retrying");
                }
            }
        }
        os = new ObjectOutputStream(socket.getOutputStream());
        is = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void send(byte[] packet) throws IOException {
        System.out.println(packet.length);
        os.writeObject(packet);
        os.flush();
    }

    @Override
    public byte[] recieve() throws IOException, ClassNotFoundException {
        byte[] bytes = (byte[]) is.readObject();
        return bytes;
    }
}
