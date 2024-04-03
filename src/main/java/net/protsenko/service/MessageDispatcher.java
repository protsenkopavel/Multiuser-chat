package net.protsenko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.protsenko.model.OutputEvent;
import net.protsenko.model.Status;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageDispatcher {

    private final Map<String, PrintWriter> outputs = new Hashtable<>();
    private final Map<String, Function<Object, String>> closables = new Hashtable<>();
    private final BlockingQueue<OutputEvent> queue = new ArrayBlockingQueue<>(1000, true);
    private final ObjectMapper om = new ObjectMapper();

    public synchronized void addSubscriber(String username, PrintWriter pw, Function<Object, String> releaser) {
        if (username == null || pw == null || releaser == null) throw new IllegalArgumentException("Bad call");

        outputs.put(username, pw);
        closables.put(username, releaser);
    }

    public void sendEvent(OutputEvent event) {
        try {
            queue.put(event);
            System.out.println(queue.size());
        } catch (InterruptedException e) {
            logWarning(e);
        }
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    OutputEvent event = queue.poll();

                    if (event == null) continue;
                    System.out.println("Process event " + event);

                    if (event.getResponse() != null) {
                        String username = event.getUsername();
                        var resp = event.getResponse();
                        var respString = om.writeValueAsString(resp) + "\n";
                        if (event.getResponse().getStatus() == Status.CLOSE) {

                            if (username == null) continue;
                            DBQueryExecutor.getInstance().setOffline(username);

                            var outWriter = outputs.remove(username);
                            if (outWriter != null && !outWriter.checkError()) {
                                outWriter.write(respString);
                            }
                            var closable = closables.remove(username);
                            if (closable != null) closable.apply("");
                        }

                        List<PrintWriter> outs = List.of();

                        if (username == null) outs = outputs.values().stream().toList();
                        else if (outputs.containsKey(username)) outs = List.of(outputs.get(username));

                        for (var out : outs) {
                            if (out != null && !out.checkError()) {
                                var to = outputs.entrySet().stream().filter(e -> e.getValue() == out).map(Map.Entry::getKey).findFirst();
                                System.out.println("Send message " + respString +  " to " +  to);
                                out.write(respString);
                                out.flush();
                            }
                        }
                    }
                } catch (Exception e) {
                    logWarning(e);
                }
            }
        }).start();
    }

    private static MessageDispatcher Instance = null;
//    {"eventType": "send", "message": "hello", "credentials": "QWRtaW46aGVsbG8="}
//    {"eventType": "online", "message": "hello", "credentials": "QWRtaW46aGVsbG8="}
//    {"eventType": "logout", "message": "hello", "credentials": "QWRtaW46aGVsbG8="}

    ///[N Online][Sender] Message
    // [3 Online] [Admin] Привет
    // [3 Online] [Operator] Привет админ
    // [3 Online] [CEO] Запускайте гуся, работяги
    // prompt

//    {"eventType": "send", "message": "test 5687", "credentials": "T3BlcmF0b3I6aGVsbG8xMjM="}
//    {"eventType": "online", "message": "hello123", "credentials": "T3BlcmF0b3I6aGVsbG8xMjM="}
//    {"eventType": "logout", "message": "", "credentials": "T3BlcmF0b3I6aGVsbG8xMjM="}



    public static MessageDispatcher getInstance() {
        if (Instance == null) {
            Instance = new MessageDispatcher();
            Instance.start();
        }
        return Instance;
    }

    private void logWarning(Exception e) {
        Logger.getLogger("DispatcherLogger").log(Level.WARNING, e.getMessage(), e);
    }
}
