# ARCHITECTURE FREEZE: MEDIVERSE — AI-POWERED UNIVERSAL HEALTHCARE ECOSYSTEM

> **STATUS: APPROVED ARCHITECTURE BASELINE (FROZEN FOR PHASE 2)**
> 
> *Phase 1.5 Requirements Validation & Corrections Completed. No further modifications to core roles, technology stack, security boundaries, or storage models are permitted without explicit re-approval.*

---

## 1. APPROVED SYSTEM ROLES & PORTALS (FROZEN)

```
+-----------------------------------------------------------------------------------+
|                           APPROVED ROLE & PORTAL BASELINE                         |
+-------------------+--------------------+-------------------+----------------------+
| ROLE              | FRONTEND PLATFORM  | PORTAL TYPE       | CORE PURPOSE         |
+-------------------+--------------------+-------------------+----------------------+
| 1. PATIENT        | Mobile (Flutter)   | Patient Portal    | Health Profile & ID  |
| 2. EMERGENCY      | Mobile (Flutter)   | Emergency Portal  | Vital Triage Info    |
| 3. DOCTOR         | Web (React)        | Doctor Portal     | Consults & Rx        |
| 4. MEDICAL        | Web (React)        | Medical Org Portal| Admissions & Care    |
| 5. LAB            | Web (React)        | Laboratory Portal | Diagnostic Reports   |
| 6. ADMIN          | Web (React)        | Admin Portal      | Verification & Ops   |
+-------------------+--------------------+-------------------+----------------------+
```

### Admin Access Boundary Clarification (Frozen)
- Admin READ access to "Patient Profile & Demographics" is strictly restricted to **administrative identity and account management information** (e.g. User ID, Account Status, Verification Flags, Contact Email).
- **Admin MUST NOT have access to:**
  - Medical history
  - Diagnoses
  - Clinical consultation notes
  - Digital prescriptions
  - Laboratory reports & structured test values
  - Private uploaded medical documents
  - Clinical health timeline

---

## 2. APPROVED TECHNOLOGY BASELINE (FROZEN)

- **Mobile Application Framework:** Flutter + Dart
- **Web Application Framework:** React + HTML / Vanilla CSS / JavaScript
- **Backend Service Framework:** Java 21 + Spring Boot (REST API Services)
- **Relational Database Management System:** MySQL 8.x
- **Authentication Scheme:** Stateless JSON Web Tokens (JWT)
- **Password Hashing Standard:** BCrypt Algorithm ($\text{Cost Factor} \ge 10$)
- **Authorization Model:** Role-Based Access Control (RBAC) + Patient Consent Engine
- **Object Storage Service:** Supabase Storage (Private Buckets + Short-lived Pre-Signed URLs)
- **Audit Logging Standard:** Append-only, tamper-resistant security audit logging
- **AI Ecosystem Architecture:** Dual AI Model:
  1. **Patient AI Assistant:** Mobile (Flutter) — Assists patient using patient-authorized data context.
  2. **Mediverse Healthcare Record Agent:** Web (React) — Assists healthcare pros using RAG context over authorized records.
- **Communication Protocol:** RESTful API over HTTPS

---

## 3. APPROVED CORE ARCHITECTURAL CONCEPTS (FROZEN)

### 3.1 Mediverse Health ID
- Generated exclusively by Spring Boot backend upon patient registration.
- Unique, persistent, immutable, and database-constrained.
- Decoupled from internal surrogate database primary keys (`user_id`).
- Serves strictly as a lookup index identifier; presenting a Health ID alone does **NOT** grant data access authorization.
- QR codes encode signed reference tokens, **NEVER** raw medical records or sensitive text.

### 3.2 Patient-Centric Health Record Model
- Aggregates all authorized clinical interactions—consultations, prescriptions, lab reports, hospital admissions, discharge summaries, and patient uploads—into a unified chronological health timeline.

### 3.3 Dual Storage Architecture
- **Supabase Private Storage:** Stores raw binary files (PDFs, scanned reports, imaging, certificates).
- **MySQL Database:** Stores domain entities, user accounts, Health ID registry, file metadata, consent records, and append-only tamper-resistant audit logs.

### 3.4 3-Tier Security Architecture
```
Frontends (Flutter / React) 
  ↳ Spring Boot REST API (JWT Filter -> RBAC Guard -> Consent Engine -> Audit Logger)
  ↳ MySQL Database / Supabase Storage / AI RAG Engine
```
- Frontends must **NEVER** connect directly to MySQL or Supabase. All data requests pass through Spring Boot.

### 3.5 AI Access Boundaries & Use Cases (Frozen)

1. **Patient AI Assistant (Flutter Mobile):**
   - Intended for patient self-service on mobile devices.
   - Operates strictly within information the patient is explicitly authorized to access.
   - Must NOT provide unrestricted clinical information.
   - Must NOT diagnose conditions or prescribe treatments.

2. **Mediverse Healthcare Record Agent (React Web):**
   - Intended primarily for authorized healthcare professionals (Doctors, Medical staff).
   - Retrieves ONLY authorized patient records under RBAC and active patient consent.
   - Performs source-grounded RAG summarization with mandatory document citations.
   - Must respect all RBAC, consent, and audit logging requirements.
   - Must NOT diagnose, prescribe, or make autonomous clinical treatment decisions.

---

## 4. REGISTER OF OPEN DECISIONS (UNRESOLVED ITEMS)

The following items are explicitly marked as unresolved and will be formally decided during subsequent phases:

- **OQ-01:** Emergency Override Session Expiration Timeout (**OPEN — TO BE DECIDED IN PHASE 2**).
- **OQ-02:** Patient Consent Handshake Protocol (**OPEN — TO BE DECIDED IN PHASE 2**).
- **OQ-03:** Medical Organization & Laboratory Sub-Role Hierarchies (**OPEN — TO BE DECIDED IN PHASE 2**).
- **OQ-04:** QR Code Security Payload Digital Signature Algorithm (**OPEN — TO BE DECIDED IN PHASE 2**).
- **OQ-05:** Selected OCR Engine for Scanned PDF Parsing (**OPEN — TO BE DECIDED IN PHASE 3**).
- **OQ-06:** LLM Provider & Vector Store Selection (**OPEN — TO BE DECIDED IN PHASE 3**).
- **OQ-07:** Real-Time Patient Push Notification Engine (**OPEN — TO BE DECIDED IN PHASE 3**).
- **OQ-08:** Standardized Medical Document Category Taxonomy (**OPEN — TO BE DECIDED IN PHASE 2**).
- **OQ-09:** Medical Certificate & Discharge Summary Digital Signature Standard (**OPEN — TO BE DECIDED IN PHASE 2**).

---

## 5. ARCHITECTURE FREEZE VERIFICATION SIGN-OFF

```
====================================================================================
                   ARCHITECTURE FREEZE VERIFICATION SIGN-OFF
====================================================================================
Project Name : MEDIVERSE — AI-Powered Universal Healthcare Ecosystem
Phase Status : PHASE 1.5 REQUIREMENT VALIDATION & CORRECTIONS COMPLETE
Baseline     : FROZEN AND VERIFIED FOR PHASE 2 (SCHEMA & API DESIGN)
====================================================================================
```
