package net.protsenko;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.protsenko.model.User;

public class AuthManager {

    private DBQueryExecutor dbQueryExecutor;

    public AuthManager(DBQueryExecutor dbQueryExecutor) {
        this.dbQueryExecutor = dbQueryExecutor;
    }

    public boolean checkAuth(String name, String psw) {
        User user = null;

        try {
            User dbUser = dbQueryExecutor.getUser(name);

            user = dbUser;
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
