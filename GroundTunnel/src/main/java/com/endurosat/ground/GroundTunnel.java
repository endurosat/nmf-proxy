package com.endurosat.ground;


import com.endurosat.common.TunnelEndpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class GroundTunnel {
    public static void main(String[] args) throws Exception {
        System.out.println("launching");
        Properties properties = new Properties();
        System.out.println(new File("").getAbsolutePath());
        InputStream inputStream = new FileInputStream(new File("groundEndpoint.properties"));
        properties.load(inputStream);
        TunnelEndpoint tunnelEndpoint = new TunnelEndpoint(properties, new GSEmulatorTunnel());
        tunnelEndpoint.init();
        tunnelEndpoint.start();
    }
}
