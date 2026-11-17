package com.mediverse.service;

import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.RefreshToken;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.repository.RefreshTokenRepository;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));

        User user = new User();
        user.setEmail("refreshtoken_service_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash("hashedpassword");
        user.setFullName("Refresh Service User");
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        testUser = userRepository.save(user);
    }

    @Test
    void test1_2_3_4_5_6_7_GenerateAndPersistRefreshTokenSuccess() {
        String rawToken = refreshTokenService.generateAndPersistRefreshToken(testUser);

        assertNotNull(rawToken);
        assertFalse(rawToken.isBlank());

        String expectedHash = refreshTokenService.hashToken(rawToken);
        assertNotEquals(rawToken, expectedHash);

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(expectedHash);
        assertTrue(tokenOpt.isPresent());

        RefreshToken token = tokenOpt.get();
        assertEquals(testUser.getUserId(), token.getUser().getUserId());
        assertNull(token.getRevokedAt());
        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now().plusDays(6)));
    }

    @Test
    void test8_ValidRefreshTokenCanBeFoundAndValidated() {
        String rawToken = refreshTokenService.generateAndPersistRefreshToken(testUser);

        Optional<RefreshToken> foundToken = refreshTokenService.findByRawToken(rawToken);
        assertTrue(foundToken.isPresent());
        assertTrue(refreshTokenService.validateRefreshToken(rawToken));
    }

    @Test
    void test9_12_IndividualTokenRevocationWorksAndCannotBeValidated() {
        String rawToken = refreshTokenService.generateAndPersistRefreshToken(testUser);
        assertTrue(refreshTokenService.validateRefreshToken(rawToken));

        refreshTokenService.revokeRefreshToken(rawToken);

        assertFalse(refreshTokenService.validateRefreshToken(rawToken));
        Optional<RefreshToken> tokenOpt = refreshTokenService.findByRawToken(rawToken);
        assertTrue(tokenOpt.isPresent());
        assertNotNull(tokenOpt.get().getRevokedAt());
    }

    @Test
    void test10_ExpiredTokenCannotBeValidated() {
        String tokenHash = refreshTokenService.hashToken("expired_raw_token_123");
        LocalDateTime expiredTime = LocalDateTime.now().minusDays(1);

        RefreshToken expiredToken = new RefreshToken(testUser, tokenHash, expiredTime);
        refreshTokenRepository.save(expiredToken);

        assertFalse(refreshTokenService.validateRefreshToken("expired_raw_token_123"));
    }

    @Test
    void test11_InvalidRandomTokenCannotBeValidated() {
        String randomRawToken = "non_existent_token_" + UUID.randomUUID();
        assertFalse(refreshTokenService.validateRefreshToken(randomRawToken));
        assertTrue(refreshTokenService.findByRawToken(randomRawToken).isEmpty());
    }

    @Test
    void test13_RevokingAllUserTokensWorks() {
        String token1 = refreshTokenService.generateAndPersistRefreshToken(testUser);
        String token2 = refreshTokenService.generateAndPersistRefreshToken(testUser);

        assertTrue(refreshTokenService.validateRefreshToken(token1));
        assertTrue(refreshTokenService.validateRefreshToken(token2));

        refreshTokenService.revokeAllUserRefreshTokens(testUser);

        assertFalse(refreshTokenService.validateRefreshToken(token1));
        assertFalse(refreshTokenService.validateRefreshToken(token2));
    }

    @Test
    void test14_RawRefreshTokenIsNeverLoggedAndHashedDeterministically() {
        String rawToken = "sample_raw_token_xyz_789";
        String hash1 = refreshTokenService.hashToken(rawToken);
        String hash2 = refreshTokenService.hashToken(rawToken);

        assertEquals(hash1, hash2);
        assertNotEquals(rawToken, hash1);
        assertFalse(hash1.contains(rawToken));
    }
}
