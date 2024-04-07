package net.protsenko.service;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.protsenko.model.Message;
import net.protsenko.model.User;
import org.flywaydb.core.internal.util.Pair;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBQueryExecutor {
    private final String URL;
    private final String USER;
    private final String PASSWORD;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EOLManager.class);

    private DBQueryExecutor(String URL, String USER, String PASSWORD) {
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
        return execute("SELECT * FROM auth_user WHERE name = '" + name + "'", User.resultSetMapper).get(0);
    }

    public void insertUser(User user) {
        var statement = "INSERT INTO auth_user(name, pswhash) values('" + user.getName() + "', '" + user.getPswHash() + "')";
        execute(statement, resultSet -> "");
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

    public void saveMessage(String from, String data) {
        executeUpdate("INSERT INTO history(sender, data, send_date) VALUES (?, ?, ?::timestamp)", stmt -> {
                    try {
                        stmt.setString(1, from);
                        stmt.setString(2, data);
                        stmt.setString(3, LocalDateTime.now().toString());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    return stmt;
                }
        );

    }

    private static DBQueryExecutor Instance = null;

    public static DBQueryExecutor getInstance() {
        if (Instance == null) {
            Config config = ConfigFactory.load("application.conf");
            Instance = new DBQueryExecutor(
                    config.getString("datasource.url"),
                    config.getString("datasource.username"),
                    config.getString("datasource.password"));
        }
        return Instance;
    }

}
