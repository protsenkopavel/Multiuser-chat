package net.protsenko;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.protsenko.service.SocketService;
import net.protsenko.service.UserInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

public class ClientApp {
    public static void main(String[] args) throws IOException {
        Logger log = LoggerFactory.getLogger(ClientApp.class);

        Config config = ConfigFactory.load("application.conf");

        var socketServerHost = config.getString("application.server.host");
        var socketServerPort = Integer.parseInt(config.getString("application.server.port"));

        var userInputHandler = new UserInputHandler();

        SocketService service = new SocketService(userInputHandler);
        log.info("***Service starting***");
        service.start(socketServerHost, socketServerPort);
        service.stop();
        System.out.println("Stop application");
    }
}
