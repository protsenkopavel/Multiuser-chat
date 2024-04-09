package net.protsenko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.protsenko.model.OutputEvent;
import net.protsenko.model.Status;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MessageDispatcher {
    private final Map<String, PrintWriter> outputs = new ConcurrentHashMap<>();

    private final BlockingQueue<OutputEvent> queue = new ArrayBlockingQueue<>(1000, true);
    private final EOLManager eolManager;
    private final ObjectMapper om;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MessageDispatcher.class);

    public MessageDispatcher(EOLManager eolManager, ObjectMapper om) {
        this.eolManager = eolManager;
        this.om = om;
    }

    public synchronized void addSubscriber(String username, PrintWriter pw) {
        if (username == null || pw == null) throw new IllegalArgumentException("Bad call");

        outputs.put(username, pw);
    }

    public synchronized void removeSubscriber(String username) {
        if (username == null) return;

        outputs.remove(username);
    }

    public void sendEvent(OutputEvent event) {
        try {
            queue.put(event);
            log.info(queue.size() + " размер очереди");
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    OutputEvent event = queue.take();
                    processEvent(event);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }).start();
    }

    private void processEvent(OutputEvent event) throws JsonProcessingException {
        String username = event.getUsername();
        if (event.getResponse() != null) {
            String response = om.writeValueAsString(event.getResponse()) + "\n";

            List<PrintWriter> outs = new ArrayList<>();

            if (username == null) {
                outs.addAll(outputs.values());
            } else {
                PrintWriter pw = outputs.get(username);
                if (pw != null) {
                    outs.add(pw);
                }
            }

            for (PrintWriter out : outs) {
                if (out != null && !out.checkError()) {
                    out.write(response);
                    out.flush();
                }
            }

            if (event.getResponse().getStatus() == Status.CLOSE) {
                PrintWriter outWriter = outputs.remove(username);
                if (outWriter != null && !outWriter.checkError()) {
                    outWriter.close();
                }
                var socketId = event.getSocketId();
                if (socketId != null) eolManager.sendEvent(socketId, "close");
            }
        }
    }

}
