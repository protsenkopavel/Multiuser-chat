package net.protsenko.service;

import net.protsenko.model.EventType;
import net.protsenko.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.nio.CharBuffer;
import java.util.Scanner;
import java.util.function.Function;

public class OnlinePrompt extends Thread {

    private final Function<Request, Object> sendFunction;
    private final Console console = System.console();

    private boolean close = false;

    private static final Logger log = LoggerFactory.getLogger(OnlinePrompt.class);
    private final RequestBuilder sendRequest;
    private final RequestBuilder logoutRequest;

    public OnlinePrompt(Function<Request, Object> sendFunction, String username, String password) {
        this.sendFunction = sendFunction;
        sendRequest = new RequestBuilder(EventType.SEND).withUsername(username).withPassword(password);
        logoutRequest = new RequestBuilder(EventType.LOGOUT).withUsername(username).withPassword(password);
    }

    public void close() {
        close = true;
    }

    public void run() {
        try (var scanner = new Scanner(System.in)) {
            while (!close) {
                try {
                    var message = scanner.nextLine();
                    if (message != null && !message.isBlank()) {
                        if (message.equals(".")) {
                            close();
                            sendFunction.apply(logoutRequest.build());
                        } else {
                            sendFunction.apply(sendRequest.withMessage(message).build());
                        }
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        log.info("Closed");

    }
}
