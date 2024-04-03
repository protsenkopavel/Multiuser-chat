package net.protsenko.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OutputEvent {
    private String username;
    private Response response;

    public OutputEvent(String username, Response response) {
        this.username = username;
        this.response = response;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "OutputEvent{" +
                "username='" + username + '\'' +
                ", response=" + response +
                '}';
    }
}
