package com.ednurosat.random;

import com.endurosat.common.TunnelEndpoint;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
public class Launcher {
    public static void main(String[] args) throws Exception {
        Properties properties1 = new Properties();
        Properties properties2 = new Properties();
        InputStream inputStream1 = Launcher.class.getResourceAsStream("endpoint1.properties");
        InputStream inputStream2 = Launcher.class.getResourceAsStream("endpoint2.properties");
        if (inputStream1 != null && inputStream2 != null) {
            properties1.load(inputStream1);
            properties2.load(inputStream2);
        } else {
            throw new FileNotFoundException("Missing Property file");
        }
        TunnelEndpoint tunnelEndpoint = new TunnelEndpoint(properties1,new TCPTunnelSpacePacketImpl());
        TunnelEndpoint tunnelEndpoint1 = new TunnelEndpoint(properties2,new TCPTunnelSpacePacketImpl());
        tunnelEndpoint.init();
        tunnelEndpoint1.init();
        tunnelEndpoint.start();
        tunnelEndpoint1.start();
        Scanner input = new Scanner(System.in);
        while (true) {
            if (input.next().equals("close")) {
                tunnelEndpoint.close();
                break;
            }
        }

    }
}
