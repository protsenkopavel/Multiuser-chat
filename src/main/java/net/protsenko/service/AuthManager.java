package net.protsenko.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.protsenko.model.User;

public class AuthManager {

    private static volatile AuthManager INSTANCE = null;

    private AuthManager() {
    }

    public static AuthManager getINSTANCE() {
        if (INSTANCE == null) {
            synchronized (AuthManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AuthManager();
                }
            }
        }
        return INSTANCE;
    }

    public boolean checkAuth(User user, String psw) {
        try {
            return BCrypt.verifyer().verify(psw.toCharArray(), user.getPswHash()).verified;
        } catch (Exception e) {
            return false;
        }
    }

    public String passwordHash(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

}
