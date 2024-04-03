package net.protsenko.model;

public class AuthenticatedRequest extends Request {
    private String username;

    public AuthenticatedRequest(Request request, String username) {
        super(request.eventType, request.credentials, request.message);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
