package net.protsenko;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.flywaydb.core.Flyway;

public class Main {
    public static void main(String[] args) {

        Config config = ConfigFactory.load("application.conf");
        System.out.println(config);

        Flyway.configure()
                .dataSource(config.getString("datasource.url"), config.getString("datasource.username"), config.getString("datasource.password"))
                .load().migrate();

        DBQueryExecutor.getInstance().getUsers().forEach(usr -> System.out.println(usr.toString()));

        System.out.println(AuthManager.getInstance().checkAuth("Admin", "hello"));
    }
}