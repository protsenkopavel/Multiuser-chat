package net.protsenko.service;

import net.protsenko.model.*;
import net.protsenko.util.Base64Util;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class RequestHandler {

    private final MessageDispatcher MD;
    private final DBQueryExecutor DBQE;
    private final AuthManager authManager;
    private final EOLManager eolManager;

    private final BlockingQueue<Request> queue = new ArrayBlockingQueue<>(1000, true);
    private final Map<UUID, String> socketUserMap = new ConcurrentHashMap<>();
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EOLManager.class);

    public RequestHandler(MessageDispatcher md, DBQueryExecutor DBQE, AuthManager authManager, EOLManager eolManager) {
        this.MD = md;
        this.DBQE = DBQE;
        this.authManager = authManager;
        this.eolManager = eolManager;
    }

    public void pushRequest(Request event) {
        try {
            queue.put(event);
            log.info("Размер очереди входящих сообщений:" + queue.size());
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public void logout(UUID id) {
        var username = socketUserMap.remove(id);
        DBQE.setOffline(username);
        MD.removeSubscriber(username);

    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Request request = queue.take();
                    processRequest(request);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }).start();
    }

    private void processRequest(Request request) {
        var creds = Base64Util.decodeBase64Credentials(request.getCredentials());
        var username = creds.getLeft();
        var password = creds.getRight();
        User user;

        try {
            user = DBQE.getUser(username);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return;
        }

        var correspondSocket = socketUserMap.get(request.getSenderId());

        if ((user == null && correspondSocket != null)) {
            eolManager.sendEvent(request.getSenderId(), "block");
            eolManager.sendEvent(request.getSenderId(), "close");
            return;
        }

        socketUserMap.put(request.getSenderId(), username);

        var userState = userState(user);

        if (userState == null) return;

        switch (userState) {
            case NOT_EXISTS -> {
                if (request.getEventType() == EventType.SIGN_UP) {
                    var pswhash = authManager.passwordHash(password);
                    DBQE.insertUser(new User(username, pswhash, false));
                    MD.addSubscriber(username, request.getPw());
                    MD.sendEvent(OutputEvent.Ok(username, Message.system("Successfully signed up")));
                }
            }
            case OFFLINE -> {
                MD.addSubscriber(username, request.getPw());
                var isAuth = authManager.checkAuth(user, password);
                if (!isAuth) {
                    eolManager.sendEvent(request.getSenderId(), "block");
                    MD.sendEvent(OutputEvent.Closed(username, "Wrong password").withSocketId(request.getSenderId()));
                } else {
                    if (request.getEventType() != EventType.SIGN_IN) {
                        eolManager.sendEvent(request.getSenderId(), "block");
                        MD.sendEvent(OutputEvent.Closed(username, "Bad request").withSocketId(request.getSenderId()));
                    } else {
                        DBQE.setOnline(username);
                        MD.sendEvent(loggedIn(username));
                        MD.sendEvent(onlineUsers(username));
                        var history = DBQE.lastMessages(10);
                        Collections.reverse(history);
                        for (var msg : history) MD.sendEvent(new OutputEvent(username, new Response(Status.OK, msg)));
                    }
                }
            }
            case ONLINE -> {
                MD.addSubscriber(username, request.getPw());
                var isAuth = authManager.checkAuth(user, password);
                if (!isAuth) {
                    eolManager.sendEvent(request.getSenderId(), "block");
                    MD.sendEvent(OutputEvent.Closed(username, "Bad credentials").withSocketId(request.getSenderId()));
                } else {
                    switch (request.getEventType()) {
                        case LOGOUT -> {
                            DBQE.setOffline(username);
                            eolManager.sendEvent(request.getSenderId(), "block");
                            MD.sendEvent(OutputEvent.Closed(username, "Close the connection").withSocketId(request.getSenderId()));
                        }
                        case ONLINE -> MD.sendEvent(onlineUsers(username));
                        case SEND -> {
                            MD.sendEvent(OutputEvent.Ok(null, new Message(username, request.getMessage())));
                            DBQE.saveMessage(username, request.getMessage());
                        }
                        default -> MD.sendEvent(OutputEvent.Error(username, "Bad request"));
                    }
                }
            }
        }
    }

    private UserState userState(User user) {
        if (user == null) return UserState.NOT_EXISTS;
        return user.isOnline() ? UserState.ONLINE : UserState.OFFLINE;
    }

    private OutputEvent onlineUsers(String username) {
        return OutputEvent.Ok(username, Message.system("Online users: " + DBQE.onlineUsers().toString()));
    }

    private OutputEvent loggedIn(String username) {
        return OutputEvent.Ok(username, Message.system("Successfully logged in"));
    }
}
