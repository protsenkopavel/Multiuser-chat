package net.protsenko.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class Response {

    @JsonProperty
    Status status;
    @JsonProperty
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

    public boolean isOk() {
        return status == Status.OK;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    public boolean isFromSystem() {
        return message.getFrom().equals("system");
    }

    public boolean hasMessageDataIs(String msg) {
        return message.getData().equals(msg);
    }

    public boolean isUserNotExists() {
        return isError() &&
                isFromSystem() &&
                hasMessageDataIs("Authentication failed: user not exists");
    }

    public boolean isSignInSuccess() {
        return isOk() &&
                isFromSystem() &&
                hasMessageDataIs("Successfully logged in");
    }

    public boolean isSignUpSuccess() {
        return isOk() &&
                isFromSystem() &&
                hasMessageDataIs("Successfully signed up");
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", message=" + message +
                '}';
    }
}
