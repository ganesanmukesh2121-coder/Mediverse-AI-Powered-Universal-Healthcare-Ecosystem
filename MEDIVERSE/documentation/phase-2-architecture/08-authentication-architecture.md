# 08 — AUTHENTICATION ARCHITECTURE SPECIFICATION

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 8.1 END-TO-END AUTHENTICATION FLOW

Mediverse relies on stateless JSON Web Token (JWT) authentication coupled with BCrypt password hashing.

```
+-----------------------------------------------------------------------------------+
|                        END-TO-END AUTHENTICATION FLOW                             |
+-----------------------------------------------------------------------------------+
| 1. LOGIN REQUEST:                                                                 |
|    Client sends `POST /api/v1/auth/login` with { email, password }.              |
|                                                                                   |
| 2. CREDENTIAL VALIDATION & BCRYPT CHECK:                                          |
|    Spring Boot backend queries `users` table by email.                            |
|    Validates plaintext password against stored hash via `BCrypt.checkpw()`.       |
|                                                                                   |
| 3. ROLE IDENTIFICATION & ACCOUNT STATUS CHECK:                                    |
|    Extracts user role (`PATIENT`, `DOCTOR`, `ADMIN`, etc.).                       |
|    Verifies `account_status == 'ACTIVE'` (For Providers, `PENDING` returns 403).  |
|                                                                                   |
| 4. JWT GENERATION:                                                                |
|    Spring Boot signs Access Token (60-min expiration) + Refresh Token.            |
|                                                                                   |
| 5. SUBSEQUENT AUTHORIZED API REQUESTS:                                            |
|    Client attaches token: `Authorization: Bearer <JWT_ACCESS_TOKEN>`.             |
|    `JwtAuthenticationFilter` validates signature, expiration, and claims.         |
|                                                                                   |
| 6. AUTHORIZATION ENGINE & AUDIT:                                                  |
|    RBAC Guard + Consent Verification Engine evaluate request.                      |
|    Append-only, tamper-resistant audit log entry is recorded in MySQL.            |
+-----------------------------------------------------------------------------------+
```

---

## 8.2 TOKEN SPECIFICATIONS & LIFECYCLE

- **Access Token:**
  - Algorithm: `HS256` (or `RS256` PKI).
  - Expiration: **60 minutes**.
  - Payload Claims: `sub` (User UUID), `email`, `role`, `health_id` (if Patient), `account_status`, `iat`, `exp`.
- **Refresh Token Architecture (ADR-13):**
  - Opaque random token with 7-day expiration (604,800,000 ms).
  - Persisted in MySQL via `refresh_tokens` entity using secure token hashing (`token_hash`).
  - Supports multi-device logins (`User` 1:N `RefreshToken`).
  - Instant server-side revocation supported via `revoked_at` timestamp.
- **Logout & Revocation Concept:**
  - Stateless JWT logout invalidates local client token storage and revokes corresponding `refresh_tokens` record (`revoked_at = NOW()`).
  - Optional backend token blacklist table handles instant access token revocation during account suspensions.
- **Password Security Standard:**
  - All passwords hashed using **BCrypt** with strength cost factor $\ge 10$. Plaintext passwords are NEVER stored, logged, or returned.
