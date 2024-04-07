package net.protsenko.model;

import java.io.PrintWriter;
import java.util.UUID;

public class Request extends PreRequest {
    private UUID senderId;

    private PrintWriter pw;

    private Request(EventType eventType, String credentials, String message, UUID senderId, PrintWriter pw) {
        super(eventType, credentials, message);
        this.senderId = senderId;
        this.pw = pw;
    }

    public static Request of(PreRequest r, UUID senderId, PrintWriter pw) {
        return new Request(r.eventType, r.credentials, r.message, senderId, pw);
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public PrintWriter getPw() {
        return pw;
    }

    public void setPw(PrintWriter pw) {
        this.pw = pw;
    }
}
