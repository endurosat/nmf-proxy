package com.endurosat.space;

import com.endurosat.common.TunnelEndpoint;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class SpaceLauncher {
    public static void main(String[] args) throws Exception {
        System.out.println("Launching Space tunnel");
        Properties properties = new Properties();
        File file = new File("spaceEndpoint.properties");
        if(!file.exists()){
            file.createNewFile();
        }
        InputStream inputStream = new FileInputStream(file);
        properties.load(inputStream);
        TunnelEndpoint tunnelEndpoint = new TunnelEndpoint(properties, new ByteStreamSocket());
        tunnelEndpoint.init();
        tunnelEndpoint.start();
    }
}
