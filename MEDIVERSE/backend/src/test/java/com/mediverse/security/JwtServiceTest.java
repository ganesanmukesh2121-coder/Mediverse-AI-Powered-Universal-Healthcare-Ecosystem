package com.mediverse.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String TEST_SECRET = "mediverse_secure_test_secret_key_1234567890_32bytes!";
    private static final long EXPIRATION_MS = 3600000;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);
    }

    @Test
    void test1_ValidUserDataProducesNonEmptyJwt() {
        String token = jwtService.generateAccessToken(
                UUID.randomUUID().toString(),
                "patient@mediverse.com",
                "PATIENT",
                "ACTIVE",
                "MV-2026-12345678"
        );

        assertNotNull(token);
        assertFalse(token.trim().isEmpty());
    }

    @Test
    void test2_JwtContainsExpectedSubjectUserId() {
        String userId = UUID.randomUUID().toString();
        String token = jwtService.generateAccessToken(userId, "user@mediverse.com", "PATIENT", "ACTIVE", "MV-2026-11112222");

        assertEquals(userId, jwtService.extractUserId(token));
    }

    @Test
    void test3_JwtContainsEmail() {
        String email = "doctor@mediverse.com";
        String token = jwtService.generateAccessToken(UUID.randomUUID().toString(), email, "DOCTOR", "ACTIVE", null);

        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void test4_JwtContainsRole() {
        String role = "ADMIN";
        String token = jwtService.generateAccessToken(UUID.randomUUID().toString(), "admin@mediverse.com", role, "ACTIVE", null);

        assertEquals(role, jwtService.extractRole(token));
    }

    @Test
    void test5_PatientTokenContainsHealthId() {
        String healthId = "MV-2026-87654321";
        String token = jwtService.generateAccessToken(UUID.randomUUID().toString(), "patient@mediverse.com", "PATIENT", "ACTIVE", healthId);

        assertEquals(healthId, jwtService.extractHealthId(token));
    }

    @Test
    void test6_NonPatientTokenDoesNotContainHealthId() {
        String healthId = "MV-2026-87654321";
        String token = jwtService.generateAccessToken(UUID.randomUUID().toString(), "doctor@mediverse.com", "DOCTOR", "ACTIVE", healthId);

        assertNull(jwtService.extractHealthId(token), "Non-patient tokens must not contain health_id claim");
    }

    @Test
    void test7_JwtContainsAccountStatus() {
        String status = "ACTIVE";
        String token = jwtService.generateAccessToken(UUID.randomUUID().toString(), "lab@mediverse.com", "LAB", status, null);

        assertEquals(status, jwtService.extractAccountStatus(token));
    }

    @Test
    void test8_JwtContainsIssuedAtAndExpiration() {
        String token = jwtService.generateAccessToken(UUID.randomUUID().toString(), "user@mediverse.com", "PATIENT", "ACTIVE", "MV-2026-12345678");

        Claims claims = jwtService.extractAllClaims(token);
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void test9_ValidTokenPassesValidation() {
        String token = jwtService.generateAccessToken(UUID.randomUUID().toString(), "user@mediverse.com", "PATIENT", "ACTIVE", "MV-2026-12345678");

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void test10_TamperedTokenFailsValidation() {
        String token = jwtService.generateAccessToken(UUID.randomUUID().toString(), "user@mediverse.com", "PATIENT", "ACTIVE", "MV-2026-12345678");
        String tamperedToken = token.substring(0, token.length() - 4) + "XXXX";

        assertFalse(jwtService.validateToken(tamperedToken));
    }

    @Test
    void test11_ExpiredTokenFailsValidation() {
        JwtService expiredJwtService = new JwtService(TEST_SECRET, -1000);
        String expiredToken = expiredJwtService.generateAccessToken(UUID.randomUUID().toString(), "expired@mediverse.com", "PATIENT", "ACTIVE", "MV-2026-00000000");

        assertFalse(jwtService.validateToken(expiredToken));
    }

    @Test
    void test12_InvalidMalformedTokenFailsValidation() {
        assertFalse(jwtService.validateToken("invalid.jwt.token"));
        assertFalse(jwtService.validateToken(""));
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    void test13_MissingOrInvalidJwtSecretFailsSafely() {
        // Missing (null) secret
        IllegalStateException exNull = assertThrows(IllegalStateException.class, () -> new JwtService(null, EXPIRATION_MS));
        assertTrue(exNull.getMessage().contains("missing or blank"));

        // Empty secret
        IllegalStateException exEmpty = assertThrows(IllegalStateException.class, () -> new JwtService("", EXPIRATION_MS));
        assertTrue(exEmpty.getMessage().contains("missing or blank"));

        // Blank whitespace secret
        IllegalStateException exBlank = assertThrows(IllegalStateException.class, () -> new JwtService("    ", EXPIRATION_MS));
        assertTrue(exBlank.getMessage().contains("missing or blank"));

        // Short secret (< 256 bits / 32 bytes)
        String shortSecret = "short_secret_under_32_bytes";
        IllegalStateException exShort = assertThrows(IllegalStateException.class, () -> new JwtService(shortSecret, EXPIRATION_MS));
        assertTrue(exShort.getMessage().contains("at least 256 bits"));
        assertFalse(exShort.getMessage().contains(shortSecret), "Secret key value must never be leaked in exception message");
    }

    @Test
    void test14_JwtContainsPermissionsListClaim() {
        List<String> expectedPermissions = List.of("DOCTOR_VIEW_AUTHORIZED_RECORD", "DOCTOR_CREATE_PRESCRIPTION");
        String token = jwtService.generateAccessToken(
                UUID.randomUUID().toString(),
                "doctor@mediverse.com",
                "DOCTOR",
                "ACTIVE",
                null,
                expectedPermissions
        );

        List<String> actualPermissions = jwtService.extractPermissions(token);
        assertEquals(expectedPermissions, actualPermissions);
    }

    @Test
    void test15_TokenWithoutPermissionsExtractsEmptyListSafely() {
        String token = jwtService.generateAccessToken(
                UUID.randomUUID().toString(),
                "patient@mediverse.com",
                "PATIENT",
                "ACTIVE",
                "MV-2026-12345678"
        );

        List<String> extracted = jwtService.extractPermissions(token);
        assertNotNull(extracted);
        assertTrue(extracted.isEmpty());
    }

    @Test
    void test16_MalformedOrNullTokenExtractsEmptyPermissionsSafely() {
        assertTrue(jwtService.extractPermissions(null).isEmpty());
        assertTrue(jwtService.extractPermissions("").isEmpty());
        assertTrue(jwtService.extractPermissions("invalid.jwt.token").isEmpty());
    }
}
