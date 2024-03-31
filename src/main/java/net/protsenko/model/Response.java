package net.protsenko.model;

public class Response {

    Status status;

    String data;

    public Response(Status status, String message) {
        this.status = status;
        this.data = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
