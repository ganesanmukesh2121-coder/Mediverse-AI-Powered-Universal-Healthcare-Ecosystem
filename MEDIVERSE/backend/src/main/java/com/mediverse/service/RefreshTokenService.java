package com.mediverse.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.entity.RefreshToken;
import com.mediverse.entity.User;
import com.mediverse.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom;
    private final long refreshTokenExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-token-expiration:604800000}") long refreshTokenExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.secureRandom = new SecureRandom();
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public String generateAndPersistRefreshToken(User user) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);

        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    public String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Raw token cannot be null or empty.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available.", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        String tokenHash = hashToken(rawToken);
        return refreshTokenRepository.findByTokenHash(tokenHash);
    }

    @Transactional
    public Optional<RefreshToken> findByRawTokenWithLock(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        String tokenHash = hashToken(rawToken);
        return refreshTokenRepository.findByTokenHashWithLock(tokenHash);
    }

    @Transactional(readOnly = true)
    public boolean validateRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }
        Optional<RefreshToken> tokenOpt = findByRawToken(rawToken);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        RefreshToken token = tokenOpt.get();
        return token.getRevokedAt() == null && token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        Optional<RefreshToken> tokenOpt = findByRawToken(rawToken);
        tokenOpt.ifPresent(this::revokeRefreshToken);
    }

    @Transactional
    public void revokeRefreshToken(RefreshToken token) {
        if (token != null && token.getRevokedAt() == null) {
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllUserRefreshTokensInNewTransaction(User user) {
        if (user == null || user.getUserId() == null) {
            return;
        }
        revokeAllUserRefreshTokensByUserId(user.getUserId());
    }

    @Transactional
    public void revokeAllUserRefreshTokens(User user) {
        if (user == null || user.getUserId() == null) {
            return;
        }
        revokeAllUserRefreshTokensByUserId(user.getUserId());
    }

    @Transactional
    public void revokeAllUserRefreshTokensByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        List<RefreshToken> tokens = refreshTokenRepository.findByUser_UserIdAndRevokedAtIsNull(userId);
        if (tokens.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (RefreshToken token : tokens) {
            token.setRevokedAt(now);
        }
        refreshTokenRepository.saveAllAndFlush(tokens);
    }
}
