# 04 — SECURITY BOUNDARY REVIEW

## 4.1 END-TO-END SECURITY FLOW

The Mediverse security model enforces strict architectural boundaries across client, backend, database, storage, and AI tiers.

```
+-----------------------------------------------------------------------------------+
|                        END-TO-END SECURITY BOUNDARY FLOW                          |
+-----------------------------------------------------------------------------------+
| CLIENT TIER:                                                                      |
| Flutter Mobile (Patient / Emergency)  |  React Web (Doctor / Medical / Lab / Admin)|
|                                       |                                           |
|       === HTTP Requests over TLS (JWT Bearer Token in Header) ===>                |
|                                       |                                           |
| BACKEND API TIER:                     v                                           |
| [ Spring Boot REST API Service ]                                                  |
|   ↳ 1. JWT Signature & Expiration Filter                                          |
|   ↳ 2. Role-Based Access Control (RBAC) Filter                                    |
|   ↳ 3. Account Status Check (Requires `ACTIVE` for Providers)                     |
|   ↳ 4. Patient Consent / Emergency Access Rules Engine                            |
|   ↳ 5. Append-Only Audit Logging Service                                          |
|                                       |                                           |
|       +-------------------------------+-------------------------------+           |
|       |                               |                               |           |
|       v                               v                               v           |
| DATA TIER:                    STORAGE TIER:                   AI TIER:            |
| [ MySQL Database ]            [ Supabase Storage ]            [ Dual AI Engine ]  |
| - Entities & Meta             - Private Buckets               - Patient AI Mobile |
| - Append-only Audit           - Pre-signed URLs               - Healthcare RAG Web|
+-----------------------------------------------------------------------------------+
```

---

## 4.2 VERIFICATION OF CRITICAL SECURITY BOUNDARIES

```
+-----------------------------------------------------------------------------------+
|                       SECURITY BOUNDARY CHECKLIST & VERIFICATION                  |
+-----------------------------------------------------------------------------------+
| [X] BOUNDARY 1: NO DIRECT DATABASE CONNECTIONS FROM FRONTENDS                     |
|     - Mobile and Web clients connect strictly via Spring Boot REST APIs.          |
|     - MySQL ports are completely firewalled from public exposure.                |
|                                                                                   |
| [X] BOUNDARY 2: BACKEND AUTHORIZATION ENFORCEMENT                                 |
|     - All authorization logic runs on Spring Boot backend.                        |
|     - Clients cannot bypass authorization or manipulate local state to gain access.|
|                                                                                   |
| [X] BOUNDARY 3: HEALTH ID IS NOT AUTHENTICATION OR AUTHORIZATION                  |
|     - Health ID serves strictly as a patient lookup index identifier.             |
|     - Possessing a Health ID does NOT grant data access without consent/emergency.|
|                                                                                   |
| [X] BOUNDARY 4: PRIVATE MEDICAL DOCUMENT STORAGE                                  |
|     - Supabase Storage buckets are private; direct public URL downloads are disabled|
|     - Pre-signed short-lived URLs (<= 15 mins) generated only after consent checks.|
|                                                                                   |
| [X] BOUNDARY 5: DUAL AI ACCESS BOUNDARY                                           |
|     - Patient AI Assistant (Mobile): Restricted to patient-authorized data.       |
|     - Healthcare Record Agent (Web): Restricted to provider-authorized RAG context|
|     - Neither AI model can bypass RBAC or patient consent rules.                 |
|                                                                                   |
| [X] BOUNDARY 6: ADMIN PRIVACY ISOLATION                                           |
|     - System Admin manages identity, verification, and audit logs.                |
|     - Admin READ access to profile is strictly LIMITED to account identity info.  |
|     - Admin has ZERO access to medical histories, consult notes, Rx, or PDFs.     |
|                                                                                   |
| [X] BOUNDARY 7: APPEND-ONLY AUDIT LOG TRAIL                                       |
|     - Audit logs are append-only and tamper-resistant; insert-only execution.    |
|     - Plaintext passwords and PHI are strictly masked from audit entries.         |
+-----------------------------------------------------------------------------------+
```

---

## 4.3 DETAILED SECURITY BOUNDARY SPECIFICATIONS

### 1. Client-Backend Isolation
- **Rule:** Mobile and Web frontends are untrusted environments. No sensitive security checks or database queries may originate directly from client-side code.
- **Verification:** All data operations pass through Spring Boot REST Controllers annotated with Spring Security authorization guards (`@PreAuthorize("hasRole('...')")`).

### 2. Health ID Boundary Rules
- **Rule:** The **Mediverse Health ID** is an identification string, NOT a secret token, password, or authorization key.
- **Verification:** Merely providing a Health ID in an API request header or body does NOT grant access. The backend verifies active patient consent or emergency rules for the authenticated actor before executing data reads.

### 3. Supabase Private Object Storage Boundary
- **Rule:** Medical files (PDFs, images, scans) uploaded to Supabase Storage are stored in **Private** object buckets.
- **Verification:** Frontends cannot construct static public URLs to fetch files. The Spring Boot backend validates consent and issues temporary pre-signed URLs with a 15-minute expiration time.

### 4. Dual AI Subsystem Boundaries
- **Rule:** 
  1. Patient AI Assistant (Mobile) operates strictly over information the patient is explicitly authorized to access. Must not provide unrestricted clinical information and cannot diagnose or prescribe.
  2. Mediverse Healthcare Record Agent (Web Doctor Portal) operates strictly over authorized records under RBAC and patient consent. Performs source-grounded RAG summarization with citations. Cannot diagnose, prescribe, or make autonomous treatment decisions.

### 5. Administrative Privacy Boundary
- **Rule:** Administrators are system operators, not clinical providers.
- **Verification:** Admin READ access to Patient Profile is strictly LIMITED to administrative identity and account management parameters (User ID, account status, email). Spring Boot API routing rules reject any Admin attempt to query medical histories, clinical consult notes, prescriptions, lab values, or private uploaded medical documents.
