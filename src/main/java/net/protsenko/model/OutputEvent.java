package net.protsenko.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OutputEvent {
    private List<String> usernames;
    private Response response;

    public OutputEvent(List<String> usernames, Response response) {
        this.usernames = usernames;
        this.response = response;
    }

    public static OutputEvent forOne(String username, Response response) {
        return new OutputEvent(Arrays.asList(username), response);
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
