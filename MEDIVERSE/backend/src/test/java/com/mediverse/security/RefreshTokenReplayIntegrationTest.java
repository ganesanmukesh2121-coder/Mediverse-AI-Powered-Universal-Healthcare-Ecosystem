package com.mediverse.security;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mediverse.dto.RefreshTokenRequestDto;
import com.mediverse.dto.RefreshTokenResponseDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.RefreshToken;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.exception.InvalidCredentialsException;
import com.mediverse.repository.RefreshTokenRepository;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;
import com.mediverse.service.AuthenticationService;
import com.mediverse.service.RefreshTokenService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
class RefreshTokenReplayIntegrationTest {

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

    private final List<String> createdUserIds = new ArrayList<>();

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
        user.setPasswordHash("hashed_password_123");
        user.setFullName("Test User " + emailPrefix);
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        User saved = userRepository.save(user);
        createdUserIds.add(saved.getUserId());
        return saved;
    }

    @Test
    @DisplayName("Normal refresh token rotation works, revoking old token and issuing new one")
    void normalRefreshRotationPreserved() {
        User user = createTestUser("normal_refresh");
        String rawToken1 = refreshTokenService.generateAndPersistRefreshToken(user);

        RefreshTokenResponseDto response = authenticationService.refreshToken(new RefreshTokenRequestDto(rawToken1));

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(rawToken1, response.getRefreshToken());

        // Old token must be revoked
        Optional<RefreshToken> oldTokenOpt = refreshTokenService.findByRawToken(rawToken1);
        assertTrue(oldTokenOpt.isPresent());
        assertNotNull(oldTokenOpt.get().getRevokedAt(), "Rotated token must be marked as revoked");

        // New token must be active
        Optional<RefreshToken> newTokenOpt = refreshTokenService.findByRawToken(response.getRefreshToken());
        assertTrue(newTokenOpt.isPresent());
        assertNull(newTokenOpt.get().getRevokedAt(), "New rotated token must be active");
        assertTrue(refreshTokenService.validateRefreshToken(response.getRefreshToken()));
    }

    @Test
    @DisplayName("Replay of revoked token is rejected with InvalidCredentialsException")
    void reuseOfRevokedTokenIsRejected() {
        User user = createTestUser("revoked_replay");
        String rawToken = refreshTokenService.generateAndPersistRefreshToken(user);
        refreshTokenService.revokeRefreshToken(rawToken);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.refreshToken(new RefreshTokenRequestDto(rawToken))
        );
        assertEquals("Refresh token has been revoked.", ex.getMessage());
    }

    @Test
    @DisplayName("Replay of revoked token revokes all active sibling tokens belonging to that user")
    void replayOfRevokedTokenRevokesAllActiveSiblingTokensForUser() {
        User user = createTestUser("replay_cascade");

        // User logs in on 3 devices: Token 1, Token 2, Token 3
        String token1 = refreshTokenService.generateAndPersistRefreshToken(user);
        String token2 = refreshTokenService.generateAndPersistRefreshToken(user);
        String token3 = refreshTokenService.generateAndPersistRefreshToken(user);

        // Device 1 rotates Token 1 -> Token 1 is revoked, Token 4 is active
        RefreshTokenResponseDto rotationResponse = authenticationService.refreshToken(new RefreshTokenRequestDto(token1));
        String token4 = rotationResponse.getRefreshToken();

        // At this point: Token 1 is revoked; Token 2, 3, 4 are active
        assertTrue(refreshTokenService.validateRefreshToken(token2));
        assertTrue(refreshTokenService.validateRefreshToken(token3));
        assertTrue(refreshTokenService.validateRefreshToken(token4));

        // Attacker replays the revoked Token 1
        assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.refreshToken(new RefreshTokenRequestDto(token1))
        );

        // Verify that Token 2, Token 3, and Token 4 are ALL now revoked in the database
        Optional<RefreshToken> token2Opt = refreshTokenService.findByRawToken(token2);
        assertTrue(token2Opt.isPresent());
        assertNotNull(token2Opt.get().getRevokedAt(), "Sibling token 2 must be revoked");

        Optional<RefreshToken> token3Opt = refreshTokenService.findByRawToken(token3);
        assertTrue(token3Opt.isPresent());
        assertNotNull(token3Opt.get().getRevokedAt(), "Sibling token 3 must be revoked");

        Optional<RefreshToken> token4Opt = refreshTokenService.findByRawToken(token4);
        assertTrue(token4Opt.isPresent());
        assertNotNull(token4Opt.get().getRevokedAt(), "Rotated sibling token 4 must be revoked");

        // The user must have ZERO active refresh tokens
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUser_UserIdAndRevokedAtIsNull(user.getUserId());
        assertTrue(activeTokens.isEmpty(), "User must have zero active refresh tokens after replay attack");
    }

    @Test
    @DisplayName("Replay detection for User A leaves User B's active refresh tokens unaffected")
    void otherUsersRefreshTokensRemainUnaffected() {
        User userA = createTestUser("victim_user_a");
        User userB = createTestUser("innocent_user_b");

        String userAToken1 = refreshTokenService.generateAndPersistRefreshToken(userA);
        String userAToken2 = refreshTokenService.generateAndPersistRefreshToken(userA);

        String userBToken = refreshTokenService.generateAndPersistRefreshToken(userB);

        // Revoke User A's token 1
        refreshTokenService.revokeRefreshToken(userAToken1);

        // Replay User A's revoked token
        assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.refreshToken(new RefreshTokenRequestDto(userAToken1))
        );

        // User A's sibling token 2 is revoked
        Optional<RefreshToken> userAToken2Opt = refreshTokenService.findByRawToken(userAToken2);
        assertTrue(userAToken2Opt.isPresent());
        assertNotNull(userAToken2Opt.get().getRevokedAt(), "User A's active token must be revoked");

        // User B's token remains ACTIVE and valid
        Optional<RefreshToken> userBTokenOpt = refreshTokenService.findByRawToken(userBToken);
        assertTrue(userBTokenOpt.isPresent());
        assertNull(userBTokenOpt.get().getRevokedAt(), "User B's token must NOT be revoked");
        assertTrue(refreshTokenService.validateRefreshToken(userBToken));

        // User B can still perform a normal rotation successfully
        RefreshTokenResponseDto userBResponse = authenticationService.refreshToken(new RefreshTokenRequestDto(userBToken));
        assertNotNull(userBResponse);
        assertNotNull(userBResponse.getAccessToken());
    }

    @Test
    @DisplayName("Sibling token revocation is committed even though refresh request is rejected and rolled back")
    void revocationIsCommittedEvenThoughRefreshRequestIsRejected() {
        User user = createTestUser("tx_commit_proof");

        String tokenA = refreshTokenService.generateAndPersistRefreshToken(user);
        String tokenB = refreshTokenService.generateAndPersistRefreshToken(user);

        // Revoke token A
        refreshTokenService.revokeRefreshToken(tokenA);

        // Precondition: token B is active
        assertTrue(refreshTokenService.validateRefreshToken(tokenB));

        // Replay token A - this causes refreshToken transaction to fail and rollback
        assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.refreshToken(new RefreshTokenRequestDto(tokenA))
        );

        // In a fresh query to the database, verify token B is permanently committed as revoked
        String tokenBHash = refreshTokenService.hashToken(tokenB);
        RefreshToken committedTokenB = refreshTokenRepository.findByTokenHash(tokenBHash)
                .orElseThrow(() -> new AssertionError("Token B must exist in database"));

        assertNotNull(committedTokenB.getRevokedAt(),
                "Sibling token B revocation must be committed to DB even though refresh request was rejected");
    }

    @Test
    @DisplayName("Expired refresh token is rejected without revoking active sibling tokens")
    void expiredRefreshTokenIsRejectedWithoutRevokingActiveTokens() {
        User user = createTestUser("expired_check");

        String activeToken = refreshTokenService.generateAndPersistRefreshToken(user);

        String expiredRawToken = "expired_token_" + UUID.randomUUID();
        String expiredHash = refreshTokenService.hashToken(expiredRawToken);
        RefreshToken expiredToken = new RefreshToken(user, expiredHash, LocalDateTime.now().minusDays(2));
        refreshTokenRepository.save(expiredToken);

        // Presenting expired token throws InvalidCredentialsException with expired message
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.refreshToken(new RefreshTokenRequestDto(expiredRawToken))
        );
        assertEquals("Refresh token has expired.", ex.getMessage());

        // The user's valid active token must remain ACTIVE
        assertTrue(refreshTokenService.validateRefreshToken(activeToken));
    }

    @Test
    @DisplayName("Unknown non-existent refresh token is rejected without affecting any user")
    void unknownRefreshTokenIsRejected() {
        User user = createTestUser("unknown_check");
        String activeToken = refreshTokenService.generateAndPersistRefreshToken(user);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.refreshToken(new RefreshTokenRequestDto("completely_unknown_token_xyz"))
        );
        assertEquals("Invalid refresh token.", ex.getMessage());

        // Active token remains active
        assertTrue(refreshTokenService.validateRefreshToken(activeToken));
    }

    @Test
    @DisplayName("Raw refresh token is never stored directly in the database")
    void rawRefreshTokenIsNeverStoredDirectly() {
        User user = createTestUser("raw_token_check");
        String rawToken = refreshTokenService.generateAndPersistRefreshToken(user);

        // Searching by raw token string in token_hash column yields nothing
        assertTrue(refreshTokenRepository.findByTokenHash(rawToken).isEmpty());

        // Searching by SHA-256 hash yields the token
        String hash = refreshTokenService.hashToken(rawToken);
        assertTrue(refreshTokenRepository.findByTokenHash(hash).isPresent());
    }
}
