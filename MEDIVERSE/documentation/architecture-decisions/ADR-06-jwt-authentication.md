# ADR-06: JWT STATELESS AUTHENTICATION

## STATUS: ACCEPTED

### DECISION
Use JSON Web Tokens (JWT) with HS256/RS256 signatures for stateless client-server authentication across mobile and web portals.

### REASON
Stateless JWT tokens eliminate backend session state synchronization overhead across API endpoints, allow mobile and web clients to attach authorization credentials via standard `Authorization: Bearer <TOKEN>` headers, and cleanly encapsulate user identity (`sub`), role (`role`), and Health ID (`health_id`) claims.

### ALTERNATIVES CONSIDERED
- **Stateful HTTP Session Cookies:** Rejected due to CORS complexity and session scaling challenges across mobile apps.

### CONSEQUENCES
- Access tokens must be short-lived (60 minutes) to minimize impact of compromised tokens.
- Immediate token revocation requires backend blacklist checks during sensitive actions.
