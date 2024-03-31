package net.protsenko.model;

public class Request {

    EventType eventType;

    String credentials;

    String message;


    public Request(EventType eventType, String credentials, String message) {
        this.eventType = eventType;
        this.credentials = credentials;
        this.message = message;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
