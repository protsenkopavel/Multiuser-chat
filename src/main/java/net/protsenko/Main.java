package net.protsenko;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.protsenko.model.EventType;
import net.protsenko.model.Request;
import org.flywaydb.core.Flyway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {

        Config config = ConfigFactory.load("application.conf");
        System.out.println(config);

        Flyway.configure()
                .dataSource(config.getString("datasource.url"), config.getString("datasource.username"), config.getString("datasource.password"))
                .load().migrate();

        DBQueryExecutor.getInstance().getUsers().forEach(usr -> System.out.println(usr.toString()));

        System.out.println(AuthManager.getInstance().checkAuth("Admin", "hello"));

        String s = new ObjectMapper().writeValueAsString(EventType.SEND);

        Request request = new Request(EventType.SEND, "credit", null);

        String ss = new ObjectMapper().writeValueAsString(request);

        System.out.println(ss);

        SocketService service = new SocketService();
        service.start(8080);

        service.stop();


    }
}