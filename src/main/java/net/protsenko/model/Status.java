package net.protsenko.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {

    OK(1, "ok"),
    ERROR(2, "error");

    private final String name;

    Status(int id, String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

}
