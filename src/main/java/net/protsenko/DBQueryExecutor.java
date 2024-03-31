package net.protsenko;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.protsenko.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBQueryExecutor {
    private final String URL;
    private final String USER;
    private final String PASSWORD;

    private final Logger lgr = Logger.getLogger("DBQE");

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
                    lgr.log(Level.SEVERE, e.getMessage(), e);
                }
            }

            return acc;
        } catch (Exception e) {
            lgr.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    public List<User> getUsers() {
        return execute("SELECT * FROM auth_user", User.resultSetMapper);
    }


    public User getUser(String name) {
        return execute("SELECT * FROM auth_user WHERE name = '" + name + "'", User.resultSetMapper).get(0);
    }

    public static DBQueryExecutor Instance = null;

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
