package net.protsenko.util;

import org.flywaydb.core.internal.util.Pair;

import java.util.Base64;

public class Base64Util {
    public static Pair<String, String> decodeBase64Credentials(String credentials) {
        var splitted = new String(Base64.getDecoder().decode(credentials)).split(":");
        return new Pair.of(splitted[0], splitted[1]);
    }
}
