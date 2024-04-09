package net.protsenko.util;

import ch.qos.logback.core.joran.sanity.Pair;

import java.util.Base64;

public class Base64Util {
    public static String encodeBase64Credentials(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}
