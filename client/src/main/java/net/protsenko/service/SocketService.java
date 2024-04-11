package net.protsenko.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class SocketService {
    private Socket serverSocket;
    private final UserInputHandler inputHandler;
    private final Logger log = LoggerFactory.getLogger(SocketService.class);

    public SocketService(UserInputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public void start(String ip, int port) {
        try {
            serverSocket = new Socket(ip, port);
            log.info("Connected to server");
            System.out.println("Connected to server");
            new ServerCommunicator(serverSocket, inputHandler).run();
        } catch (IOException e) {
            log.info("Could not connect to socket server");
            System.out.println("Could not connect to socket server");
        }
    }

    public void stop() throws IOException {
        System.out.println("Stop socket service");
        serverSocket.close();
    }
}