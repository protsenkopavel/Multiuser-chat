package net.protsenko;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.protsenko.model.User;

public class AuthManager {

    private final DBQueryExecutor dbQueryExecutor;

    public AuthManager(DBQueryExecutor dbQueryExecutor) {
        this.dbQueryExecutor = dbQueryExecutor;
    }

    public boolean checkAuth(String name, String psw) {
        User user = null;

        try {
            user = dbQueryExecutor.getUser(name);
        } catch (Exception e) {
            return false;
        }

        return BCrypt.verifyer().verify(psw.toCharArray(), user.getPswHash()).verified;
    }

    public static AuthManager Instance = null;

    public static AuthManager getInstance() {
        if (Instance == null) {
            Instance = new AuthManager(DBQueryExecutor.getInstance());
        }
        return Instance;
    }

}
