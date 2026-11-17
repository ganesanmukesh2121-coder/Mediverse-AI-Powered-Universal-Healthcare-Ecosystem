package com.mediverse.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediverse.dto.RefreshTokenRequestDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.RefreshToken;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.repository.RefreshTokenRepository;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;
import com.mediverse.service.RefreshTokenService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
class LogoutSessionManagementIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final List<String> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        for (String userId : createdUserIds) {
            userRepository.findById(userId).ifPresent(user -> {
                List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);
                refreshTokenRepository.deleteAll(tokens);
                userRepository.delete(user);
            });
        }
        createdUserIds.clear();
    }

    private User createTestUser(String emailPrefix) {
        Role role = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));

        User user = new User();
        user.setEmail(emailPrefix + "_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash("hashed_pwd_123");
        user.setFullName("User " + emailPrefix);
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        User saved = userRepository.save(user);
        createdUserIds.add(saved.getUserId());
        return saved;
    }

    private String generateAccessTokenForUser(User user) {
        return jwtService.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole().getRoleName(),
                user.getAccountStatus().name(),
                null,
                List.of("PATIENT_VIEW_OWN_PROFILE")
        );
    }

    @Test
    @DisplayName("1. Successful single-session logout revokes the specified refresh token and returns 200")
    void test1_SuccessfulSingleSessionLogout() throws Exception {
        User user = createTestUser("logout_user1");
        String rawToken = refreshTokenService.generateAndPersistRefreshToken(user);
        String accessToken = generateAccessTokenForUser(user);

        RefreshTokenRequestDto request = new RefreshTokenRequestDto(rawToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully."));

        // Verify in database that the token is now revoked
        Optional<RefreshToken> tokenOpt = refreshTokenService.findByRawToken(rawToken);
        assertTrue(tokenOpt.isPresent());
        assertNotNull(tokenOpt.get().getRevokedAt(), "Token must be marked as revoked after logout");
        assertFalse(refreshTokenService.validateRefreshToken(rawToken));
    }

    @Test
    @DisplayName("2. Single-session logout rejects another user's refresh token with HTTP 403 Forbidden")
    void test2_SingleSessionLogoutRejectsAnotherUserRefreshToken_With403() throws Exception {
        User userA = createTestUser("user_a");
        User userB = createTestUser("user_b");

        String userBToken = refreshTokenService.generateAndPersistRefreshToken(userB);
        String userAAccessToken = generateAccessTokenForUser(userA);

        // User A attempts to revoke User B's refresh token
        RefreshTokenRequestDto request = new RefreshTokenRequestDto(userBToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied: Cannot revoke a session belonging to another user."));

        // User B's token must NOT be revoked
        Optional<RefreshToken> userBTokenOpt = refreshTokenService.findByRawToken(userBToken);
        assertTrue(userBTokenOpt.isPresent());
        assertNull(userBTokenOpt.get().getRevokedAt(), "User B's token must not be affected by User A");
        assertTrue(refreshTokenService.validateRefreshToken(userBToken));
    }

    @Test
    @DisplayName("3. Single-session logout rejects unknown refresh tokens with HTTP 401 Unauthorized")
    void test3_SingleSessionLogoutRejectsUnknownRefreshToken_With401() throws Exception {
        User user = createTestUser("logout_unknown");
        String accessToken = generateAccessTokenForUser(user);

        RefreshTokenRequestDto request = new RefreshTokenRequestDto("completely_unknown_token_xyz");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid refresh token."));
    }

    @Test
    @DisplayName("4. Single-session logout does not revoke other active sessions of the same user")
    void test4_SingleSessionLogoutDoesNotRevokeOtherSessions() throws Exception {
        User user = createTestUser("multi_session_user");

        String token1 = refreshTokenService.generateAndPersistRefreshToken(user);
        String token2 = refreshTokenService.generateAndPersistRefreshToken(user);
        String token3 = refreshTokenService.generateAndPersistRefreshToken(user);

        String accessToken = generateAccessTokenForUser(user);

        // User logs out only token 1 (e.g. from laptop)
        RefreshTokenRequestDto request = new RefreshTokenRequestDto(token1);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully."));

        // Token 1 is revoked
        assertFalse(refreshTokenService.validateRefreshToken(token1));

        // Tokens 2 and 3 must remain active
        assertTrue(refreshTokenService.validateRefreshToken(token2));
        assertTrue(refreshTokenService.validateRefreshToken(token3));

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUser_UserIdAndRevokedAtIsNull(user.getUserId());
        assertEquals(2, activeTokens.size(), "User must still have 2 active refresh token sessions");
    }

    @Test
    @DisplayName("5. Successful logout-all revokes every active refresh token belonging to the authenticated user")
    void test5_SuccessfulLogoutAll() throws Exception {
        User user = createTestUser("logout_all_user");

        String token1 = refreshTokenService.generateAndPersistRefreshToken(user);
        String token2 = refreshTokenService.generateAndPersistRefreshToken(user);
        String token3 = refreshTokenService.generateAndPersistRefreshToken(user);

        String accessToken = generateAccessTokenForUser(user);

        mockMvc.perform(post("/api/v1/auth/logout-all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All sessions logged out successfully."));

        // All 3 tokens must now be revoked
        assertFalse(refreshTokenService.validateRefreshToken(token1));
        assertFalse(refreshTokenService.validateRefreshToken(token2));
        assertFalse(refreshTokenService.validateRefreshToken(token3));

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUser_UserIdAndRevokedAtIsNull(user.getUserId());
        assertTrue(activeTokens.isEmpty(), "User must have zero active refresh tokens after logout-all");
    }

    @Test
    @DisplayName("6. Logout-all affects only the authenticated user's sessions and leaves other users untouched")
    void test6_LogoutAllAffectsOnlyAuthenticatedUserSessions() throws Exception {
        User userA = createTestUser("logout_all_a");
        User userB = createTestUser("logout_all_b");

        String userAToken1 = refreshTokenService.generateAndPersistRefreshToken(userA);
        String userAToken2 = refreshTokenService.generateAndPersistRefreshToken(userA);
        String userBToken = refreshTokenService.generateAndPersistRefreshToken(userB);

        String userAAccessToken = generateAccessTokenForUser(userA);

        // User A performs logout-all
        mockMvc.perform(post("/api/v1/auth/logout-all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All sessions logged out successfully."));

        // User A's tokens are all revoked
        assertFalse(refreshTokenService.validateRefreshToken(userAToken1));
        assertFalse(refreshTokenService.validateRefreshToken(userAToken2));
        assertTrue(refreshTokenRepository.findByUser_UserIdAndRevokedAtIsNull(userA.getUserId()).isEmpty());

        // User B's token remains ACTIVE
        assertTrue(refreshTokenService.validateRefreshToken(userBToken));
        Optional<RefreshToken> userBTokenOpt = refreshTokenService.findByRawToken(userBToken);
        assertTrue(userBTokenOpt.isPresent());
        assertNull(userBTokenOpt.get().getRevokedAt(), "User B's session must remain active");
    }

    @Test
    @DisplayName("7. Unauthenticated logout request without Bearer token returns HTTP 401")
    void test7_UnauthenticatedLogoutRequestsReturn401() throws Exception {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto("any_refresh_token_xyz");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("8. Unauthenticated logout-all request without Bearer token returns HTTP 401")
    void test8_UnauthenticatedLogoutAllRequestsReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout-all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("9. Refresh token revoked through logout cannot be reused to rotate tokens")
    void test9_RevokedRefreshTokenCannotBeReused() throws Exception {
        User user = createTestUser("revocation_reuse_test");
        String rawToken = refreshTokenService.generateAndPersistRefreshToken(user);
        String accessToken = generateAccessTokenForUser(user);

        // 1. Log out the token
        RefreshTokenRequestDto logoutRequest = new RefreshTokenRequestDto(rawToken);
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());

        // 2. Attempt to use the logged-out token to refresh
        RefreshTokenRequestDto refreshRequest = new RefreshTokenRequestDto(rawToken);
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token has been revoked."));
    }
}
