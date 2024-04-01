package net.protsenko.service;

import net.protsenko.model.OutputEvent;
import net.protsenko.model.Status;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageDispatcher {

    private Map<String, PrintWriter> ddd = new Hashtable<>();
    private Map<String, Function<Object, Void>> closables = new Hashtable<>();

    private BlockingQueue<OutputEvent> queue = new ArrayBlockingQueue<OutputEvent>(1000, true);

    public synchronized void addSubscriber(String username, PrintWriter pw, Function<Object, Void> releaser) {
        if (username == null || pw == null || releaser == null) throw new IllegalArgumentException("Bad call");

        ddd.put(username, pw);
        closables.put(username, releaser);
    }

    public void sendEvent(OutputEvent event) throws InterruptedException {
        queue.put(event);
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                OutputEvent event = queue.poll();
                if (event.getUsernames() != null && event.getResponse() != null) {
                    List<String> usernames = event.getUsernames();
                    var resp = event.getResponse();
                    if (event.getResponse().getStatus() == Status.CLOSE) {
                        var un = usernames.get(0);
                        var outWriter = ddd.remove(un);
                        if (outWriter != null && !outWriter.checkError()) outWriter.write(resp.formatMessage());
                        try {
                            var closable = closables.remove(un);
                            if (closable != null) closable.apply("");
                        } catch (Exception e) {
                            logWarning(e);
                        }
                    }

                    usernames.forEach(un -> {
                        var outWriter = ddd.get(un);
                        if (outWriter != null && !outWriter.checkError()) outWriter.write(resp.formatMessage());
                    });
                }
            }
        }).start();
    }

    private static MessageDispatcher Instance = null;

    public static MessageDispatcher getInstance() {
        if (Instance == null) {
            Instance = new MessageDispatcher();
        }
        return Instance;
    }

    private void logWarning(Exception e) {
        Logger.getLogger("DispatcherLogger").log(Level.WARNING, e.getMessage(), e);
    }
}
