package net.protsenko.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.protsenko.model.User;

public class AuthManager {

    private final DBQueryExecutor dbQueryExecutor;

    private AuthManager(DBQueryExecutor dbQueryExecutor) {
        this.dbQueryExecutor = dbQueryExecutor;
    }

    public boolean checkAuth(String name, String psw) {

        try {
            User user = dbQueryExecutor.getUser(name);
            if (user != null) {
                return BCrypt.verifyer().verify(psw.toCharArray(), user.getPswHash()).verified;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static AuthManager Instance = null;

    public static AuthManager getInstance() {
        if (Instance == null) {
            Instance = new AuthManager(DBQueryExecutor.getInstance());
        }
        return Instance;
    }

}
