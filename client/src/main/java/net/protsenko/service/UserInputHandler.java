package net.protsenko.service;

import com.typesafe.config.Config;

public class UserInputHandler {

    public static void promptUserCredentials() {
        String user = null;
        String password = null;
//
//        try {
//            user = config.getString("application.username");
//            password = config.getString("application.password");
//        } catch (Exception ignored) {
//
//        }

        var console = System.console();

        if (user == null || password == null) {
            if (user == null) {
                String userInput = console.readLine("Please enter your name:");
                while (userInput.isEmpty()) {
                    userInput = console.readLine("Name cannot be empty:");
                }
                user = userInput;
            }
            String passwordInput = new String(console.readPassword("Please enter your password:"));
            while (passwordInput.isEmpty()) {
                passwordInput = new String(console.readPassword("Password cannot be empty:"));
            }
            password = passwordInput;
        }
    }

}
