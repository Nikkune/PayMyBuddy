package dev.nikkune.paymybuddy.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    /**
     * Utility class PasswordUtil provides methods to encode passwords and
     * verify if a raw password matches an encoded one.
     */

    @Test
    void testMatchesWithCorrectPassword() {
        // Arrange
        String rawPassword = "password123";
        String encodedPassword = PasswordUtil.encodePassword(rawPassword);

        // Act
        boolean result = PasswordUtil.matches(rawPassword, encodedPassword);

        // Assert
        assertTrue(result, "The raw password should match the encoded password");
    }

    @Test
    void testMatchesWithIncorrectPassword() {
        // Arrange
        String rawPassword = "password123";
        String wrongRawPassword = "wrongpassword";
        String encodedPassword = PasswordUtil.encodePassword(rawPassword);

        // Act
        boolean result = PasswordUtil.matches(wrongRawPassword, encodedPassword);

        // Assert
        assertFalse(result, "The raw password should not match the encoded password if incorrect");
    }

    @Test
    void testMatchesWithNullRawPassword() {
        // Arrange
        String encodedPassword = PasswordUtil.encodePassword("password123");

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> PasswordUtil.matches(null, encodedPassword),
                "Null raw password should throw an IllegalArgumentException");
    }

    @Test
    void testMatchesWithNullEncodedPassword() {
        // Arrange
        String rawPassword = "password123";

        // Act
        boolean result = PasswordUtil.matches(rawPassword, null);

        // Assert
        assertFalse(result, "Any raw password should not match a null encoded password");
    }
}