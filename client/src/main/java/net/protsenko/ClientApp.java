package net.protsenko;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.protsenko.model.EventType;
import net.protsenko.model.PreRequest;
import net.protsenko.service.SocketService;
import net.protsenko.util.Base64Util;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ClientApp {
    public static void main(String[] args) throws IOException {
        var log = LoggerFactory.getLogger(ClientApp.class);
        Config config = ConfigFactory.load("application.conf");
        var om = new ObjectMapper();

        var socketServerHost = config.getString("application.server.host");
        var socketServerPort = Integer.parseInt(config.getString("application.server.port"));

        SocketService service = new SocketService();
        try {
            service.startConnection(socketServerHost, socketServerPort);
            log.info("Connected to server");
        } catch (IOException e) {
            log.info("Could not start socket server");
            return;
        }

        String username = null;
        String password = null;
        try {
            username = config.getString("application.username");
            password = config.getString("application.password");
        } catch (Exception ignored) {

        }

        var console = System.console();

        if (username == null || password == null) {
            if (username == null) {
                String userInput = console.readLine("Please enter your name: ");
                while (userInput.isEmpty()) {
                    userInput = console.readLine("Name cannot be empty: ");
                }
                username = userInput;
            }
            String passwordInput = new String(console.readPassword("Please enter your password: "));
            while (passwordInput.isEmpty()) {
                passwordInput = new String(console.readPassword("Password cannot be empty: "));
            }
            password = passwordInput;
        }

        var credentials = Base64Util.encodeBase64Credentials(username, password);
        String message = "";

        var firstRequest = new PreRequest(EventType.valueOf("SIGN_UP"), credentials, message);
        String request = null;
        try {
            request = om.writeValueAsString(firstRequest);
        } catch (JsonProcessingException e) {
            log.error("Bad request", e);
        }

        service.sendMessage(request);
        if (service.receiveMessage() == null) {
            log.info("No message received");
        } else {
            System.out.println(service.receiveMessage());
        }

    }
}
