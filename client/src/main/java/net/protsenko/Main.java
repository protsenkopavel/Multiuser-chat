package net.protsenko;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Config config = ConfigFactory.load("application.conf");

        var socketServerHost = config.getString("application.server.host");
        var socketServerPort = Integer.parseInt(config.getString("application.server.port"));

        String user = null;
        String password = null;
        try {
            user = config.getString("application.username");
            password = config.getString("application.password");
        } catch (Exception ignored) {

        }

        var console = System.console();

        if (user == null || password == null) {
            if (user == null) {
                String userInput = console.readLine("Okay, throw me some numbers:");
                while (userInput.isEmpty()) {
                    userInput = console.readLine("Number 9 is unavailable, try another burger:");
                }
                user = userInput;
            }
            String passwordInput = new String(console.readPassword("Okay, gimme your password:"));
            while (passwordInput.isEmpty()) {
                passwordInput = new String(console.readPassword("Вы где колоду заряжаете? Распечатай нормальную колоду:"));
            }
            password = passwordInput;
        }


    }
}
