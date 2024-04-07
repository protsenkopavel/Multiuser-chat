package net.protsenko.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Message {

    String from;
    String data;
    String date;

    public Message(@JsonProperty("from") String from, @JsonProperty("data") String data) {
        this.from = from;
        this.data = data;
        this.date = LocalDateTime.now().toString();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Message withDate(String date) {
        this.date = date;
        return this;
    }

    public static Message system(String data) {
        return new Message("system", data).withDate(LocalDateTime.now().toString());
    }
}
