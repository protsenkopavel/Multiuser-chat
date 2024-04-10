package net.protsenko.util;

import java.util.Base64;

public class Base64Util {

    public static String encodeBase64Credentials(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public static Pair<String, String> decodeBase64Credentials(String credentials) {
        var splitted = new String(Base64.getDecoder().decode(credentials)).split(":");
        return new Pair<>(splitted[0], splitted[1]);
    }
}
