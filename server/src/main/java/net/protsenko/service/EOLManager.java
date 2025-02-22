package net.protsenko.service;

import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class EOLManager {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EOLManager.class);
    private final Lock lock = new ReentrantLock();
    private final Map<UUID, List<String>> socketEvents = new ConcurrentHashMap<>();

    private String withLock(Function<Object, String> run) {
        try {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                return run.apply("");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            //release lock
            lock.unlock();
        }
        return null;
    }

    public void sendEvent(UUID id, String eventName) {
        withLock(arg -> {
            var correspondEvents = socketEvents.get(id);
            if (correspondEvents == null || correspondEvents.isEmpty()) {
                socketEvents.put(id, new ArrayList<>(Arrays.asList(eventName)));
            } else {
                correspondEvents.add(eventName);
            }
            return "";
        });
    }

    public String getEvent(UUID id) {
        return withLock(arg -> {
            var correspondEvents = socketEvents.get(id);
            if (correspondEvents == null || correspondEvents.isEmpty())
                return null;
            else {
                return correspondEvents.remove(0);
            }
        });
    }

}
