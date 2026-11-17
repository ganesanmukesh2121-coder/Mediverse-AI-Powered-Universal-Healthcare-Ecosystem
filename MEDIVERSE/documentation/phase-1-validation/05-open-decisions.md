# 05 — REGISTER OF OPEN DECISIONS

## 5.1 OPEN DECISION GOVERNANCE

Phase 1.5 strictly enforces that **no unapproved assumptions are made**. Any architectural, operational, or technical detail that has not been explicitly validated is recorded in this register and marked as:

> **OPEN — TO BE DECIDED IN PHASE 2** (or Phase 3)

---

## 5.2 REGISTER OF UNRESOLVED DECISIONS

```
+-----------------------------------------------------------------------------------+
|                            REGISTER OF OPEN DECISIONS                             |
+-----------------------------------------------------------------------------------+
| OQ-01: Emergency Override Session Expiration Timeout                              |
| OQ-02: Patient Consent Handshake Protocol (OTP vs. In-App vs. QR)                 |
| OQ-03: Medical Organization & Diagnostic Lab Sub-Role Hierarchies                 |
| OQ-04: QR Code Security Payload Digital Signature Algorithm                       |
| OQ-05: Selected OCR Engine for PDF Document Parsing                               |
| OQ-06: LLM Provider & Vector Store Technology Selection                           |
| OQ-07: Real-Time Patient Push Notification Engine                                 |
| OQ-08: Standardized Medical Document Category Taxonomy                            |
| OQ-09: Medical Certificate & Discharge Summary Digital Signature Standard         |
+-----------------------------------------------------------------------------------+
```

---

## 5.3 DETAILED OPEN DECISION AUDIT SHEET

### 1. OQ-01: Emergency Override Session Expiration Timeout
- **Description:** Exact session duration and re-authentication frequency for active emergency override sessions.
- **Options Under Consideration:** 15-minute fixed session vs. 30-minute auto-expiring session vs. single-lookup session.
- **Status:** **OPEN — TO BE DECIDED IN PHASE 2** (Phase 2 Security Architecture).

### 2. OQ-02: Patient Consent Handshake Protocol
- **Description:** Exact workflow for patients to grant consent to doctors during physical consultations.
- **Options Under Consideration:**
  - Option A: Patient scans Doctor Portal QR code on mobile app.
  - Option B: Doctor triggers consent request; Patient receives OTP via SMS/App.
  - Option C: Patient approves in-app push notification on mobile app.
- **Status:** **OPEN — TO BE DECIDED IN PHASE 2** (Phase 2 API & Workflow Specs).

### 3. OQ-03: Medical Organization & Diagnostic Lab Sub-Role Hierarchies
- **Description:** Whether hospital and lab portals require internal sub-roles (e.g., Head Pathologist vs. Lab Technician, Admitting Clerk vs. Attending Physician).
- **Options Under Consideration:** Flat role model (`MEDICAL`, `LAB`) for academic demo vs. granular sub-role hierarchy.
- **Status:** **OPEN — TO BE DECIDED IN PHASE 2** (Phase 2 Database Schema Design).

### 4. OQ-04: QR Code Security Payload Digital Signature Algorithm
- **Description:** Selection of cryptographic signing algorithm for securing patient Health ID references inside mobile QR codes.
- **Options Under Consideration:** HMAC-SHA256 signed payload vs. RSA/ECDSA asymmetric signature.
- **Status:** **OPEN — TO BE DECIDED IN PHASE 2** (Phase 2 Security Specification).

### 5. OQ-05: Selected OCR Engine for Scanned PDF Parsing
- **Description:** Technical library selection for extracting text from scanned medical PDFs and images.
- **Options Under Consideration:** Tesseract OCR (Open Source) vs. AWS Textract (Cloud) vs. EasyOCR (Python).
- **Status:** **OPEN — TO BE DECIDED IN PHASE 3** (Phase 3 AI Subsystem Specification).

### 6. OQ-06: LLM Provider & Vector Store Selection
- **Description:** Technology stack choice for the **Mediverse Healthcare Record Agent** RAG engine.
- **Options Under Consideration:** Groq API / OpenAI API vs. Local Ollama (Llama 3 / Mistral) + PgVector / Qdrant.
- **Status:** **OPEN — TO BE DECIDED IN PHASE 3** (Phase 3 AI Subsystem Specification).

### 7. OQ-07: Real-Time Patient Push Notification Engine
- **Description:** Infrastructure provider for sending patient alerts when records are accessed or added.
- **Options Under Consideration:** Firebase Cloud Messaging (FCM) vs. WebSockets vs. Local In-App Notifications.
- **Status:** **OPEN — TO BE DECIDED IN PHASE 3** (Phase 3 Mobile/Web Integration).

### 8. OQ-08: Standardized Medical Document Category Taxonomy
- **Description:** Formal enumeration of document category tags permitted during patient and provider document uploads.
- **Options Under Consideration:** Fixed enum (`PRESCRIPTION`, `LAB_REPORT`, `DISCHARGE_SUMMARY`, `IMAGING_SCAN`, `CERTIFICATE`, `OTHER`) vs. dynamic category lookup table.
- **Status:** **OPEN — TO BE DECIDED IN PHASE 2** (Phase 2 Database Schema Design).

### 9. OQ-09: Medical Certificate & Discharge Summary Digital Signature Standard
- **Description:** Cryptographic standard for verifying authenticity of official medical certificates and hospital discharge summaries published via Mediverse.
- **Options Under Consideration:** Server-signed PDF metadata hash vs. public key infrastructure (PKI) digital signatures.
- **Status:** **OPEN — TO BE DECIDED IN PHASE 2** (Phase 2 Architecture Design).
