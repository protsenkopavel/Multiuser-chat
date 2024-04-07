package net.protsenko.model;

import java.util.UUID;

public class OutputEvent {
    private String username;
    private Response response;

    private UUID socketId;

    public OutputEvent(String username, Response response) {
        this.username = username;
        this.response = response;
    }

    public OutputEvent withSocketId(UUID socketId) {
        this.socketId = socketId;
        return this;
    }

    public static OutputEvent Closed(String username, String message) {
        return new OutputEvent(username, new Response(Status.CLOSE, Message.system(message)));
    }

    public static OutputEvent Ok(String username, Message message) {
        return new OutputEvent(username, new Response(Status.OK, message));
    }

    public static OutputEvent Error(String username, String message) {
        return new OutputEvent(username, new Response(Status.ERROR, Message.system(message)));
    }

    public String getUsername() {
        return this.username;
    }

    public Response getResponse() {
        return this.response;
    }

    public UUID getSocketId() {
        return this.socketId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public void setSocketId(UUID socketId) {
        this.socketId = socketId;
    }

    public String toString() {
        return "OutputEvent(username=" + this.getUsername() + ", response=" + this.getResponse() + ", socketId=" + this.getSocketId() + ")";
    }
}
