package com.mediverse.repository;

import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.RefreshToken;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class RefreshTokenTest {

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
        user.setEmail("refreshtoken_test_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash("hashedpassword");
        user.setFullName("Refresh Token User");
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        testUser = userRepository.save(user);
    }

    @Test
    void test1_2_3_EntityMappingAndPrimaryKeyAndUserManyToOne() {
        String tokenHash = "hash_" + UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        RefreshToken token = new RefreshToken(testUser, tokenHash, expiresAt);
        RefreshToken savedToken = refreshTokenRepository.save(token);

        assertNotNull(savedToken.getRefreshTokenId());
        assertEquals(36, savedToken.getRefreshTokenId().length());
        assertNotNull(savedToken.getUser());
        assertEquals(testUser.getUserId(), savedToken.getUser().getUserId());
    }

    @Test
    void test4_5_6_7_TokenHashExpiresAtCreatedAtRevokedAtFields() {
        String tokenHash = "hash_" + UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        RefreshToken token = new RefreshToken(testUser, tokenHash, expiresAt);
        RefreshToken savedToken = refreshTokenRepository.save(token);

        assertEquals(tokenHash, savedToken.getTokenHash());
        assertNotNull(savedToken.getExpiresAt());
        assertNotNull(savedToken.getCreatedAt());
        assertNull(savedToken.getRevokedAt());

        LocalDateTime revokedAt = LocalDateTime.now();
        savedToken.setRevokedAt(revokedAt);
        RefreshToken updatedToken = refreshTokenRepository.save(savedToken);

        assertNotNull(updatedToken.getRevokedAt());
    }

    @Test
    void test8_9_PersistRetrieveAndFindByTokenHash() {
        String tokenHash = "hash_" + UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        RefreshToken token = new RefreshToken(testUser, tokenHash, expiresAt);
        refreshTokenRepository.save(token);

        Optional<RefreshToken> foundToken = refreshTokenRepository.findByTokenHash(tokenHash);
        assertTrue(foundToken.isPresent());
        assertEquals(tokenHash, foundToken.get().getTokenHash());
        assertEquals(testUser.getUserId(), foundToken.get().getUser().getUserId());
    }

    @Test
    void test10_MultipleRefreshTokensCanBelongToOneUser() {
        String hash1 = "hash1_" + UUID.randomUUID();
        String hash2 = "hash2_" + UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        RefreshToken token1 = new RefreshToken(testUser, hash1, expiresAt);
        RefreshToken token2 = new RefreshToken(testUser, hash2, expiresAt);

        refreshTokenRepository.save(token1);
        refreshTokenRepository.save(token2);

        List<RefreshToken> tokens = refreshTokenRepository.findByUser(testUser);
        assertTrue(tokens.size() >= 2);
    }
}
