package com.mediverse.security;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {

    @Value("${jwt.secret:${JWT_SECRET:}}")
    private String secretKeyString;

    @Value("${jwt.access-token-expiration:3600000}")
    private long accessTokenExpirationMs;

    private SecretKey key;

    public JwtService() {
    }

    public JwtService(String secretKeyString, long accessTokenExpirationMs) {
        this.secretKeyString = secretKeyString;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        init();
    }

    @PostConstruct
    public void init() {
        if (secretKeyString == null || secretKeyString.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret configuration is missing or blank. Set JWT_SECRET environment variable or jwt.secret property.");
        }
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret key must be at least 256 bits (32 bytes) for HS256 signing.");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String userId, String email, String role, String accountStatus, String healthId) {
        return generateAccessToken(userId, email, role, accountStatus, healthId, Collections.emptyList());
    }

    public String generateAccessToken(String userId, String email, String role, String accountStatus, String healthId, Collection<String> permissions) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        var builder = Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("role", role)
                .claim("account_status", accountStatus)
                .claim("permissions", permissions != null ? new ArrayList<>(permissions) : Collections.emptyList())
                .issuedAt(now)
                .expiration(expiryDate);

        boolean isPatient = "PATIENT".equalsIgnoreCase(role) || "ROLE_PATIENT".equalsIgnoreCase(role);
        if (isPatient && healthId != null && !healthId.isBlank()) {
            builder.claim("health_id", healthId);
        }

        return builder.signWith(this.key, Jwts.SIG.HS256).compact();
    }

    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }
            Claims claims = extractAllClaims(token);
            return claims != null && !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractAccountStatus(String token) {
        return extractAllClaims(token).get("account_status", String.class);
    }

    public String extractHealthId(String token) {
        return extractAllClaims(token).get("health_id", String.class);
    }

    public List<String> extractPermissions(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object perms = claims.get("permissions");
            if (perms instanceof List<?> list) {
                List<String> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof String s && !s.isBlank()) {
                        result.add(s.trim());
                    }
                }
                return result;
            }
        } catch (Exception e) {
            // Handled safely on malformed or missing claims
        }
        return Collections.emptyList();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
