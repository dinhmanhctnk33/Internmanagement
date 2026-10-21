package com.example.internmanagement.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SecurityContractTest {
    private static String source(String relative) throws Exception {
        return Files.readString(Path.of("src", "main", "java", "com", "example", "internmanagement").resolve(relative));
    }

    @Test
    void passwordResetAlwaysHashesBeforePersistence() throws Exception {
        String dao = source("dao/UserDAO.java");
        String resetServlet = source("controller/ResetPasswordServlet.java");
        assertTrue(dao.contains("PasswordUtil.hash(newPassword)"));
        assertTrue(resetServlet.contains("PasswordUtil.hash(newPassword)"));
    }

    @Test
    void loginRotatesSessionAndSetsTimeout() throws Exception {
        String login = source("controller/LoginServlet.java");
        assertTrue(login.contains("existingSession.invalidate()"));
        assertTrue(login.contains("request.getSession(true)"));
        assertTrue(login.contains("setMaxInactiveInterval(30 * 60)"));
    }

    @Test
    void globalFilterSetsBaselineBrowserSecurityHeaders() throws Exception {
        String filter = source("filter/AuthenticationFilter.java");
        assertTrue(filter.contains("X-Content-Type-Options"));
        assertTrue(filter.contains("X-Frame-Options"));
        assertTrue(filter.contains("Referrer-Policy"));
        assertTrue(filter.contains("Permissions-Policy"));
    }
}
