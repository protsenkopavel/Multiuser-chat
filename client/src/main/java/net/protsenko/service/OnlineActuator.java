package net.protsenko.service;

import net.protsenko.model.EventType;
import net.protsenko.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class OnlineActuator {
    private final Function<Request, Object> sendFunction;
    private final String username;
    private final String password;

    private static final Logger log = LoggerFactory.getLogger(OnlineActuator.class);

    public OnlineActuator(Function<Request, Object> sendFunction, String username, String password) {
        this.sendFunction = sendFunction;
        this.username = username;
        this.password = password;
    }

    public void run() {
        var scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                sendFunction.apply(new RequestBuilder(EventType.ONLINE).withUsername(username).withPassword(password).build());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw e;
            }
        }, 10, 30, TimeUnit.SECONDS);
    }

}
