package net.protsenko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.protsenko.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class ServerCommunicator extends Thread{
    private PrintWriter out;
    private BufferedReader in;
    private final Socket serverSocket;
    private UserInputHandler userInputHandler;
    private RequestBuilder RB;

    private static final Logger log = LoggerFactory.getLogger(ServerCommunicator.class);

    public ServerCommunicator(Socket serverSocket, UserInputHandler userInputHandler) {
        this.serverSocket = serverSocket;
        this.userInputHandler = userInputHandler;
    }

    public void run() {
        try (
                Socket severSocket = serverSocket;
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(severSocket.getOutputStream())), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(severSocket.getInputStream()))
        ) {
            this.out = out;
            this.in = in;

            var creds = UserInputHandler.promptUserCredentials();

            //first request
            String request = null;
            try {
                request = RB.createRequest(EventType.SIGN_UP, creds, "");
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }


            sendMessage(request);

            if (receiveMessage() == null) {
                log.info("No message received");
            } else {
                System.out.println(receiveMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String msg) throws IOException {
        out.println(msg);
//        String resp = in.readLine();
//        return resp;
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }
}
