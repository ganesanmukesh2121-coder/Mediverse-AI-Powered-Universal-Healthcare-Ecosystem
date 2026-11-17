# 10 — SECURITY REQUIREMENTS & AUDIT ARCHITECTURE

## 10.1 SECURITY ARCHITECTURE OVERVIEW

Mediverse implements a defense-in-depth security model protecting sensitive healthcare records across all six portals. Security relies on five core pillars:

```
+-----------------------------------------------------------------------------------+
|                           DEFENSE-IN-DEPTH SECURITY MODEL                         |
+-----------------------------------------------------------------------------------+
| 1. AUTHENTICATION  -> JWT Bearer Tokens + BCrypt Password Hashing                 |
| 2. AUTHORIZATION   -> Role-Based Access Control (RBAC) across 6 roles              |
| 3. CONSENT ENGINE  -> Explicit Patient Consent & Emergency Access Rules           |
| 4. AUDIT TRAIL     -> Append-only, tamper-resistant audit logging                 |
| 5. STORAGE DEFENSE -> Private Supabase Buckets + Pre-Signed URL Access Tokens     |
+-----------------------------------------------------------------------------------+
```

---

## 10.2 AUTHENTICATION & CREDENTIAL SECURITY

### 1. Password Security
- User passwords MUST be salted and hashed using the **BCrypt** algorithm (cost factor $\ge 10$).
- Plaintext passwords must NEVER be saved in databases, printed in console logs, or returned in API responses.

### 2. JSON Web Token (JWT) Specifications
- Upon successful authentication, the Spring Boot API issues a signed JWT token.
- **JWT Header:** Algorithm `HS256` or `RS256`, type `JWT`.
- **JWT Payload Claims:** `sub` (User ID), `health_id` (Mediverse Health ID), `role` (`PATIENT`, `EMERGENCY`, `DOCTOR`, `MEDICAL`, `LAB`, `ADMIN`), `verification_status` (`PENDING`, `ACTIVE`, `REJECTED`, `SUSPENDED`), `iat`, `exp` (60 minutes).
- Subsequent API requests must attach token via HTTP header: `Authorization: Bearer <JWT_TOKEN>`.

---

## 10.3 AUTHORIZATION & ROLE-BASED ACCESS CONTROL (RBAC)

Spring Boot API filters intercept every incoming HTTP request and validate RBAC permissions against token claims:

```
Request Received -> Extract JWT -> Validate Signature & Expiration -> Extract Role & Verification Status
  ↳ Verification Status == 'ACTIVE'? (If not ACTIVE & non-admin -> Return 403 Forbidden)
  ↳ Role matches Route Authority? (If no -> Return 403 Forbidden)
  ↳ Execute Patient Consent / Emergency Access Check
  ↳ Allow Request Execution
```

---

## 10.4 PATIENT CONSENT & ADMIN ISOLATION ENGINES

Access to a patient's historical records by Doctors or Medical Organizations must pass the **Consent Engine**:

```
+-----------------------------------------------------------------------------------+
|                           CONSENT VERIFICATION PIPELINE                           |
+-----------------------------------------------------------------------------------+
| Provider Requests Patient Timeline                                                |
|   |                                                                               |
|   v                                                                               |
| Check Request Role -> Is ADMIN? -> DENY (Admin cannot view private clinical data) |
|   |                                                                               |
|   v                                                                               |
| Is EMERGENCY? -> Execute Emergency Override Handler & Audit -> ALLOW (Vitals Only)|
|   |                                                                               |
|   v                                                                               |
| Is DOCTOR / MEDICAL? -> Check MySQL `patient_consents` table                       |
|   |--> Active Consent Found & Valid? -> ALLOW Access                               |
|   |--> No Active Consent? -> DENY Access (403 Forbidden: "Consent Required")      |
+-----------------------------------------------------------------------------------+
```

### Administrative Privacy Boundary
- Admin READ access to Patient Profile is strictly LIMITED to administrative identity/account management info (User ID, account status, email).
- **Admin MUST NOT have access to:** medical history, diagnosis, consultation notes, prescriptions, laboratory reports, private medical documents, or clinical timelines.

---

## 10.5 EMERGENCY ACCESS SECURITY & OVERRIDE PROTOCOL

1. **Authentication Requirement:** Emergency responders must be authenticated users possessing the `EMERGENCY` role.
2. **Access Limitation:** Emergency API endpoints query strictly designated triage parameters (blood group, severe allergies, active high-risk medications, core conditions, emergency contacts).
3. **Mandatory Append-Only Audit:** Every emergency lookup writes an append-only, tamper-resistant audit entry containing responder user ID, target Health ID, device IP, timestamp, and stated emergency rationale.
4. **Patient Notification:** Upon system sync, the patient receives an in-app security notification informing them that an Emergency Access override was executed on their profile.

---

## 10.6 AUDIT LOGGING ARCHITECTURE

All security-sensitive operations across all six portals MUST produce an append-only, tamper-resistant audit log entry in the MySQL `audit_logs` table.

```
+-----------------------------------------------------------------------------------+
|                                AUDIT LOG DATA SCHEMA                              |
+---------------------+-------------------+-----------------------------------------+
| COLUMN NAME         | DATA TYPE         | DESCRIPTION                             |
+---------------------+-------------------+-----------------------------------------+
| audit_id            | BIGINT (PK, Auto) | Unique log record identifier            |
| timestamp           | DATETIME (UTC)    | Exact timestamp of operation execution  |
| actor_user_id       | VARCHAR(36)       | UUID of user performing action          |
| actor_role          | VARCHAR(20)       | Role (`PATIENT`, `DOCTOR`, `ADMIN`, etc)|
| target_patient_id   | VARCHAR(36)       | Health ID / User ID of affected patient |
| action_type         | VARCHAR(50)       | Operation code (`LOGIN`, `EMERGENCY_OVERRIDE`)|
| resource_uri        | VARCHAR(255)      | API route accessed (`/api/v1/doctor/..`)|
| execution_status    | VARCHAR(20)       | `SUCCESS`, `FAILURE`, `DENIED`          |
| ip_address          | VARCHAR(45)       | Client IPv4/IPv6 address                |
| user_agent          | VARCHAR(255)      | Browser/App user agent string           |
| security_context    | TEXT (JSON)       | Additional metadata (e.g. override reason)|
+---------------------+-------------------+-----------------------------------------+
```

### Audit Logging Guardrails
- **Append-Only & Tamper-Resistant:** Audit records are insert-only. No API endpoint or user role (including `ADMIN`) is permitted to update or delete audit records.
- **Data Masking:** Audit logs must NEVER record plaintext passwords, JWT secret keys, or raw patient clinical notes inside log payloads.

---

## 10.7 FILE STORAGE SECURITY (SUPABASE STORAGE)

1. **Private Buckets:** Supabase Storage buckets (`patient-documents`, `lab-reports`, `hospital-files`) are configured as **Private**. Direct public URL downloads are disabled.
2. **Pre-Signed Timed URLs:** When an authorized user requests a document, the backend verifies consent and generates a short-lived pre-signed URL (expiration: $\le 15$ minutes).
3. **MIME-Type & Hash Validation:** Uploaded files must undergo MIME-type verification (permitting only `application/pdf`, `image/jpeg`, `image/png`) and file size limits ($\le 15 \text{ MB}$) before storage processing.
