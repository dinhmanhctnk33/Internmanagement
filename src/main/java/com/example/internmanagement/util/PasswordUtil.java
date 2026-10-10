package com.example.internmanagement.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() { }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean matches(String password, String stored) {
        if (password == null || stored == null) return false;
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            try { return BCrypt.checkpw(password, stored); }
            catch (IllegalArgumentException ignored) { return false; }
        }
        return password.equals(stored);
    }

    public static boolean needsUpgrade(String stored) {
        return stored != null && !stored.startsWith("$2a$") && !stored.startsWith("$2b$") && !stored.startsWith("$2y$");
    }
}
