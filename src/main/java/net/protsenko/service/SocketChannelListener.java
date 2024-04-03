package net.protsenko.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.protsenko.model.*;
import net.protsenko.service.InputParser;
import net.protsenko.util.Base64Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketChannelListener extends Thread {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private final AuthManager authManager = AuthManager.getInstance();
    private final MessageDispatcher MD = MessageDispatcher.getInstance();
    private final DBQueryExecutor DBExecutor = DBQueryExecutor.getInstance();
    private final RequestHandler RH = RequestHandler.getInstance();

    private String username = null;

    public SocketChannelListener(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            String firstInput = in.readLine();
            Request request;
            String credentials = "";

            if (firstInput != null) {
                logLine("first" + firstInput);
                try {
                    request = InputParser.parseInputString(firstInput);
                    // обработка первого вызова
                    String requestCredentials = request.getCredentials();
                    var creds = Base64Util.decodeBase64Credentials(requestCredentials);
                    username = creds.getLeft();
                    MD.addSubscriber(username, out, closeFun());
                    var isAuth = authManager.checkAuth(username, creds.getRight());
                    if (!isAuth) {
                        try {
                            DBExecutor.getUser(username);
                            MD.sendEvent(new OutputEvent(username, new Response(Status.CLOSE, Message.system("Wrong password"))));
                        } catch (IndexOutOfBoundsException e) {
                            var pswhash = BCrypt.withDefaults().hashToString(12, creds.getRight().toCharArray());
                            DBExecutor.insertUser(new User(username, pswhash, false));
                        }
                    } else {
                        System.out.println("Auth");
                        DBExecutor.setOnline(username);
                        var history = DBExecutor.lastMessages(10);
                        Collections.reverse(history);
                        for (var msg : history) {
                            MD.sendEvent(new OutputEvent(username, new Response(Status.OK, msg)));
                        }

                        RH.processMessage(new AuthenticatedRequest(request, username));
                        RH.processMessage(new AuthenticatedRequest(request, username));
                        credentials = request.getCredentials();
                    }
                    //send last N messages from chat
                } catch (Exception e) {
                    logWarning(e);
                    close();
                    return;
                }
            }

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                logLine("continous" + inputLine);
                try {
                    request = InputParser.parseInputString(inputLine);
                    if (!request.getCredentials().equals(credentials)) {
                        MD.sendEvent(new OutputEvent(username, new Response(Status.CLOSE,  Message.system("Malware"))));
                        break;
                    }

                    RH.processMessage(new AuthenticatedRequest(request, username));
                } catch (Exception e) {
                    if (e instanceof IOException) throw new IllegalArgumentException("Fuck it up");
                    logWarning(e);
                }
            }
        } catch (IOException e) {
            logWarning(e);
        }
    }


    private void close() {
        try {
            System.out.println("Closing");
            if (username != null) DBExecutor.setOffline(username);
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {

        }
    }

    private Function<Object, String> closeFun() {
        return o -> {
            try {
                System.out.println("Closing");
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println(e);
            }
            return "";
        };
    }

    private void logWarning(Exception e) {
        Logger.getLogger("ListenerLogger").log(Level.WARNING, e.getMessage(), e);
    }

    private void logLine(String line) {
        Logger.getLogger("ListenerLogger").log(Level.INFO, "Received message: " + line);
    }
}