package net.protsenko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.protsenko.model.EventType;
import net.protsenko.model.Request;
import net.protsenko.model.Response;
import net.protsenko.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class ServerCommunicator {
    private PrintWriter out;
    private BufferedReader in;
    private final Socket serverSocket;
    private final UserInputHandler userInputHandler;

    private final ObjectMapper om = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(ServerCommunicator.class);

    public ServerCommunicator(Socket serverSocket, UserInputHandler userInputHandler) {
        this.serverSocket = serverSocket;
        this.userInputHandler = userInputHandler;
    }

    public void run() {
        try (
                Socket severSocket = serverSocket;
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(severSocket.getOutputStream())));
                BufferedReader in = new BufferedReader(new InputStreamReader(severSocket.getInputStream()))
        ) {
            log.info("Connected to server");

            this.out = out;
            this.in = in;

            var creds = userInputHandler.promptUserCredentials();

            //first request
            var signInRequest = RequestBuilder.signInRequest().withUsername(creds.key()).withPassword(creds.value());

            var signInResp = requestResponse(signInRequest.build());

            if (responseUserNotExists(signInResp)) {
                var signUpResponse = requestResponse(signInRequest.withEventType(EventType.SIGN_UP).build());

                    if (responseSignUpSuccess(signUpResponse)) {
                        requestResponse(signInRequest.withEventType(EventType.SIGN_IN).build());
                        //loop
                    }
            } else {
                if (responseSignInSuccess(signInResp)) {
                    //loop
                } else {
                    //loop creds
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Request request) {
        try {
            var msg = om.writeValueAsString(request);
            log.info("Sending message: {}", msg);
            out.println(msg);
            out.flush();
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    public Response requestResponse(Request request) {
        try {
            var msg = om.writeValueAsString(request);
            log.info("Sending message: {}", msg);
            out.println(msg);
            out.flush();
            var response = receiveMessage();

            if (response == null) {
                log.warn("No message received");
            } else {
                log.info("Received message: {}", response);
                return response;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public Response receiveMessage() throws IOException {
        return om.readValue(in.readLine(), Response.class);
    }

    private boolean responseOk(Response response) {
        return response.getStatus() == Status.OK;
    }

    private boolean responseError(Response response) {
        return response.getStatus() == Status.ERROR;
    }

    private boolean responseFromSystem(Response response) {
        return response.getMessage().getFrom().equals("system");
    }

    private boolean responseMessageIs(Response response, String msg) {
        return response.getMessage().getData().equals(msg);
    }

    private boolean responseUserNotExists(Response response) {
        return responseError(response) &&
                responseFromSystem(response) &&
                responseMessageIs(response, "Authentication failed: user not exists");
    }

    private boolean responseSignInSuccess(Response response) {
        return responseOk(response) &&
                responseFromSystem(response) &&
                responseMessageIs(response, "Successfully logged in");
    }

    private boolean responseSignUpSuccess(Response response) {
        return responseOk(response) &&
                responseFromSystem(response) &&
                responseMessageIs(response, "Successfully signed up");
    }
}
