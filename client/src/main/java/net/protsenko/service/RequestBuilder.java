package net.protsenko.service;

import net.protsenko.model.EventType;
import net.protsenko.model.Request;
import net.protsenko.util.Base64Util;

public class RequestBuilder {

    private EventType et;
    private String username = "";
    private String password = "";
    private String credentials = null;
    private String message = null;

    public RequestBuilder(EventType et) {
        this.et = et;
    }

    public Request build() {
        return new Request(
                et, 
                (credentials != null) ? credentials : Base64Util.encodeBase64Credentials(username, password), 
                message
        );
    }

    public static RequestBuilder signInRequest() {
        return new RequestBuilder(EventType.SIGN_IN);
    }
    
    public RequestBuilder withCredentials(String credentials) {
        this.credentials = credentials;
        return this;
    }
    
    public RequestBuilder withUsername(String username) {
        if (username != null) this.username = username;
        return this;
    }
    
    public RequestBuilder withPassword(String password) {
        if (password != null) this.password = password;
        return this;
    }

    public RequestBuilder withMessage(String message) {
        if (message != null) this.message = message;
        return this;
    }

    public RequestBuilder withEventType(EventType et) {
        if (et != null) this.et = et;
        return this;
    }

//    public String createRequest(EventType eventType, String credentials, String message) throws JsonProcessingException {
//        return om.writeValueAsString(new Request(eventType, credentials, message));
//    }
}
