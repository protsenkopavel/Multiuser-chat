package net.protsenko.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {

    Status status;
    Message message;

    public Response(@JsonProperty("status") Status status, @JsonProperty("message") Message message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
