package net.protsenko;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.protsenko.service.SocketService;
import net.protsenko.service.UserInputHandler;

import java.io.IOException;

public class ClientApp {
    public static void main(String[] args) throws IOException {
        Config config = ConfigFactory.load("application.conf");

        var socketServerHost = config.getString("application.server.host");
        var socketServerPort = Integer.parseInt(config.getString("application.server.port"));

        var userInputHandler = new UserInputHandler();

        SocketService service = new SocketService(userInputHandler);
        service.start(socketServerHost, socketServerPort);
        service.stop();
    }
}
