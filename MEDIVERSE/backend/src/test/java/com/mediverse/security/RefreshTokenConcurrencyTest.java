package com.mediverse.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
class RefreshTokenConcurrencyTest {

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
        user.setFullName("Concurrency User " + emailPrefix);
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        User saved = userRepository.save(user);
        createdUserIds.add(saved.getUserId());
        return saved;
    }

    @Test
    @DisplayName("Two concurrent requests using the exact same refresh token: exactly one succeeds, other is rejected safely")
    void concurrentRefresh_TwoThreadsSameToken_ExactlyOneSucceeds() throws Exception {
        User user = createTestUser("concurrency_2threads");
        String rawToken = refreshTokenService.generateAndPersistRefreshToken(user);

        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<RefreshTokenResponseDto> successfulResponses = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> capturedExceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all threads to execute at the exact same time
                    RefreshTokenResponseDto response = authenticationService.refreshToken(
                            new RefreshTokenRequestDto(rawToken)
                    );
                    successCount.incrementAndGet();
                    successfulResponses.add(response);
                } catch (Throwable ex) {
                    failureCount.incrementAndGet();
                    capturedExceptions.add(ex);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        startLatch.countDown();

        boolean finishedInTime = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(finishedInTime, "Concurrent refresh tasks did not finish within 10 seconds");

        // Exactly one request must succeed
        assertEquals(1, successCount.get(), "Exactly one concurrent request must succeed");

        // The losing request must be rejected safely
        assertEquals(1, failureCount.get(), "The other concurrent request must be rejected safely");

        // Verify successful response
        RefreshTokenResponseDto success = successfulResponses.get(0);
        assertNotNull(success);
        assertNotNull(success.getAccessToken());
        assertNotNull(success.getRefreshToken());
        assertNotEquals(rawToken, success.getRefreshToken());

        // Verify failure was caused by InvalidCredentialsException
        Throwable failedEx = capturedExceptions.get(0);
        assertTrue(failedEx instanceof InvalidCredentialsException,
                "Expected InvalidCredentialsException but was: " + failedEx.getClass().getName());
        assertEquals("Refresh token has been revoked.", failedEx.getMessage());

        // Verify that the original token is marked revoked in the database
        Optional<RefreshToken> oldTokenOpt = refreshTokenService.findByRawToken(rawToken);
        assertTrue(oldTokenOpt.isPresent());
        assertNotNull(oldTokenOpt.get().getRevokedAt(), "Original token must be revoked");
    }

    @Test
    @DisplayName("Four concurrent requests using the exact same refresh token: exactly one succeeds, three rejected safely")
    void concurrentRefresh_FourThreadsSameToken_ExactlyOneSucceeds() throws Exception {
        User user = createTestUser("concurrency_4threads");
        String rawToken = refreshTokenService.generateAndPersistRefreshToken(user);

        int numberOfThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<RefreshTokenResponseDto> successfulResponses = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> capturedExceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    RefreshTokenResponseDto response = authenticationService.refreshToken(
                            new RefreshTokenRequestDto(rawToken)
                    );
                    successCount.incrementAndGet();
                    successfulResponses.add(response);
                } catch (Throwable ex) {
                    failureCount.incrementAndGet();
                    capturedExceptions.add(ex);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean finishedInTime = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(finishedInTime, "Concurrent tasks did not finish within 10 seconds");

        assertEquals(1, successCount.get(), "Exactly one of 4 concurrent requests must succeed");
        assertEquals(3, failureCount.get(), "The other 3 concurrent requests must be rejected safely");

        for (Throwable t : capturedExceptions) {
            assertTrue(t instanceof InvalidCredentialsException);
            assertEquals("Refresh token has been revoked.", t.getMessage());
        }
    }

    @Test
    @DisplayName("Concurrent refresh requests for different users succeed independently without deadlocks")
    void concurrentRefresh_DifferentTokensDifferentUsers_BothSucceedConcurrently() throws Exception {
        User userA = createTestUser("user_a_concurrent");
        User userB = createTestUser("user_b_concurrent");

        String tokenA = refreshTokenService.generateAndPersistRefreshToken(userA);
        String tokenB = refreshTokenService.generateAndPersistRefreshToken(userB);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        executor.submit(() -> {
            try {
                startLatch.await();
                authenticationService.refreshToken(new RefreshTokenRequestDto(tokenA));
                successCount.incrementAndGet();
            } catch (Throwable t) {
                failureCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                authenticationService.refreshToken(new RefreshTokenRequestDto(tokenB));
                successCount.incrementAndGet();
            } catch (Throwable t) {
                failureCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        startLatch.countDown();

        boolean finishedInTime = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(finishedInTime);

        assertEquals(2, successCount.get(), "Both distinct user refreshes must succeed independently");
        assertEquals(0, failureCount.get(), "No failures or deadlocks for distinct tokens");
    }
}
