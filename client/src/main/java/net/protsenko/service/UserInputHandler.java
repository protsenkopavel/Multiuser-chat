package net.protsenko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.protsenko.model.EventType;
import net.protsenko.model.PreRequest;
import net.protsenko.util.Base64Util;
import org.slf4j.LoggerFactory;

public class UserInputHandler {

    static RequestBuilder RB = new RequestBuilder();

    public static String  promptUserCredentials() {
        Config config = ConfigFactory.load("application.conf");

        String username = null;
        String password = null;
        try {
            username = config.getString("application.username");
            password = config.getString("application.password");
        } catch (Exception ignored) {

        }

        var console = System.console();

        while (username == null && password == null) {
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

        return Base64Util.encodeBase64Credentials(username, password);


    }

}
