package es.e1sordo.thesis.wtiat.agent.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class SystemInfoReceiver {

    public static String getSystemIpAddress() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("google.com", 80));
            return socket.getLocalAddress().getHostAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private SystemInfoReceiver() {
    }
}
