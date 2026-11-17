# 04 — PROJECT SCOPE

## 4.1 IN-SCOPE FUNCTIONALITY

The scope of Mediverse encompasses the full specification, architectural design, requirements definition, and eventual technical implementation of a multi-portal healthcare ecosystem.

```
+-----------------------------------------------------------------------------------+
|                                 IN-SCOPE ECOSYSTEM                                |
+------------------+------------------+-------------------+-------------------------+
| 1. PATIENT       | 2. EMERGENCY     | 3. DOCTOR         | 4. MEDICAL ORG          |
|    PORTAL        |    PORTAL        |    PORTAL         |    PORTAL               |
|  - Registration  |  - Quick Triage  |  - Health ID Search|  - Hospital Admissions |
|  - Health ID View|  - Critical Info |  - Consult Notes  |  - Clinical Summaries   |
|  - Timeline      |  - Emergency Ops |  - Prescriptions  |  - Discharge Reports    |
|  - Doc Uploads   |  - Audit Logged  |  - AI Summaries   |  - Org Staff Management |
+------------------+------------------+-------------------+-------------------------+
| 5. LABORATORY    | 6. ADMINISTRATIVE| 7. CORE BACKEND   | 8. AI & STORAGE         |
|    PORTAL        |    PORTAL        |    SERVICES       |    ENGINE               |
|  - Test Records  |  - Provider Verify| - Spring Boot REST|  - Supabase Storage     |
|  - Report Upload |  - Role Lifecycle| - MySQL Metadata  |  - LLM + RAG Assistant  |
|  - Result Linking|  - Audit Logs    | - JWT / RBAC Engine| - OCR Workflow         |
+------------------+------------------+-------------------+-------------------------+
```

### 1. Patient Portal (Flutter Mobile)
- User registration, login, profile management.
- Automatic backend assignment of persistent Mediverse Health ID.
- Patient Health Timeline displaying chronological consultations, lab reports, prescriptions, hospital admissions, and uploaded documents.
- User document upload engine (PDF, scanned images, medical reports) with categorization metadata.
- Consent and permission management panel for viewing and granting provider access.
- AI Healthcare Assistant interface for querying personal health records.
- Emergency information configuration (blood group, allergies, emergency contacts, core conditions).

### 2. Emergency Portal (Flutter Mobile)
- Rapid emergency authentication and triage access.
- Patient lookup via Health ID or emergency QR code.
- Immediate read-only display of vital parameters: blood group, critical allergies, emergency contact phone numbers, active high-risk medications, chronic medical conditions.
- Strict session-bound access with automated, non-repudiable emergency audit logging.

### 3. Doctor Portal (React Web)
- Secure login and active verification status check.
- Patient identification via Health ID input or QR scanning reference.
- Consultation entry: clinical notes, diagnosis recording, symptoms capture.
- Digital prescription generation (medications, dosages, frequency, duration).
- Timeline access to authorized historical medical records and attached document previews.
- Triggering AI patient summary generation via Mediverse Healthcare Record Agent.

### 4. Medical Organization Portal (React Web)
- Hospital/clinic organization registration and administrative login.
- Patient lookup for hospital admissions and inpatient tracking.
- Recording admission details, ward/room assignment metadata, and clinical updates.
- Creating discharge summaries and uploading hospital medical records.
- Authorized staff user account management within the organization context.

### 5. Laboratory Portal (React Web)
- Laboratory user login and account management.
- Patient lookup for diagnostic test ordering and record linking.
- Creating lab test records, entering structured test parameters/results.
- Uploading official diagnostic PDF reports directly to the patient's timeline.

### 6. Administrative Portal (React Web)
- Admin login and security dashboard.
- Verification workflow for pending provider applications (`Doctor`, `Medical`, `Lab`).
- Account lifecycle management (activating, deactivating, suspending accounts).
- Security audit monitoring: searching, filtering, and reviewing system-wide access logs.

### 7. Technical Infrastructure (Backend, DB, Storage, AI)
- Spring Boot REST API layer exposing JSON endpoints.
- MySQL relational schema for entities, RBAC, health timelines, metadata, and audit logs.
- Supabase Storage object buckets for encrypted PDF/image document storage.
- AI RAG Orchestrator utilizing vector search and LLMs for grounded patient record retrieval.
- Document Intelligence pipeline using OCR to parse scanned medical PDFs.

---

## 4.2 EXPLICIT OUT-OF-SCOPE BOUNDARIES

To ensure project feasibility within academic timeline constraints, the following features are strictly **OUT OF SCOPE** unless explicitly approved in a future phase:

```
+-----------------------------------------------------------------------------------+
|                           EXPLICITLY OUT OF SCOPE                                 |
+-----------------------------------------------------------------------------------+
| ❌ 1. Autonomous medical diagnosis or automated clinical decision-making.          |
| ❌ 2. Autonomous drug prescribing or automated treatment plan generation.          |
| ❌ 3. Robotic surgery, automated triage, or physical medical device control.       |
| ❌ 4. Real-world integration with live government registries (e.g., ABDM, NHS).  |
| ❌ 5. Direct API integration with commercial hospital EHR/EMR systems (Epic/Cerner)|
| ❌ 6. Live connection to commercial payment gateways or insurance billing engines. |
| ❌ 7. Real patient Personal Health Information (PHI) or real PII datasets.          |
| ❌ 8. Training custom Large Language Models (LLMs) from scratch.                   |
| ❌ 9. Blockchain architectures or distributed ledger smart contracts.              |
| ❌ 10. Direct hardware IoT sensor integration or real-time wearable streaming.     |
| ❌ 11. Custom cloud server provisioning or enterprise multi-region Kubernetes.     |
+-----------------------------------------------------------------------------------+
```

---

## 4.3 SCOPE COMPARISON MATRIX

| Feature Area | In-Scope Execution | Out-of-Scope (Excluded) |
| :--- | :--- | :--- |
| **Patient Identification** | Server-generated Mediverse Health ID & QR reference | Direct Government ID validation APIs |
| **Provider Verification** | Admin review portal workflow (`PENDING` -> `ACTIVE`) | Automatic state medical council registry APIs |
| **Data Storage** | Supabase Storage for files + MySQL metadata | Storing raw medical files as MySQL BLOBs |
| **Medical Assistance** | RAG-based AI summary of patient history for doctors | Autonomous AI diagnosis or treatment prescribing |
| **Patient Datasets** | Synthetic/demo healthcare datasets | Live hospital EMR data or real patient records |
