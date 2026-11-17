package com.mediverse.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        PasswordEncoderConfig config = new PasswordEncoderConfig();
        passwordEncoder = config.passwordEncoder();
    }

    @Test
    void testPasswordEncoderBeanInstance() {
        assertNotNull(passwordEncoder, "PasswordEncoder bean should not be null");
    }

    @Test
    void testEncodeAndMatches() {
        String rawPassword = "MediverseSecurePass123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should not equal raw password");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), "PasswordEncoder.matches should return true for valid raw password");
    }

    @Test
    void testSaltRandomization() {
        String rawPassword = "MediverseSecurePass123!";
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        assertNotEquals(encoded1, encoded2, "Two BCrypt encodings of the same password should not be identical due to random salting");
        assertTrue(passwordEncoder.matches(rawPassword, encoded1));
        assertTrue(passwordEncoder.matches(rawPassword, encoded2));
    }
}
