package net.protsenko.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public String formatMessage() throws JsonProcessingException {
        var om = new ObjectMapper();
        var node = om.createObjectNode();
        node = node.set("status", om.valueToTree(status));
        node = node.set("data", om.valueToTree(data));
        return om.writeValueAsString(node);
    }
}
