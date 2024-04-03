package net.protsenko.service;

import net.protsenko.model.*;

public class RequestHandler {

    private final MessageDispatcher MD = MessageDispatcher.getInstance();
    private final DBQueryExecutor DBQE = DBQueryExecutor.getInstance();

    public void processMessage(AuthenticatedRequest request) {
        System.out.println(request.getEventType());
        if (request.getEventType() == EventType.LOGOUT) {
            MD.sendEvent(new OutputEvent(request.getUsername(), new Response(Status.CLOSE, Message.system("Close the connection"))));
            return;
        }
        if (request.getEventType() == EventType.ONLINE) {
            MD.sendEvent(new OutputEvent(request.getUsername(), new Response(Status.OK,  Message.system("Online users: " + DBQE.onlineUsers().toString()))));
            return;
        }
        MD.sendEvent(new OutputEvent(null, new Response(Status.OK, new Message(request.getUsername(), request.getMessage(), null))));
        DBQE.saveMessage(request.getUsername(), request.getMessage());
    }

    private static RequestHandler Instance = null;

    public static RequestHandler getInstance() {
        if (Instance == null) Instance = new RequestHandler();
        return Instance;
    }

}
