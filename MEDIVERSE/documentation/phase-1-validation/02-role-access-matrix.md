# 02 — COMPREHENSIVE ROLE-ACCESS MATRIX

## 2.1 ACCESS CONTROL LEGEND

Access permissions across system resources are defined using standardized indicators:

- **OWN:** Full control and sovereignty over personal record creation, modification, and consent management.
- **CREATE:** Authority to author and publish new records.
- **READ:** Full read access to authorized resources.
- **UPDATE:** Ability to modify existing records authored by the role.
- **LIMITED:** Restrictive access to specific purpose-bound parameters only (e.g. Emergency triage vitals only, Lab diagnostic orders only, or Admin account identity info only).
- **DENIED:** Absolute access restriction enforced by Spring Boot RBAC filters.

---

## 2.2 SYSTEM ROLE-ACCESS MATRIX

| Resource Category | Patient | Emergency | Doctor | Medical Org | Lab | Admin | Authorization Condition |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **Patient Account Identity (Admin)**| **OWN** | **LIMITED** | **READ** | **READ** | **LIMITED** | **LIMITED** | Admin access restricted strictly to account/identity management. |
| **Medical History & Timeline** | **OWN** | **DENIED** | **READ** | **READ** | **DENIED** | **DENIED** | **Patient Consent Required** for Doctor & Medical. Admin DENIED. |
| **Emergency Vitals & Allergies** | **OWN** | **READ** | **READ** | **READ** | **DENIED** | **DENIED** | Emergency access triggers append-only audit log. |
| **Consultation Notes & Diagnosis** | **READ** | **DENIED** | **CREATE / UPDATE** | **DENIED** | **DENIED** | **DENIED** | Doctor must be author; Patient consent required. Admin DENIED. |
| **Digital Prescriptions** | **READ** | **DENIED** | **CREATE / UPDATE** | **DENIED** | **DENIED** | **DENIED** | Doctor creates; Patient reads own prescriptions. Admin DENIED. |
| **Lab Orders & Test Requisitions** | **READ** | **DENIED** | **CREATE** | **CREATE** | **CREATE / UPDATE**| **DENIED** | Purpose-bound to active lab diagnostic order. Admin DENIED. |
| **Diagnostic PDF Reports & Scans** | **READ** | **DENIED** | **READ** | **READ** | **CREATE** | **DENIED** | Lab uploads; Doctor/Medical read under consent. Admin DENIED. |
| **Hospital Admissions & Treatment**| **READ** | **DENIED** | **READ** | **CREATE / UPDATE**| **DENIED** | **DENIED** | Medical Org authors; Patient consent required. Admin DENIED. |
| **Hospital Discharge Summaries** | **READ** | **DENIED** | **READ** | **CREATE / UPDATE**| **DENIED** | **DENIED** | Published by hospital; Linked to patient timeline. Admin DENIED.|
| **Personal Document Uploads** | **OWN** | **DENIED** | **READ** | **READ** | **DENIED** | **DENIED** | Uploaded by Patient; Doctor reads under consent. Admin DENIED. |
| **Patient Consent Management** | **OWN** | **DENIED** | **DENIED** | **DENIED** | **DENIED** | **DENIED** | Patient exclusively manages consent grants/revocations.|
| **Provider Verification Queue** | **DENIED** | **DENIED** | **READ (Own)** | **READ (Own)** | **READ (Own)**| **CREATE / UPDATE**| Admin exclusively verifies provider accounts. |
| **System Security Audit Logs** | **LIMITED**| **DENIED** | **DENIED** | **DENIED** | **DENIED** | **READ** | Admin reads global logs; Patient views own log. |
| **Patient AI Assistant Views** | **READ** | **DENIED** | **DENIED** | **DENIED** | **DENIED** | **DENIED** | Operates on Flutter Mobile using patient-authorized data context. |
| **Mediverse Healthcare Record Agent**| **DENIED** | **DENIED** | **READ** | **READ** | **DENIED** | **DENIED** | Operates on Web Portals for healthcare pros under RAG & consent.|

---

## 2.3 MANDATORY ACCESS BOUNDARIES & RULES

```
+-----------------------------------------------------------------------------------+
|                        AUTHORIZATION ENFORCEMENT RULES                            |
+-----------------------------------------------------------------------------------+
| 1. ADMIN ACCESS BOUNDARY RULE:                                                    |
|    Admin READ access to Patient Profile is strictly LIMITED to administrative     |
|    identity and account management data (e.g. User ID, Account Status).          |
|    Admin MUST NOT have access to medical history, diagnoses, consult notes,       |
|    prescriptions, lab reports, private documents, or clinical timelines.          |
|                                                                                   |
| 2. PATIENT CONSENT RULE:                                                          |
|    Doctor / Medical Org access to Timeline, Consults, Prescriptions, Discharge,   |
|    and Personal Uploads REQUIRES active Patient Consent in MySQL `patient_consents`. |
|                                                                                   |
| 3. EMERGENCY OVERRIDE RULE:                                                       |
|    Emergency Role access bypasses standard consent BUT retrieves ONLY vital        |
|    triage parameters AND writes append-only, tamper-resistant audit logs.         |
|                                                                                   |
| 4. DUAL AI BOUNDARY RULE:                                                         |
|    - Patient AI Assistant (Mobile): Assists patient using patient-authorized data.|
|    - Mediverse Healthcare Record Agent (Web): Assists healthcare pros using RAG   |
|      over authorized records under RBAC and consent.                              |
|    - NEITHER AI model may diagnose conditions, prescribe drugs, or override doctors.|
|                                                                                   |
| 5. AUDIT LOGGING RULE:                                                            |
|    All sensitive actions write append-only, tamper-resistant security audit logs. |
+-----------------------------------------------------------------------------------+
```
