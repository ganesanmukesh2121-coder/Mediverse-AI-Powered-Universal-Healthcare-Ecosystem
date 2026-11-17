# 07 — NON-FUNCTIONAL REQUIREMENTS

## 7.1 CATEGORIZATION OF NON-FUNCTIONAL REQUIREMENTS

Non-functional requirements specify system quality attributes, operational constraints, and architectural governance standards suitable for a final-year B.Tech IT engineering project.

```
+-----------------------------------------------------------------------------------+
|                            NON-FUNCTIONAL PILLARS                                 |
+-------------------+--------------------+-------------------+----------------------+
| SECURITY & PRIVACY| PERFORMANCE        | RELIABILITY       | MAINTAINABILITY      |
|  - JWT & BCrypt   |  - API < 500ms     |  - Graceful Fail  |  - Modular Monolith  |
|  - RBAC & Consent |  - Docs < 2s       |  - DB Transactions|  - Clean Layering    |
|  - Private Storage|  - 50 Concurrency  |  - Error Handlers |  - REST Standards    |
|  - Tamper Audit   |                    |                   |                      |
+-------------------+--------------------+-------------------+----------------------+
```

---

## 7.2 SECURITY REQUIREMENTS (NFR-SEC)

- **NFR-SEC-01 (Stateless JWT Authentication):** All REST API endpoints (excluding public login/registration) must require a valid, signed JSON Web Token (JWT) in the `Authorization: Bearer <token>` HTTP header.
- **NFR-SEC-02 (Password Encryption):** User passwords must NEVER be stored in plaintext or reversible encryption. All passwords must be salted and hashed using the **BCrypt** algorithm (minimum strength factor 10).
- **NFR-SEC-03 (Role-Based Access Control):** The Spring Boot backend must enforce method-level or URL-level RBAC security rules for all API routes based on token claims.
- **NFR-SEC-04 (HTTPS Encryption):** All communication between client applications (Flutter mobile, React web) and the Spring Boot backend must take place over encrypted Transport Layer Security (TLS/HTTPS).
- **NFR-SEC-05 (Input Sanitization & Injection Defense):** All incoming API request bodies, path variables, and query parameters must be validated and sanitized to prevent SQL Injection, Cross-Site Scripting (XSS), and Command Injection attacks.
- **NFR-SEC-06 (Private Storage Buckets):** Supabase Storage buckets containing patient medical files must remain strictly private. Direct public URL access to medical documents is prohibited.
- **NFR-SEC-07 (Non-Exposed Database):** MySQL database ports must NOT be exposed to the public internet. Mobile and Web applications must NEVER establish direct connections to MySQL.
- **NFR-SEC-08 (Append-Only Audit Logging):** All security-sensitive access attempts, permissions changes, and data queries must write append-only, tamper-resistant audit logs to MySQL.

---

## 7.3 PRIVACY REQUIREMENTS (NFR-PRV)

- **NFR-PRV-01 (Data Minimization):** Healthcare providers must receive only the minimum necessary patient information required for their specific workflow (e.g., Labs receive diagnostic order parameters only; Emergency receives vital triage parameters only).
- **NFR-PRV-02 (Consent-Gated Data Access):** Clinical consultation history, historical prescriptions, and uploaded medical documents can only be retrieved by external providers if explicit patient consent or valid emergency override rules exist.
- **NFR-PRV-03 (QR Code Privacy):** Patient QR codes must contain only signed reference identifiers. QR codes must NEVER encode sensitive clinical diagnosis text, drug lists, or personal health records directly inside the QR payload.
- **NFR-PRV-04 (Administrative Isolation):** System administrators are prohibited from viewing patient medical documents, clinical consultation notes, prescriptions, lab reports, or clinical timelines. Admin READ access to patient profile data is strictly limited to identity/account management parameters (e.g. User ID, status flags, email).

---

## 7.4 PERFORMANCE REQUIREMENTS (NFR-PER)

*Note: Performance targets are calibrated for a final-year engineering project demonstration environment.*

- **NFR-PER-01 (API Response Time):** Standard REST API endpoints (authentication, profile fetch, patient lookup, prescription creation) must respond within **500 milliseconds** under standard demonstration loads.
- **NFR-PER-02 (Document Load Speed):** Pre-signed document URL generation and initial metadata retrieval for medical PDFs/images must complete within **2.0 seconds**.
- **NFR-PER-03 (AI Synthesis Latency):** RAG summary generation via the Mediverse Healthcare Record Agent for complex patient timelines must complete within **5.0 to 10.0 seconds** with interactive loading feedback on the Doctor Portal.
- **NFR-PER-04 (Demonstration Concurrency):** The Spring Boot API and MySQL database must support a minimum of **50 concurrent user sessions** without performance degradation during project evaluation testing.

---

## 7.5 RELIABILITY & FAULT TOLERANCE (NFR-REL)

- **NFR-REL-01 (Graceful Exception Handling):** The backend must handle errors gracefully, returning standardized JSON error responses containing error codes, user-friendly messages, and timestamps without exposing raw Java stack traces.
- **NFR-REL-02 (Database Transaction Boundaries):** All multi-step database writes (e.g., patient registration creating user record + generating Health ID + initializing profile) must execute within `@Transactional` boundaries. If any step fails, the entire transaction must roll back.
- **NFR-REL-03 (Storage Upload Fallback):** If a file upload to Supabase Storage fails, MySQL document metadata records must NOT be orphan-committed.

---

## 7.6 MAINTAINABILITY & CODE STRUCTURE (NFR-MNT)

- **NFR-MNT-01 (Layered Modular Architecture):** The Spring Boot backend must follow a clean layered architectural pattern: Controller Layer → Service Layer → Repository Layer → Entity Layer.
- **NFR-MNT-02 (Frontend Component Modularity):** React Web portals and Flutter Mobile apps must utilize modular, reusable component architecture with clear separation between UI layout, state management, and API service calls.

---

## 7.7 SCALABILITY (NFR-SCL)

- **NFR-SCL-01 (Stateless Application Tier):** Spring Boot API services must remain strictly stateless. Session state must reside in JWT tokens or database records.
- **NFR-SCL-02 (Scalable Storage Integration):** Using Supabase Storage ensures document storage capacity scales independently of the application backend.

---

## 7.8 USABILITY & ACCESSIBILITY (NFR-USB)

- **NFR-USB-01 (Intuitive UI/UX):** Frontend interfaces must feature modern visual design, clear typography, intuitive navigation, responsive layouts, and distinct visual feedback for critical operations.
- **NFR-USB-02 (Mobile Usability):** Patient and Emergency Flutter apps must be optimized for mobile touchscreens with responsive navigation, clear status badges, and accessible font sizes.
