package com.mediverse.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.LoginRequestDto;
import com.mediverse.dto.LoginResponseDto;
import com.mediverse.dto.MessageResponseDto;
import com.mediverse.dto.RefreshTokenRequestDto;
import com.mediverse.dto.RefreshTokenResponseDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.HealthIdRegistry;
import com.mediverse.entity.Permission;
import com.mediverse.entity.RefreshToken;
import com.mediverse.entity.Role;
import com.mediverse.entity.RolePermission;
import com.mediverse.entity.User;
import com.mediverse.exception.AccountNotActiveException;
import com.mediverse.exception.InvalidCredentialsException;
import com.mediverse.repository.RolePermissionRepository;
import com.mediverse.repository.UserRepository;
import com.mediverse.security.JwtService;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RolePermissionRepository rolePermissionRepository;
    private final long accessTokenExpirationMs;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            RolePermissionRepository rolePermissionRepository,
            @Value("${jwt.access-token-expiration:3600000}") long accessTokenExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.rolePermissionRepository = rolePermissionRepository;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            if (user.getAccountStatus() == AccountStatus.PENDING) {
                throw new AccountNotActiveException("Account is pending verification.");
            } else if (user.getAccountStatus() == AccountStatus.REJECTED) {
                throw new AccountNotActiveException("Account has been rejected.");
            } else if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
                throw new AccountNotActiveException("Account has been suspended.");
            } else {
                throw new AccountNotActiveException("Account is not active.");
            }
        }

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "PATIENT";

        String healthId = null;
        if ("PATIENT".equalsIgnoreCase(roleName) || "ROLE_PATIENT".equalsIgnoreCase(roleName)) {
            HealthIdRegistry hir = user.getHealthIdRegistry();
            if (hir != null) {
                healthId = hir.getHealthId();
            }
        }

        List<String> permissions = resolvePermissionsForUser(user);

        String accessToken = jwtService.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                roleName,
                user.getAccountStatus().name(),
                healthId,
                permissions
        );

        String refreshToken = refreshTokenService.generateAndPersistRefreshToken(user);

        return new LoginResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpirationMs / 1000,
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                roleName,
                user.getAccountStatus().name(),
                healthId,
                "Login successful."
        );
    }

    @Transactional
    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new InvalidCredentialsException("Invalid or missing refresh token.");
        }

        String rawRefreshToken = request.getRefreshToken();

        // 1. Acquire pessimistic write lock on the target refresh token record
        Optional<RefreshToken> tokenOpt = refreshTokenService.findByRawTokenWithLock(rawRefreshToken);
        if (tokenOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid refresh token.");
        }

        RefreshToken oldToken = tokenOpt.get();
        User user = oldToken.getUser();

        if (oldToken.getRevokedAt() != null) {
            // Replay attack detected: revoked refresh token presented again.
            // Revoke all active refresh tokens for this user in an independent transaction
            // so that sibling token revocation persists even when this request rolls back.
            if (user != null) {
                refreshTokenService.revokeAllUserRefreshTokensInNewTransaction(user);
            }
            throw new InvalidCredentialsException("Refresh token has been revoked.");
        }

        if (oldToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Refresh token has expired.");
        }

        if (user == null || user.getAccountStatus() != AccountStatus.ACTIVE) {
            if (user != null && user.getAccountStatus() == AccountStatus.PENDING) {
                throw new AccountNotActiveException("Account is pending verification.");
            } else if (user != null && user.getAccountStatus() == AccountStatus.REJECTED) {
                throw new AccountNotActiveException("Account has been rejected.");
            } else if (user != null && user.getAccountStatus() == AccountStatus.SUSPENDED) {
                throw new AccountNotActiveException("Account has been suspended.");
            } else {
                throw new AccountNotActiveException("Account is not active.");
            }
        }

        // 2. Revoke the old refresh token record (Rotation on locked entity)
        refreshTokenService.revokeRefreshToken(oldToken);

        // 2. Generate a new refresh token (Persisting only its hash)
        String newRawRefreshToken = refreshTokenService.generateAndPersistRefreshToken(user);

        // 3. Generate a new 60-minute JWT access token with updated permissions
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "PATIENT";
        String healthId = null;
        if ("PATIENT".equalsIgnoreCase(roleName) || "ROLE_PATIENT".equalsIgnoreCase(roleName)) {
            HealthIdRegistry hir = user.getHealthIdRegistry();
            if (hir != null) {
                healthId = hir.getHealthId();
            }
        }

        List<String> permissions = resolvePermissionsForUser(user);

        String newAccessToken = jwtService.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                roleName,
                user.getAccountStatus().name(),
                healthId,
                permissions
        );

        return new RefreshTokenResponseDto(
                newAccessToken,
                newRawRefreshToken,
                "Bearer",
                accessTokenExpirationMs / 1000
        );
    }

    private List<String> resolvePermissionsForUser(User user) {
        if (user == null || user.getRole() == null) {
            return Collections.emptyList();
        }
        Role role = user.getRole();
        List<RolePermission> rolePermissions;
        if (role.getRoleId() != null) {
            rolePermissions = rolePermissionRepository.findByRole_RoleId(role.getRoleId());
        } else {
            rolePermissions = rolePermissionRepository.findByRole_RoleName(role.getRoleName());
        }
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }
        return rolePermissions.stream()
                .map(RolePermission::getPermission)
                .filter(Objects::nonNull)
                .map(Permission::getPermissionKey)
                .filter(key -> key != null && !key.isBlank())
                .distinct()
                .toList();
    }

    @Transactional
    public MessageResponseDto logout(String authenticatedUserId, RefreshTokenRequestDto request) {
        if (authenticatedUserId == null || authenticatedUserId.isBlank()) {
            throw new InvalidCredentialsException("Unauthenticated user.");
        }
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new InvalidCredentialsException("Invalid or missing refresh token.");
        }

        String rawRefreshToken = request.getRefreshToken();
        Optional<RefreshToken> tokenOpt = refreshTokenService.findByRawToken(rawRefreshToken);
        if (tokenOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid refresh token.");
        }

        RefreshToken token = tokenOpt.get();
        User tokenOwner = token.getUser();

        if (tokenOwner == null || !tokenOwner.getUserId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("Cannot revoke a session belonging to another user.");
        }

        // Revoke only the supplied refresh-token session
        refreshTokenService.revokeRefreshToken(token);

        return new MessageResponseDto("Logged out successfully.");
    }

    @Transactional
    public MessageResponseDto logoutAll(String authenticatedUserId) {
        if (authenticatedUserId == null || authenticatedUserId.isBlank()) {
            throw new InvalidCredentialsException("Unauthenticated user.");
        }

        refreshTokenService.revokeAllUserRefreshTokensByUserId(authenticatedUserId);

        return new MessageResponseDto("All sessions logged out successfully.");
    }
}
