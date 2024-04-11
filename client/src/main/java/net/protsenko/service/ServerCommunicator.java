package net.protsenko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.protsenko.model.EventType;
import net.protsenko.model.Request;
import net.protsenko.model.Response;
import net.protsenko.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static net.protsenko.util.ConsoleColorUtils.*;
import static net.protsenko.util.DateFormatterUtil.*;

public class ServerCommunicator {
    private PrintWriter out;
    private BufferedReader in;
    private final Socket serverSocket;
    private final UserInputHandler userInputHandler;

    private final ObjectMapper om = new ObjectMapper();
    private final Console console = System.console();

    OnlinePrompt onlinePrompt = null;
    OnlineActuator onlineActuator = null;

    private Pair<String, String> creds = null;

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
            log.info("Start session");

            this.out = out;
            this.in = in;

            int onlineUsers = 0;
            var loop = true;

            ClientState clientState = ClientState.FOR_CREDENTIALS;

            while (loop) {
                switch (clientState) {
                    case FOR_CREDENTIALS -> {
                        console.printf(systemConsoleOut(onlineUsers, "Начало сессии!"));
                        creds = userInputHandler.promptUserCredentials();
                        clientState = ClientState.FOR_SIGN_IN;
                    }
                    case FOR_SIGN_IN -> {
                        var signInRequest = RequestBuilder.signInRequest().withUsername(creds.key()).withPassword(creds.value());
                        var signInResp = requestResponse(signInRequest.build());
                        if (signInResp.isUserNotExists()) {
                            clientState = ClientState.FOR_SIGN_UP;
                        } else {
                            if (signInResp.isSignInSuccess()) {
                                console.printf(systemConsoleOut(onlineUsers, "Успешный вход!"));
                                clientState = ClientState.LISTEN;
                            } else {
                                console.printf(systemConsoleOut(onlineUsers, "Требуется регистрация!"));
                                var logoutReq = new RequestBuilder(EventType.LOGOUT).withUsername(creds.key()).withPassword(creds.value());
                                requestResponse(logoutReq.build());
                            }
                        }
                    }
                    case FOR_SIGN_UP -> {
                        var signUpRequest = RequestBuilder.signUpRequest().withUsername(creds.key()).withPassword(creds.value()).build();
                        var signUpResponse = requestResponse(signUpRequest);
                        if (signUpResponse.isSignUpSuccess()) {
                            console.printf(systemConsoleOut(onlineUsers, "Успешная регистрация!"));
                            clientState = ClientState.FOR_SIGN_IN;
                        } else {
                            console.printf(systemConsoleOut(onlineUsers, "Ошибка регистрации!"));
                            clientState = ClientState.FOR_CREDENTIALS;
                        }
                    }
                    case LISTEN -> {
                        checkAndStartOnlineActuator(creds);
                        checkAndStartOnlinePrompt(creds);

                        var response = parseResponse(waitForResponse());
                        var messageFrom = response.getMessage().getFrom();
                        var messageData = response.getMessage().getData();
                        var messageDate = response.getMessage().getDate();
                        switch (response.getStatus()) {
                            case OK -> {

                                if (messageFrom.equals("system")) {
                                    if (messageData.startsWith("Online users: "))
                                        onlineUsers = Integer.parseInt(messageData.split(": ")[1]);
                                } else {
                                    var consoleOut = consoleOut(onlineUsers, messageFrom, messageDate, messageData, false);

                                    console.printf(consoleOut);
                                }
                            }
                            case ERROR -> {
                                log.warn("Received error message: {}", response);
                                var consoleOut = consoleOut(onlineUsers, messageFrom, messageDate, messageData, true);

                                console.printf(consoleOut);
                            }
                            default -> {
                                console.printf(systemConsoleOut(onlineUsers, "Конец сессии"));
                                log.info("End of output");
                                loop = false;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (creds != null) {
                var logoutRequest = new RequestBuilder(EventType.LOGOUT).withUsername(creds.key()).withPassword(creds.value());
                requestFireAndForget(logoutRequest.build());
            }
            log.error(e.getMessage(), e);
        } finally {
            if (onlinePrompt != null) {
                onlinePrompt.close();
            }
            if (onlineActuator != null) {
                onlineActuator.close();
            }
        }
    }

    private void checkAndStartOnlineActuator(Pair<String, String> credentials) {
        if (onlineActuator == null) {
            onlineActuator = new OnlineActuator(this::requestFireAndForget, credentials.key(), credentials.value());
            onlineActuator.run();
        }
    }

    private void checkAndStartOnlinePrompt(Pair<String, String> credentials) {
        if (onlinePrompt == null) {
            onlinePrompt = new OnlinePrompt(this::requestFireAndForget, credentials.key(), credentials.value());
            onlinePrompt.start();
        }
    }

    public Object requestFireAndForget(Request request) {
        try {
            var msg = om.writeValueAsString(request);
            log.info("Sending message: {}", msg);
            out.println(msg);
            out.flush();
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public Response requestResponse(Request request) {
        try {
            requestFireAndForget(request);
            var response = waitForResponse();

            if (response == null) {
                log.warn("No message received");
            } else {
                log.info("Received message: {}", response);
                return parseResponse(response);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public String waitForResponse() throws IOException {
        var received = in.readLine();
        if (received != null) log.info("Received message: {}", received);
        return received;
    }

    public Response parseResponse(String response) throws IOException {
        return om.readValue(response, Response.class);
    }

    private static String consoleOut(int onlineUsers, String from, String date, String data, boolean isError) {
        var dateP = LocalDateTime.parse(date, fullDateTimeFormatter);
        var isToday = dateP.isEqual(LocalDateTime.now(ZoneId.systemDefault()));
        var currentDTParser = isToday ? dateTimeFormatter : fullDateTimeFormatter;

        return GREEN + "[ONLINE·" + onlineUsers + ']' + RESET +
                CYAN + "[" + from + "]" + RESET + ':' +
                YELLOW + '[' + dateP.format(currentDTParser) + ']' + RESET + ": " +
                (isError ? RED : WHITE) + data + RESET + "\n";
    }

    private static String systemConsoleOut(int onlineUsers, String text) {
        return consoleOut(onlineUsers, "Application", LocalDateTime.now().format(fullDateTimeFormatter), text, false);
    }

}
