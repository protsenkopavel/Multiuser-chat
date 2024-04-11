package net.protsenko.service;

import net.protsenko.model.EventType;
import net.protsenko.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.nio.CharBuffer;
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
        while (!close) {
            var buffer = CharBuffer.allocate(1000);
            String message = null;

            try {
                console.reader().read(buffer);
                message = new String(buffer.array()).trim();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            } finally {
                buffer.clear();
            }

            if (message != null && !message.isBlank()) {
                try {
                    if (message.equals(".")) {
                        sendFunction.apply(logoutRequest.build());
                        close();
                    }
                    sendFunction.apply(sendRequest.withMessage(message).build());
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        log.info("Closed");
    }
}
