package net.protsenko.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {

    SIGN_IN(1, "signIn"),
    LOGOUT(3, "logout"),
    ONLINE(4, "online"),
    SEND(5, "send");

    private final String name;

    EventType(int i, String name) {
         this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

}
