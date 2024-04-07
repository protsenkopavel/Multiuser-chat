package net.protsenko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.protsenko.model.OutputEvent;
import net.protsenko.model.Status;

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
    private final EOLManager eolManager = EOLManager.getINSTANCE();
    private final ObjectMapper om = new ObjectMapper();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EOLManager.class);

    private static volatile MessageDispatcher INSTANCE = null;

    private MessageDispatcher() {
    }

    public static MessageDispatcher getINSTANCE() {
        if (INSTANCE == null) {
            synchronized (MessageDispatcher.class) {
                INSTANCE = new MessageDispatcher();
                INSTANCE.start();
            }
        }
        return INSTANCE;
    }

    public synchronized void addSubscriber(String username, PrintWriter pw) {
        if (username == null || pw == null) throw new IllegalArgumentException("Bad call");

        outputs.put(username, pw);
    }

    public void sendEvent(OutputEvent event) {
        try {
            queue.put(event);
            System.out.println(queue.size() + " размер очереди");
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void start() {
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
                if (out != null && !out.checkError()) out.write(response);
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

//    {"eventType": "send", "message": "hello", "credentials": "QWRtaW46aGVsbG8="}
//    {"eventType": "send", "message": "hello", "credentials": "QWRTaW46aGVsbG7="}
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


//    {"eventType": "signUp", "message": "", "credentials": "VGVzdHVzZXI6cGFzc3dvcmQ="}
//    {"eventType": "signUp", "message": "", "credentials": "UGF2ZWw6cGFzc3dvcmQ="}
//    {"eventType": "signUp", "message": "", "credentials": "QW5vdGhlclVzZXI6cGFzc3dvcmQ="}
//    {"eventType": "signUp", "message": "", "credentials": "b2hTaMSxdEhlcmVXZUdvQWdhaW46cGFzc3dvcmQ="}
//    {"eventType": "online", "message": "hello123", "credentials": "T3BlcmF0b3I6aGVsbG8xMjM="}
//    {"eventType": "logout", "message": "", "credentials": "T3BlcmF0b3I6aGVsbG8xMjM="}

}
