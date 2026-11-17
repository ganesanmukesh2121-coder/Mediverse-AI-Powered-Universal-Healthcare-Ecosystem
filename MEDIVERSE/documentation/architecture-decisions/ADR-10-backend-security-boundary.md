# ADR-10: BACKEND API ISOLATION SECURITY BOUNDARY

## STATUS: ACCEPTED

### DECISION
Enforce a hard architectural isolation boundary: all presentation frontends (Flutter Mobile and React Web) connect exclusively via the Spring Boot REST API layer. Direct database or storage connections from frontends are strictly forbidden.

### REASON
Allowing client applications to connect directly to MySQL or cloud storage bypasses backend security authorization filters, RBAC rules, patient consent logic, and audit logging. Routing 100% of data traffic through Spring Boot guarantees that every request is authenticated, authorized, validated, and logged.

### ALTERNATIVES CONSIDERED
- **Direct Client-to-Supabase Storage Downloads:** Rejected to prevent unauthorized URL sharing or token leakage.

### CONSEQUENCES
- Spring Boot backend acts as the single security gateway and proxy for storage pre-signed URL generation.
