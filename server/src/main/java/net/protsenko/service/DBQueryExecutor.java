package net.protsenko.service;

import net.protsenko.model.Message;
import net.protsenko.model.User;

import java.sql.*;
import java.util.List;
import java.util.function.Function;

public class DBQueryExecutor {
    private final String URL;
    private final String USER;
    private final String PASSWORD;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DBQueryExecutor.class);

    public DBQueryExecutor(String URL, String USER, String PASSWORD) {
        this.URL = URL;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
    }

    private <T> List<T> execute(String query, Function<ResultSet, T> mapper) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            List<T> acc = new java.util.ArrayList<>(List.of());
            while (rs.next()) {
                try {
                    T res = mapper.apply(rs);
                    if (res != null) acc.add(res);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            return acc;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private void executeUpdate(String query, Function<PreparedStatement, PreparedStatement> updater) {
        PreparedStatement stmt = null;
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
            stmt = con.prepareStatement(query);
            stmt = updater.apply(stmt);
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public List<User> getUsers() {
        return execute("SELECT * FROM auth_user", User.resultSetMapper);
    }


    public User getUser(String name) {
        try {
            return execute("SELECT * FROM auth_user WHERE name = '" + name + "'", User.resultSetMapper).get(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void insertUser(User user) {
        executeUpdate("INSERT INTO auth_user(name, pswhash) values(?, ?)", stmt -> {
            try {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getPswHash());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return stmt;
        });
    }

    public void saveMessage(Message message) {
        executeUpdate("INSERT INTO history(sender, data, send_date) VALUES (?, ?, ?::timestamp)", stmt -> {
                    try {
                        stmt.setString(1, message.getFrom());
                        stmt.setString(2, message.getData());
                        stmt.setString(3, message.getDate());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    return stmt;
                }
        );
    }

    public Integer onlineUsers() {
        var statement = "SELECT count(*) FROM auth_user WHERE isOnline = true";
        return execute(statement, resultSet -> {
            try {
                return resultSet.getInt(1);
            } catch (Exception e) {
                return 1;
            }
        }).get(0);
    }

    public void setOffline(String username) {
        executeUpdate("UPDATE auth_user SET isOnline = false WHERE name = ?", stmt -> {
                    try {
                        stmt.setString(1, username);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    return stmt;
                }
        );
    }

    public void setOnline(String username) {
        executeUpdate("UPDATE auth_user SET isOnline = true WHERE name = ?", stmt -> {
                    try {
                        stmt.setString(1, username);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    return stmt;
                }
        );
    }

    public List<Message> lastMessages(Integer n) {
        if (n != null && n > 0) n = 10;
        var statement = "SELECT sender, data, send_date FROM history ORDER BY id DESC" + " LIMIT " + n;
        return execute(statement, resultSet -> {
            try {
                return new Message(resultSet.getString(1), resultSet.getString(2))
                        .withDate(resultSet.getString(3));
            } catch (Exception e) {
                return null;
            }
        });
    }
}
