package net.protsenko.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketService {
    private ServerSocket serverSocket;

    private final RequestHandler RH;
    private final EOLManager EOL;

    private final Logger log = LoggerFactory.getLogger(SocketService.class);

    public SocketService(RequestHandler rh, EOLManager eolManager) {
        this.EOL = eolManager;
        this.RH = rh;
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        log.info("Starting socket server at port " + port);
        while (true)
            new SocketChannelListener(serverSocket.accept(), RH, EOL).start();
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

}