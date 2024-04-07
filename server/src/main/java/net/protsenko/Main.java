package net.protsenko;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.protsenko.service.*;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args) throws IOException {

        var log = org.slf4j.LoggerFactory.getLogger(Main.class);

        Config config = ConfigFactory.load("application.conf");

        var dataSourceUrl = config.getString("datasource.url");
        var dataSourceUser = config.getString("datasource.username");
        var dataSourcePassword = config.getString("datasource.password");
        var socketPort = config.getInt("socket.port");

        Flyway.configure()
                .dataSource(config.getString("datasource.url"), config.getString("datasource.username"), config.getString("datasource.password"))
                .load().migrate();

        var om = new ObjectMapper();
        var authManager = new AuthManager();

        var dbExecutor = new DBQueryExecutor(dataSourceUrl, dataSourceUser, dataSourcePassword);

        var eolManager = new EOLManager();

        var messageDispatcher = new MessageDispatcher(eolManager, om);
        var requestHandler = new RequestHandler(messageDispatcher, dbExecutor, authManager, eolManager);

        requestHandler.start();
        messageDispatcher.start();

        SocketService service = new SocketService(dbExecutor, requestHandler, eolManager, messageDispatcher);

        service.start(socketPort);

        service.stop();

//{"eventType": "signIn", "message": "hello", "credentials": "QWRtaW46aGVsbG8="}
    }
}