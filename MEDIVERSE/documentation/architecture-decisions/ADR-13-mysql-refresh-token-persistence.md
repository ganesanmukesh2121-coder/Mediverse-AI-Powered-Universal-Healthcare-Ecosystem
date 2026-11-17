# ADR-13: MYSQL REFRESH TOKEN PERSISTENCE

## STATUS: ACCEPTED

### DECISION
Mediverse will store opaque refresh tokens in MySQL using a dedicated relational table `refresh_tokens`.

### REASONING
1. **Existing Infrastructure Alignment**: MySQL 8 is already the finalized core relational database in Mediverse. Storing refresh tokens in MySQL avoids introducing Redis or other unnecessary caching/infrastructure dependencies.
2. **Server-Side Revocation Control**: Storing refresh tokens on the backend enables instantaneous session revocation (individual session logout or global user session revocation during account suspensions/password resets).
3. **Entity Separation**: Refresh tokens MUST NOT be stored as ad-hoc columns inside the `users` entity. Isolating them in a separate entity maintains clean JPA entity design and supports multi-device/multi-session logins.
4. **JPA Mapping Consistency**: Aligns with existing project entity conventions, foreign key relationships (`CHAR(36)` UUIDs), and indexed lookups.

### SECURITY ENHANCEMENT: REFRESH TOKEN HASHING
- **Raw Token Confidentiality**: The raw opaque refresh token is NEVER stored in the database.
- **Database Storage**: The database stores a secure cryptographic hash (`token_hash`) of the refresh token.
- **Token Delivery**: The raw opaque token is returned ONLY once to the authenticated client in the login/refresh HTTP response.

### REFRESH TOKENS SCHEMA DESIGN (`refresh_tokens` — 20th Relational Entity)
- `refresh_token_id`: `CHAR(36)`, UUID primary key
- `user_id`: `CHAR(36)`, foreign key referencing `users(user_id)`
- `token_hash`: `VARCHAR(255)`, secure hash of the opaque refresh token (unique index)
- `expires_at`: `DATETIME`, expiration timestamp (7 days from creation)
- `created_at`: `DATETIME`, creation timestamp
- `revoked_at`: `DATETIME`, nullable timestamp indicating when the token was explicitly revoked

### RELATIONSHIP
- **User 1:N RefreshToken**: A single user (`User`) can own multiple active `RefreshToken` records, allowing concurrent authenticated sessions across multiple devices or web browsers.

### VALIDATION & REVOCATION RULES
- A refresh token is valid and usable if and only if ALL of the following criteria are satisfied:
  1. The record exists in `refresh_tokens` matching the computed `token_hash`.
  2. `revoked_at IS NULL` (the token has not been revoked).
  3. `expires_at` is in the future (`expires_at > NOW()`).
- **Account-Level Revocation**: When an account is suspended, rejected, or experiences a global security event (e.g. password reset), ALL refresh tokens belonging to that `user_id` are revoked by setting `revoked_at = NOW()`.

### OPEN ARCHITECTURAL DECISIONS (DEFERRED)
The following decisions remain explicitly **OPEN** and are not finalized in this ADR:
1. **Refresh Token Rotation (RTR) Policy**: Whether refreshing an access token automatically invalidates the old refresh token and issues a new refresh token pair.
2. **Exact Logout Semantics**: Whether logout invalidates only the current session token or all user sessions.
3. **Background Cleanup Frequency**: Frequency and scheduling of cleanup tasks for expired/revoked token records.
4. **Physical Deletion vs Audit Retention**: Whether revoked/expired token records are physically deleted from MySQL or retained for audit/forensic tracking.
