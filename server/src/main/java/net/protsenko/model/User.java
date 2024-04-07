package net.protsenko.model;

import java.sql.ResultSet;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class User {
    private String name;

    private String pswHash;

    private boolean isOnline;

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", pswHash='" + pswHash + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }

    public User(String name, String pswHash, boolean isOnline) {
        this.name = name;
        this.pswHash = pswHash;
        this.isOnline = isOnline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPswHash() {
        return pswHash;
    }

    public void setPswHash(String pswHash) {
        this.pswHash = pswHash;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public static Function<ResultSet, User> resultSetMapper = resultSet -> {
        try {
            return new User(
                    resultSet.getString("name"),
                    resultSet.getString("pswhash"),
                    resultSet.getBoolean("isOnline"));
        } catch (Exception e) {
            Logger.getLogger("Logger").log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    };
}
