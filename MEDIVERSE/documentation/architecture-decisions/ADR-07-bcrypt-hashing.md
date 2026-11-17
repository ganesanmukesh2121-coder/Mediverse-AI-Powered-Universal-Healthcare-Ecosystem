# ADR-07: BCRYPT PASSWORD HASHING ALGORITHM

## STATUS: ACCEPTED

### DECISION
Use BCrypt password hashing algorithm with a minimum cost factor of 10 for all stored user passwords in MySQL.

### REASON
BCrypt incorporates adaptive work factors (cost) and automatic per-password salting to protect authentication credentials against offline rainbow table attacks, dictionary attacks, and GPU-accelerated brute-forcing.

### ALTERNATIVES CONSIDERED
- **MD5 / SHA-256:** Completely rejected due to extreme vulnerability to GPU rainbow table cracking.
- **Argon2:** Excellent alternative, but BCrypt is natively supported in Spring Security out-of-the-box.

### CONSEQUENCES
- Plaintext passwords are NEVER stored, logged, or returned in API responses.
