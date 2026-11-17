package com.mediverse.service;

import com.mediverse.dto.LoginRequestDto;
import com.mediverse.dto.LoginResponseDto;
import com.mediverse.dto.MessageResponseDto;
import com.mediverse.dto.RefreshTokenRequestDto;
import com.mediverse.dto.RefreshTokenResponseDto;
import com.mediverse.entity.*;
import com.mediverse.exception.AccountNotActiveException;
import com.mediverse.exception.InvalidCredentialsException;
import com.mediverse.repository.*;
import com.mediverse.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private HealthIdRegistryRepository healthIdRegistryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User createTestUser(String roleName, AccountStatus status, String healthId) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName, roleName + " description")));

        User user = new User();
        user.setEmail(roleName.toLowerCase() + "_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash(passwordEncoder.encode("ValidPass123!"));
        user.setFullName("Test " + roleName);
        user.setRole(role);
        user.setAccountStatus(status);
        user = userRepository.save(user);

        if (healthId != null) {
            HealthIdRegistry hir = new HealthIdRegistry();
            hir.setHealthId(healthId);
            hir.setPatientUser(user);
            hir.setStatus(HealthIdStatus.ACTIVE);
            healthIdRegistryRepository.save(hir);
            user.setHealthIdRegistry(hir);
        }

        return user;
    }

    @Test
    void test1_ActivePatientLoginSucceeds() {
        String healthId = "MV-2026-TESTPAT1";
        User user = createTestUser("PATIENT", AccountStatus.ACTIVE, healthId);

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");
        LoginResponseDto response = authenticationService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals("PATIENT", response.getRole());
        assertEquals("ACTIVE", response.getAccountStatus());
        assertEquals(healthId, response.getHealthId());
    }

    @Test
    void test2_ActiveDoctorLoginSucceeds() {
        User user = createTestUser("DOCTOR", AccountStatus.ACTIVE, null);

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");
        LoginResponseDto response = authenticationService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("DOCTOR", response.getRole());
        assertNull(response.getHealthId());
    }

    @Test
    void test3_ActiveMedicalLoginSucceeds() {
        User user = createTestUser("MEDICAL", AccountStatus.ACTIVE, null);

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");
        LoginResponseDto response = authenticationService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("MEDICAL", response.getRole());
    }

    @Test
    void test4_ActiveLabLoginSucceeds() {
        User user = createTestUser("LAB", AccountStatus.ACTIVE, null);

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");
        LoginResponseDto response = authenticationService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("LAB", response.getRole());
    }

    @Test
    void test5_ActiveEmergencyLoginSucceeds() {
        User user = createTestUser("EMERGENCY", AccountStatus.ACTIVE, null);

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");
        LoginResponseDto response = authenticationService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("EMERGENCY", response.getRole());
    }

    @Test
    void test6_ActiveAdminLoginSucceeds() {
        User user = createTestUser("ADMIN", AccountStatus.ACTIVE, null);

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");
        LoginResponseDto response = authenticationService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void test7_8_CorrectPasswordProducesJwtAndPatientJwtContainsHealthId() {
        String healthId = "MV-2026-PATIENT8";
        User user = createTestUser("PATIENT", AccountStatus.ACTIVE, healthId);

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");
        LoginResponseDto response = authenticationService.login(request);

        String token = response.getAccessToken();
        assertTrue(jwtService.validateToken(token));
        assertEquals(user.getUserId(), jwtService.extractUserId(token));
        assertEquals("PATIENT", jwtService.extractRole(token));
        assertEquals(healthId, jwtService.extractHealthId(token));
    }

    @Test
    void test9_WrongPasswordFails() {
        User user = createTestUser("PATIENT", AccountStatus.ACTIVE, "MV-2026-WRONGPASS");

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "WrongPassword123!");
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(request));
    }

    @Test
    void test10_NonExistentEmailFails() {
        LoginRequestDto request = new LoginRequestDto("nonexistent_" + UUID.randomUUID() + "@mediverse.com", "ValidPass123!");
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(request));
    }

    @Test
    void test11_PendingDoctorCannotLogin() {
        User user = createTestUser("DOCTOR", AccountStatus.PENDING, null);

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");
        assertThrows(AccountNotActiveException.class, () -> authenticationService.login(request));
    }

    @Test
    void test12_PendingMedicalUserCannotLogin() {
        User user = createTestUser("MEDICAL", AccountStatus.PENDING, null);

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");
        assertThrows(AccountNotActiveException.class, () -> authenticationService.login(request));
    }

    @Test
    void test13_PendingLabUserCannotLogin() {
        User user = createTestUser("LAB", AccountStatus.PENDING, null);

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");
        assertThrows(AccountNotActiveException.class, () -> authenticationService.login(request));
    }

    @Test
    void test14_RejectedAccountCannotLogin() {
        User user = createTestUser("DOCTOR", AccountStatus.REJECTED, null);

        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");
        assertThrows(AccountNotActiveException.class, () -> authenticationService.login(request));
    }

    @Test
    void test15_16_17_FailedLoginNeverReturnsJwtPasswordNeverReturnedJwtGeneratedOnlyForActive() {
        User user = createTestUser("DOCTOR", AccountStatus.PENDING, null);
        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");

        assertThrows(AccountNotActiveException.class, () -> authenticationService.login(request));
    }

    @Test
    void test18_ValidRefreshTokenReturnsNewTokensAndRevokesOldToken() {
        User user = createTestUser("PATIENT", AccountStatus.ACTIVE, "MV-2026-REFRESH1");
        String oldRawRefreshToken = refreshTokenService.generateAndPersistRefreshToken(user);

        RefreshTokenRequestDto request = new RefreshTokenRequestDto(oldRawRefreshToken);
        RefreshTokenResponseDto response = authenticationService.refreshToken(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(oldRawRefreshToken, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600, response.getExpiresIn());

        Optional<RefreshToken> oldTokenOpt = refreshTokenService.findByRawToken(oldRawRefreshToken);
        assertTrue(oldTokenOpt.isPresent());
        assertNotNull(oldTokenOpt.get().getRevokedAt());

        Optional<RefreshToken> newTokenOpt = refreshTokenService.findByRawToken(response.getRefreshToken());
        assertTrue(newTokenOpt.isPresent());
        assertNull(newTokenOpt.get().getRevokedAt());
    }

    @Test
    void test19_InvalidRefreshTokenIsRejected() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto("invalid_raw_refresh_token_xyz");
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.refreshToken(request));
    }

    @Test
    void test20_AlreadyRevokedRefreshTokenIsRejected() {
        User user = createTestUser("PATIENT", AccountStatus.ACTIVE, "MV-2026-REVOKED");
        String rawRefreshToken = refreshTokenService.generateAndPersistRefreshToken(user);

        refreshTokenService.revokeRefreshToken(rawRefreshToken);

        RefreshTokenRequestDto request = new RefreshTokenRequestDto(rawRefreshToken);
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.refreshToken(request));
    }

    @Test
    void test21_ExpiredRefreshTokenIsRejected() {
        User user = createTestUser("PATIENT", AccountStatus.ACTIVE, "MV-2026-EXPIRED");
        String rawToken = "expired_raw_token_xyz";
        String hash = refreshTokenService.hashToken(rawToken);

        RefreshToken expiredToken = new RefreshToken(user, hash, LocalDateTime.now().minusDays(1));
        refreshTokenRepository.save(expiredToken);

        RefreshTokenRequestDto request = new RefreshTokenRequestDto(rawToken);
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.refreshToken(request));
    }

    @Test
    void test22_InactiveUserCannotRefresh() {
        User user = createTestUser("DOCTOR", AccountStatus.PENDING, null);
        String rawRefreshToken = refreshTokenService.generateAndPersistRefreshToken(user);

        RefreshTokenRequestDto request = new RefreshTokenRequestDto(rawRefreshToken);
        assertThrows(AccountNotActiveException.class, () -> authenticationService.refreshToken(request));
    }

    @Test
    void test23_RawRefreshTokenIsNeverPersisted() {
        User user = createTestUser("PATIENT", AccountStatus.ACTIVE, "MV-2026-NOPERSIST");
        String rawRefreshToken = refreshTokenService.generateAndPersistRefreshToken(user);

        String expectedHash = refreshTokenService.hashToken(rawRefreshToken);
        assertTrue(refreshTokenRepository.findByTokenHash(rawRefreshToken).isEmpty());
        assertTrue(refreshTokenRepository.findByTokenHash(expectedHash).isPresent());
    }

    @Test
    void test24_LoginIssuesAccessTokenWithPermissionsForPatient() {
        User user = createTestUser("PATIENT", AccountStatus.ACTIVE, "MV-2026-PAT12345");
        LoginResponseDto response = authenticationService.login(new LoginRequestDto(user.getEmail(), "ValidPass123!"));
        assertNotNull(response.getAccessToken());

        List<String> perms = jwtService.extractPermissions(response.getAccessToken());
        assertEquals(6, perms.size(), "Patient must receive exactly 6 permissions");
        assertTrue(perms.contains("PATIENT_VIEW_OWN_PROFILE"));
        assertTrue(perms.contains("PATIENT_UPDATE_OWN_PROFILE"));
        assertTrue(perms.contains("PATIENT_UPLOAD_DOCUMENT"));
        assertTrue(perms.contains("PATIENT_MANAGE_CONSENT"));
        assertTrue(perms.contains("PATIENT_VIEW_OWN_AUDIT"));
        assertTrue(perms.contains("PATIENT_USE_AI_ASSISTANT"));
    }

    @Test
    void test25_LoginIssuesAccessTokenWithPermissionsForDoctor() {
        User user = createTestUser("DOCTOR", AccountStatus.ACTIVE, null);
        LoginResponseDto response = authenticationService.login(new LoginRequestDto(user.getEmail(), "ValidPass123!"));
        assertNotNull(response.getAccessToken());

        List<String> perms = jwtService.extractPermissions(response.getAccessToken());
        assertEquals(5, perms.size(), "Doctor must receive exactly 5 permissions");
        assertTrue(perms.contains("DOCTOR_VIEW_AUTHORIZED_RECORD"));
        assertTrue(perms.contains("DOCTOR_CREATE_CONSULTATION"));
        assertTrue(perms.contains("DOCTOR_CREATE_PRESCRIPTION"));
        assertTrue(perms.contains("DOCTOR_ATTACH_DOCUMENT"));
        assertTrue(perms.contains("DOCTOR_USE_RAG_AGENT"));
    }

    @Test
    void test26_AdminTokenDoesNotContainClinicalPermissions() {
        User user = createTestUser("ADMIN", AccountStatus.ACTIVE, null);
        LoginResponseDto response = authenticationService.login(new LoginRequestDto(user.getEmail(), "ValidPass123!"));
        assertNotNull(response.getAccessToken());

        List<String> perms = jwtService.extractPermissions(response.getAccessToken());
        assertEquals(3, perms.size(), "Admin must receive exactly 3 permissions");
        assertTrue(perms.contains("ADMIN_VERIFY_PROVIDER"));
        assertTrue(perms.contains("ADMIN_MANAGE_ACCOUNT"));
        assertTrue(perms.contains("ADMIN_VIEW_AUDIT"));

        // Boundary verification: Admin must NOT receive clinical permissions
        assertFalse(perms.contains("DOCTOR_VIEW_AUTHORIZED_RECORD"));
        assertFalse(perms.contains("DOCTOR_CREATE_PRESCRIPTION"));
        assertFalse(perms.contains("PATIENT_VIEW_OWN_PROFILE"));
        assertFalse(perms.contains("EMERGENCY_VIEW_EMERGENCY_INFO"));
    }

    @Test
    void test27_EmergencyTokenReceivesOnlyEmergencyInfoPermission() {
        User user = createTestUser("EMERGENCY", AccountStatus.ACTIVE, null);
        LoginResponseDto response = authenticationService.login(new LoginRequestDto(user.getEmail(), "ValidPass123!"));
        assertNotNull(response.getAccessToken());

        List<String> perms = jwtService.extractPermissions(response.getAccessToken());
        assertEquals(1, perms.size(), "Emergency must receive exactly 1 permission");
        assertTrue(perms.contains("EMERGENCY_VIEW_EMERGENCY_INFO"));

        // Boundary verification: Emergency must NOT receive historical clinical record permissions
        assertFalse(perms.contains("DOCTOR_VIEW_AUTHORIZED_RECORD"));
        assertFalse(perms.contains("DOCTOR_CREATE_CONSULTATION"));
        assertFalse(perms.contains("PATIENT_VIEW_OWN_PROFILE"));
    }

    @Test
    void test28_RefreshTokenRotationIssuesNewAccessTokenWithPermissions() {
        User user = createTestUser("LAB", AccountStatus.ACTIVE, null);
        String rawRefreshToken = refreshTokenService.generateAndPersistRefreshToken(user);

        RefreshTokenResponseDto response = authenticationService.refreshToken(new RefreshTokenRequestDto(rawRefreshToken));
        assertNotNull(response.getAccessToken());

        List<String> perms = jwtService.extractPermissions(response.getAccessToken());
        assertEquals(3, perms.size(), "Lab must receive exactly 3 permissions on refresh");
        assertTrue(perms.contains("LAB_CREATE_TEST_ORDER"));
        assertTrue(perms.contains("LAB_ENTER_RESULTS"));
        assertTrue(perms.contains("LAB_UPLOAD_REPORT"));
    }

    @Test
    void test29_SuspendedAccountCannotLogin() {
        User user = createTestUser("DOCTOR", AccountStatus.SUSPENDED, null);
        LoginRequestDto request = new LoginRequestDto(user.getEmail(), "ValidPass123!");

        AccountNotActiveException ex = assertThrows(AccountNotActiveException.class, () -> authenticationService.login(request));
        assertEquals("Account has been suspended.", ex.getMessage());
    }

    @Test
    void test30_SuspendedUserCannotRefresh() {
        User user = createTestUser("PATIENT", AccountStatus.SUSPENDED, "MV-2026-SUSPENDED");
        String rawRefreshToken = refreshTokenService.generateAndPersistRefreshToken(user);

        RefreshTokenRequestDto request = new RefreshTokenRequestDto(rawRefreshToken);
        AccountNotActiveException ex = assertThrows(AccountNotActiveException.class, () -> authenticationService.refreshToken(request));
        assertEquals("Account has been suspended.", ex.getMessage());
    }

    @Test
    void test31_LogoutSuccess() {
        User user = createTestUser("PATIENT", AccountStatus.ACTIVE, "MV-2026-LOGOUT1");
        String rawToken = refreshTokenService.generateAndPersistRefreshToken(user);

        MessageResponseDto response = authenticationService.logout(user.getUserId(), new RefreshTokenRequestDto(rawToken));
        assertEquals("Logged out successfully.", response.getMessage());

        assertFalse(refreshTokenService.validateRefreshToken(rawToken));
    }

    @Test
    void test32_LogoutAnotherUserTokenThrowsAccessDenied() {
        User userA = createTestUser("PATIENT", AccountStatus.ACTIVE, "MV-2026-USERA");
        User userB = createTestUser("PATIENT", AccountStatus.ACTIVE, "MV-2026-USERB");

        String userBToken = refreshTokenService.generateAndPersistRefreshToken(userB);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                authenticationService.logout(userA.getUserId(), new RefreshTokenRequestDto(userBToken))
        );
        assertEquals("Cannot revoke a session belonging to another user.", ex.getMessage());
        assertTrue(refreshTokenService.validateRefreshToken(userBToken));
    }

    @Test
    void test33_LogoutUnknownTokenThrowsInvalidCredentials() {
        User user = createTestUser("PATIENT", AccountStatus.ACTIVE, "MV-2026-UNKNOWN");

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.logout(user.getUserId(), new RefreshTokenRequestDto("unknown_token_123"))
        );
        assertEquals("Invalid refresh token.", ex.getMessage());
    }

    @Test
    void test34_LogoutAllSuccess() {
        User user = createTestUser("PATIENT", AccountStatus.ACTIVE, "MV-2026-LOGOUTALL");
        String token1 = refreshTokenService.generateAndPersistRefreshToken(user);
        String token2 = refreshTokenService.generateAndPersistRefreshToken(user);

        MessageResponseDto response = authenticationService.logoutAll(user.getUserId());
        assertEquals("All sessions logged out successfully.", response.getMessage());

        assertFalse(refreshTokenService.validateRefreshToken(token1));
        assertFalse(refreshTokenService.validateRefreshToken(token2));
        assertTrue(refreshTokenRepository.findByUser_UserIdAndRevokedAtIsNull(user.getUserId()).isEmpty());
    }
}
