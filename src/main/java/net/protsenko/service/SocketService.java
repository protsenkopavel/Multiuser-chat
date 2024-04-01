package net.protsenko.service;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketService {
    private ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true)
            new SocketChannelListener(serverSocket.accept()).start();
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

}