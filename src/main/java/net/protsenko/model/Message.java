package net.protsenko.model;

import java.time.LocalDateTime;

public class Message {

    String from;
    String data;
    String date;

    public Message(String from, String data, String date) {
        this.from = from;
        this.data = data;
        this.date = (date != null) ? date : LocalDateTime.now().toString();
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

    public static Message system(String data) {
        return new Message("system", data, LocalDateTime.now().toString());
    }
}
