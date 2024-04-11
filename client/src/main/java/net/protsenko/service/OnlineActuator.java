package net.protsenko.service;

import net.protsenko.model.EventType;
import net.protsenko.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static net.protsenko.util.RandomUtil.getRandomNumberUsingInts;

public class OnlineActuator {
    private final Function<Request, Object> sendFunction;
    private final Request onlineRequest;

    private boolean close = false;

    private static final Logger log = LoggerFactory.getLogger(OnlineActuator.class);

    public OnlineActuator(Function<Request, Object> sendFunction, String username, String password) {
        this.sendFunction = sendFunction;
        this.onlineRequest = new RequestBuilder(EventType.ONLINE).withUsername(username).withPassword(password).build();
    }

    public void close() {
        this.close = true;
    }

    public void run() {
        var scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (close) scheduler.shutdownNow();
                else {
                    if (getRandomNumberUsingInts(1, 3000) == 1)
                        sendFunction.apply(onlineRequest);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw e;
            }

        }, 10, 10, TimeUnit.MILLISECONDS);
    }

}
