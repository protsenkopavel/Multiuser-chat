package net.protsenko.service;

import net.protsenko.model.*;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class SocketChannelListener extends Thread {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private final DBQueryExecutor DBExecutor = DBQueryExecutor.getINSTANCE();
    private final RequestHandler RH = RequestHandler.getINSTANCE();
    private final EOLManager eolManager = EOLManager.getINSTANCE();

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EOLManager.class);

    private String username = null;
    private boolean blocked = false;

    private final UUID socketId = UUID.randomUUID();

    public SocketChannelListener(Socket socket) {
        this.clientSocket = socket;
        listenForUpdates();
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

            //if user not exists -> state is waiting for SIGN_UP
            //if user not online -> state is waiting for SIGN_IN
            //if user online -> state is waiting for ONLINE, SEND, LOGOUT

            //state: NULL -> SIGN_UP / SEND (+ check auth) / ONLINE (+ check auth) / LOGOUT (+ check auth)
            //state (with auth) -> SEND / ONLINE / LOGOUT

        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } finally {
            close();
        }
    }


    private void close() {
        try {
            System.out.println("Closing");
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