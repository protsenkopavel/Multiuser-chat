package net.protsenko.service;

import net.protsenko.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class SocketChannelListener extends Thread {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private final DBQueryExecutor DBExecutor;
    private final RequestHandler RH;
    private final EOLManager eolManager;
    private final MessageDispatcher messageDispatcher;

    private static final Logger log = LoggerFactory.getLogger(SocketChannelListener.class);

    private String username = null;
    private boolean blocked = false;

    private final UUID socketId = UUID.randomUUID();

    public SocketChannelListener(Socket socket, DBQueryExecutor dbExecutor, RequestHandler rh, EOLManager eolManager, MessageDispatcher messageDispatcher) {
        this.clientSocket = socket;
        DBExecutor = dbExecutor;
        this.RH = rh;
        this.eolManager = eolManager;
        this.messageDispatcher = messageDispatcher;
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
                        RH.pushRequest(Request.of(request, socketId, out));
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }

            log.info("ABOBA");
            log.info(username);
            RH.removeSocket(socketId);

            if (username != null) {
                messageDispatcher.removeSubscriber(username);
                DBExecutor.setOffline(username);
            }

        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } finally {
            close();
        }
    }


    private void close() {
        try {
            log.info("Closing");
            if (username != null) DBExecutor.setOffline(username);
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