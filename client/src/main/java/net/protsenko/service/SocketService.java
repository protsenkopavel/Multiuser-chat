package net.protsenko.service;

import net.protsenko.ClientApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class SocketService {
    private Socket serverSocket;
    private final UserInputHandler inputHandler;
    private final Logger log = LoggerFactory.getLogger(ClientApp.class);

    public SocketService(UserInputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public void start(String ip, int port) {
        try {
            serverSocket = new Socket(ip, port);
            log.info("Connected to server");
            new ServerCommunicator(serverSocket, inputHandler).run();
        } catch (IOException e) {
            log.info("Could not start socket server");
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }
}