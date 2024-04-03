package net.protsenko;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.protsenko.model.EventType;
import net.protsenko.model.Request;
import net.protsenko.service.AuthManager;
import net.protsenko.service.DBQueryExecutor;
import net.protsenko.service.SocketService;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {

        Config config = ConfigFactory.load("application.conf");
        System.out.println(config);

        Flyway.configure()
                .dataSource(config.getString("datasource.url"), config.getString("datasource.username"), config.getString("datasource.password"))
                .load().migrate();

        DBQueryExecutor.getInstance().getUsers().forEach(usr -> System.out.println(usr.toString()));

        System.out.println(AuthManager.getInstance().checkAuth("Admin", "hello"));

/*
        String s = new ObjectMapper().writeValueAsString(EventType.SEND);

        Request request = new Request(EventType.SEND, "credit", null);

        String ss = new ObjectMapper().writeValueAsString(request);

        System.out.println(ss);

        Arrays.stream(EventType.values()).forEach(v ->
        {
            try {
                var s1 = new ObjectMapper().writeValueAsString(v);
                System.out.println(s1);
                var d = new ObjectMapper().readValue(s1, EventType.class);
                System.out.println(d);
            } catch (JsonProcessingException e) {
                System.out.println(e);
            }
        });

        System.out.println(new ObjectMapper().readValue("{\"eventType\": \"send\", \"credentials\": \"QWRtaW46aGVsbG8=\", \"message\": \"hello\"}", Request.class));
*/

        SocketService service = new SocketService();
        service.start(8080);

        service.stop();


    }
}