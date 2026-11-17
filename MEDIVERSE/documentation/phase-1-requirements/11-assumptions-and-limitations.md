# 11 — ASSUMPTIONS, LIMITATIONS, AND OPEN QUESTIONS

## 11.1 TECHNICAL ASSUMPTIONS

The architectural design of Mediverse rests upon the following technical assumptions:

1. **Synthetic Data Baseline:** The platform will be demonstrated and tested using synthetic/mock patient demographics, medical history files, lab test reports, and doctor notes. No real-world Personal Health Information (PHI) or Personally Identifiable Information (PII) will be processed.
2. **Standard HTTP/HTTPS Environment:** Mobile and Web clients operate in environments supporting standard HTTPS communication, JSON parsing, and local secure storage for JWT tokens.
3. **Database Availability:** A standard MySQL 8.x database instance is available locally or via cloud deployment to handle relational schemas, metadata, and audit logs.
4. **Storage Service Availability:** Supabase Storage is accessible via standard REST APIs for binary file upload and pre-signed URL generation.

---

## 11.2 OPERATIONAL & SYSTEM LIMITATIONS

1. **No External EMR Integration:** Mediverse does NOT integrate with third-party hospital Electronic Health Record (EHR) software (e.g., Epic, Cerner) or national digital health networks (e.g., ABDM) during Phase 1/2.
2. **Academic Demonstration Concurrency:** System capacity is scaled and validated for academic demonstration targets (50 concurrent users), not enterprise multi-region production loads.
3. **Simulated Verification:** Provider identity verification (Doctors, Medical Orgs, Labs) relies on administrative manual workflow within the Admin Portal, rather than real-time API verification against state medical registers.

---

## 11.3 REQUIREMENTS TO BE DECIDED IN A LATER PHASE (OPEN QUESTIONS)

The following architectural and operational edge-cases require formal decision and approval during subsequent project phases:

```
+-----------------------------------------------------------------------------------+
|                        OPEN QUESTIONS (LATER PHASE DECISIONS)                     |
+-----------------------------------------------------------------------------------+
| ❓ OQ-01: EMERGENCY SESSION TIMEOUT & MFA REQUIREMENTS                            |
|    - Exact duration for active emergency override sessions before re-authentication. |
|                                                                                   |
| ❓ OQ-02: PATIENT CONSENT HANDSHAKE WORKFLOW                                      |
|    - Decision between OTP-based consent vs. In-App push notification vs. QR scan.  |
|                                                                                   |
| ❓ OQ-03: MEDICAL ORGANIZATION & LABORATORY USER HIERARCHIES                      |
|    - Sub-role permissions (e.g., Head Pathologist vs. Lab Technician).            |
|                                                                                   |
| ❓ OQ-04: QR CODE ENCRYPTION & SIGNING MECHANISM                                  |
|    - Selection of digital signature algorithm for mobile QR payload verification. |
|                                                                                   |
| ❓ OQ-05: OPTICAL CHARACTER RECOGNITION (OCR) LIBRARY                               |
|    - Benchmarking Tesseract OCR vs. AWS Textract vs. EasyOCR for PDF scans.       |
|                                                                                   |
| ❓ OQ-06: LLM PROVIDER & VECTOR DATABASE SELECTION                                |
|    - Choice between cloud LLM (Groq / OpenAI) vs. local self-hosted model (Ollama). |
|                                                                                   |
| ❓ OQ-07: PATIENT NOTIFICATION ENGINE ARCHITECTURE                                |
|    - Selection of push notification provider (Firebase Cloud Messaging vs. SMS).  |
|                                                                                   |
| ❓ OQ-08: DOCUMENT TAXONOMY & CATEGORIZATION STANDARDS                            |
|    - Standardizing clinical document tags across all six portals.                 |
+-----------------------------------------------------------------------------------+
```

---

## 11.4 SUMMARY TABLE OF OPEN QUESTIONS

| ID | Topic | Current Status | Target Phase for Resolution |
| :--- | :--- | :--- | :--- |
| **OQ-01** | Emergency Timeout | **TO BE DECIDED IN A LATER PHASE** | Phase 2 (Architecture Specification) |
| **OQ-02** | Consent Handshake Model | **TO BE DECIDED IN A LATER PHASE** | Phase 2 (Architecture Specification) |
| **OQ-03** | Sub-role Hierarchies | **TO BE DECIDED IN A LATER PHASE** | Phase 2 (Schema Design) |
| **OQ-04** | QR Security Spec | **TO BE DECIDED IN A LATER PHASE** | Phase 2 (Security Specification) |
| **OQ-05** | OCR Engine Selection | **TO BE DECIDED IN A LATER PHASE** | Phase 3 (AI Subsystem Specs) |
| **OQ-06** | LLM & Vector Store | **TO BE DECIDED IN A LATER PHASE** | Phase 3 (AI Subsystem Specs) |
| **OQ-07** | Push Notifications | **TO BE DECIDED IN A LATER PHASE** | Phase 3 (Frontend Integration) |
| **OQ-08** | Document Taxonomy | **TO BE DECIDED IN A LATER PHASE** | Phase 2 (Database Design) |
