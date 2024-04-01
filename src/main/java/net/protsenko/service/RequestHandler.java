package net.protsenko.service;

import net.protsenko.model.Request;

public class RequestHandler {

    private final MessageDispatcher MD = MessageDispatcher.getInstance();

    public void processMessage(Request request) {

    }

    private static RequestHandler Instance = null;

    public static RequestHandler getInstance() {
        if (Instance == null) Instance = new RequestHandler();
        return Instance;
    }

}
