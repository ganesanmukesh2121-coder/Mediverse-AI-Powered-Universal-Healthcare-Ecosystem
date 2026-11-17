# 15 — SECURITY ARCHITECTURE SPECIFICATION

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 15.1 COMPREHENSIVE SECURITY DEFENSE IN DEPTH

Mediverse incorporates 16 core security controls protecting data at rest, in transit, and during API execution.

```
+-----------------------------------------------------------------------------------+
|                        DEFENSE-IN-DEPTH SECURITY MATRIX                           |
+---------------------+-------------------------------------------------------------+
| SECURITY DOMAIN     | IMPLEMENTED ARCHITECTURAL CONTROL                           |
+---------------------+-------------------------------------------------------------+
| 1. Transport        | TLS 1.3 / HTTPS for all Client-to-API network traffic.      |
| 2. Authentication   | Stateless JWT Bearer Tokens with 60-minute expiration.       |
| 3. Password Storage | BCrypt hashing algorithm with strength cost factor >= 10.   |
| 4. Authorization    | Method-level Spring Security RBAC annotations.             |
| 5. Patient Consent  | Mandatory database validation before clinical record reads. |
| 6. Input Validation | DTO validation (`@Valid`, `@NotNull`, `@Pattern`) on APIs.  |
| 7. SQL Injection    | JPA Criteria & Parameterized Prepared Statements only.       |
| 8. File Validation  | MIME type verification & 15 MB file size boundary.          |
| 9. Storage Security | Private Supabase Buckets + 15-minute pre-signed URLs.       |
| 10. API Gateway     | Spring Boot API proxy isolating database from public net.   |
| 11. Audit Logging   | Append-only, tamper-resistant MySQL audit trail logging.    |
| 12. Admin Isolation | Admin READ access to profile limited to account identity.   |
| 13. AI Boundaries   | AI prohibited from autonomous diagnosis/prescribing.        |
| 14. CORS Control    | Restrictive Origin, Method, and Header CORS policies.       |
| 15. Error Handling  | Standardized JSON errors masking raw Java stack traces.      |
| 16. Data Minimization| Workflows retrieve minimal necessary record parameters.     |
+---------------------+-------------------------------------------------------------+
```
