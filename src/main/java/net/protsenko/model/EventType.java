package net.protsenko.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {

    LOGOUT(1, "logout"),
    ONLINE(2, "online"),
    SEND(3,"send");

    private final String name;

    EventType(int i, String name) {
         this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

}
