package net.protsenko.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.protsenko.model.*;
import net.protsenko.util.Base64Util;

import java.io.*;
import java.net.Socket;
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
        try (
                Socket socket = clientSocket;
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {

            this.out = out;
            this.in = in;

            String firstInput = in.readLine();
            Request request;
            String password = "";

            if (firstInput != null) {
                logLine("first" + firstInput);
                try {
                    request = InputParser.parseInputString(firstInput);
                    // обработка первого вызова
                    String requestCredentials = request.getCredentials();
                    var creds = Base64Util.decodeBase64Credentials(requestCredentials);
                    username = creds.getLeft();
                    password = creds.getRight();
                    var isAuth = authManager.checkAuth(username, password);
                    if (!isAuth) {
//                        var pswhash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
//                        DBExecutor.insertUser(new User(username, pswhash, false));
                        MD.sendEvent(new OutputEvent(username, new Response(Status.CLOSE, Message.system("Wrong password"))), out);
                    } else {
                        System.out.println("Auth");
                        MD.addSubscriber(username, out, closeFun());
                        DBExecutor.setOnline(username);
                        //send last N messages from chat
                        var history = DBExecutor.lastMessages(10);
                        Collections.reverse(history);
                        for (var msg : history) {
                            MD.sendEvent(new OutputEvent(username, new Response(Status.OK, msg)));
                        }
                        RH.processMessage(new AuthenticatedRequest(request, username));
                        MD.sendEvent(new OutputEvent(username, new Response(Status.OK,  Message.system("Online users: " + DBExecutor.onlineUsers().toString()))));
                    }
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
                    String requestCredentials = request.getCredentials();
                    var creds = Base64Util.decodeBase64Credentials(requestCredentials);
                    String anotherPassword = creds.getRight();
                    if (!anotherPassword.equals(password)) {
                        System.out.println("Попался");
                        MD.sendEvent(new OutputEvent(username, new Response(Status.CLOSE, Message.system("Malware"))), out);
                    }

                    RH.processMessage(new AuthenticatedRequest(request, username));
                } catch (Exception e) {
                    if (e instanceof IOException) throw new IllegalArgumentException("Fuck it up");
                    logWarning(e);
                }
            }
        } catch (IOException e) {
            logWarning(e);
        } finally {
            close();
        }
    }


    private void close() {
        try {
            System.out.println("Closing");
            if (username != null) {
                DBExecutor.setOffline(username);
            }
            if (out != null) {
                out.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logWarning(e);
        }
    }

    private Function<Object, String> closeFun() {
        return o -> {
            System.out.println("Closing from function");
            close();
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