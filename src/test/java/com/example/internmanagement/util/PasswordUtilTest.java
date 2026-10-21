package com.example.internmanagement.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {
    @Test
    void hashUsesBcryptAndMatchesOriginalPassword() {
        String hash = PasswordUtil.hash("MatKhau!123");
        assertTrue(hash.startsWith("$2"));
        assertNotEquals("MatKhau!123", hash);
        assertTrue(PasswordUtil.matches("MatKhau!123", hash));
        assertFalse(PasswordUtil.matches("sai-mat-khau", hash));
    }

    @Test
    void legacyPlainTextPasswordCanLoginButRequiresUpgrade() {
        assertTrue(PasswordUtil.matches("123456", "123456"));
        assertTrue(PasswordUtil.needsUpgrade("123456"));
    }

    @Test
    void malformedAndNullHashesAreRejectedSafely() {
        assertFalse(PasswordUtil.matches(null, "abc"));
        assertFalse(PasswordUtil.matches("abc", null));
        assertFalse(PasswordUtil.matches("abc", "$2a$broken"));
    }
}
