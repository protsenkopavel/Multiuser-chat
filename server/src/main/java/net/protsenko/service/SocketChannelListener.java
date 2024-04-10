package net.protsenko.service;

import net.protsenko.model.ServerSocketRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class SocketChannelListener extends Thread {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private final RequestHandler RH;
    private final EOLManager eolManager;

    private static final Logger log = LoggerFactory.getLogger(SocketChannelListener.class);

    private boolean blocked = false;

    private final UUID socketId = UUID.randomUUID();

    public SocketChannelListener(Socket socket, RequestHandler rh, EOLManager eolManager) {
        this.clientSocket = socket;
        this.RH = rh;
        this.eolManager = eolManager;
        listenForUpdates();
        log.info("Open socket connection");
    }

    public void run() {
        try (
                Socket socket = clientSocket;
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            this.out = out;
            this.in = in;

            String input;

            while ((input = in.readLine()) != null) {
                if (!blocked) {
                    try {
                        var request = InputParser.parseInputString(input);
                        RH.pushRequest(ServerSocketRequest.of(request, socketId, out));
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }

            RH.logout(socketId);

        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } finally {
            close();
        }
    }


    private void close() {
        try {
            log.info("Closing");
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void listenForUpdates() {
        new Thread(() -> {
            try {
                Boolean value = null;
                while (value == null) {
                    sleep(50);
                    var event = eolManager.getEvent(socketId);
                    if (event != null) {
                        switch (event) {
                            case "block" -> blocked = true;
                            case "close" -> value = true;
                        }
                    }
                }
                close();
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
        }).start();
    }
}