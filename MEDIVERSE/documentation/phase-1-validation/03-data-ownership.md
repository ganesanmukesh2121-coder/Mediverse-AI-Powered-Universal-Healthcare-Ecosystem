# 03 — DATA OWNERSHIP MODEL

## 3.1 CONCEPTUAL DATA CATEGORIZATION

The Mediverse ecosystem enforces clear data ownership and authorial boundaries. Conceptually, all system information falls into seven distinct ownership categories:

```
+-----------------------------------------------------------------------------------+
|                            DATA OWNERSHIP CATEGORIES                              |
+-----------------------------------------------------------------------------------+
| 1. PATIENT-OWNED DATA           -> Profiles, Uploaded Files, Consent Rules         |
| 2. DOCTOR-CREATED DATA          -> Consult Notes, Diagnoses, E-Prescriptions        |
| 3. MEDICAL-ORG-CREATED DATA     -> Hospital Admissions, Inpatient Care, Discharge   |
| 4. LAB-CREATED DATA             -> Test Requisitions, Structured Metrics, PDF Reports|
| 5. EMERGENCY INFORMATION        -> Blood Group, Critical Allergies, Core Vitals    |
| 6. ADMINISTRATIVE & SECURITY    -> User Identity, Provider Status, Audit Logs      |
| 7. AI-GENERATED DERIVED INFO    -> Source-Grounded Summaries, OCR Extracts, Vectors|
+-----------------------------------------------------------------------------------+
```

---

## 3.2 DETAILED OWNERSHIP BREAKDOWN

### 1. Patient-Owned Data
- **Entities:** Patient Demographic Profile, Personal Medical File Uploads (PDFs/Images), Access Consent Grants & Revocations, Personal Health ID Reference.
- **Ownership Attributes:** Owned entirely by the Patient. Patient has full sovereignty to manage profile details, upload personal records, and configure consent permissions for external healthcare providers.

### 2. Doctor-Created Data
- **Entities:** Clinical Consultation Notes, Chief Complaints, Diagnostic Findings, Digital E-Prescriptions, Doctor-Attached Clinical Documents.
- **Ownership Attributes:** Authored and digitally signed by the attending Doctor. Once published, clinical notes and prescriptions become immutable parts of the patient's healthcare record. Patients have read-only access to their prescriptions and consultation history.

### 3. Medical-Organization-Created Data
- **Entities:** Hospital Admission Records, Department/Ward Assignments, Inpatient Clinical Updates, Hospital Discharge Summaries, Surgical Notes.
- **Ownership Attributes:** Authored by authorized Medical Organization staff. Discharge summaries become immutable timeline events once published. Patients can view discharge summaries under patient timeline access.

### 4. Lab-Created Data
- **Entities:** Diagnostic Test Orders, Structured Metric Results (e.g., Blood Glucose, Lipid Metrics), Signed Diagnostic PDF Reports.
- **Ownership Attributes:** Authored and verified by licensed Diagnostic Laboratory personnel. Diagnostic reports are linked directly to the patient's Health ID. Once finalized, lab reports are immutable.

### 5. Emergency Information
- **Entities:** Emergency Contacts, Blood Group, Severe Drug Allergies, Active High-Risk Medications, Core Chronic Conditions.
- **Ownership Attributes:** Configured primarily by the Patient (or authorized clinical intake). Maintained as a specialized, rapid-access triage subset accessible to accredited Emergency personnel under mandatory append-only audit logging.

### 6. Administrative & Security Data
- **Entities:** User Identity Accounts, Hashed Password Credentials, Provider Verification Applications, Account Statuses (`PENDING`, `ACTIVE`, `REJECTED`), Global System Security Audit Logs.
- **Ownership Attributes:** Managed exclusively by System Administrators. Security audit logs are append-only, tamper-resistant records owned by the system security engine. Admin access to Patient Profiles is strictly LIMITED to administrative identity/account info required for account management (User ID, account status, email). Admin MUST NOT have access to clinical medical history, diagnoses, consultation notes, prescriptions, lab reports, or clinical timelines.

### 7. AI-Generated Derived Information
- **Entities:** Patient AI Assistant Explanations (Mobile), Healthcare Record Agent RAG Summaries (Web Doctor Portal), Vector Embeddings, OCR Extracted Text Chunks.
- **Ownership Attributes:** Derived automatically by the dual AI engine from authorized source documents. AI summaries carry no independent legal authority and exist as transient or cached assistive views.

---

## 3.3 DATA LIFECYCLE & RELATIONSHIP MODEL

```
+-----------------------------------------------------------------------------------+
|                        CONCEPTUAL DATA RELATIONSHIPS                              |
+-----------------------------------------------------------------------------------+
|                                 [ PATIENT ]                                       |
|                                      |                                            |
|    +---------------------------------+---------------------------------+          |
|    |                                 |                                 |          |
|    v                                 v                                 v          |
| [ Patient Profile ]          [ Health Timeline ]             [ Emergency Info ]   |
| (Patient-Owned)              (Aggregated View)               (Vitals Subset)      |
|                                      ^                                            |
|         +----------------------------+----------------------------+               |
|         |                            |                            |               |
|         v                            v                            v               |
| [ Consults & Rx ]          [ Admissions & Discharge ]     [ Lab Test Reports ]    |
| (Doctor-Created)           (Medical-Org-Created)          (Lab-Created)           |
+-----------------------------------------------------------------------------------+
```

- **Immutability Principle:** Provider-authored clinical records (Doctor consults, prescriptions, hospital discharge summaries, lab reports) become **immutable** upon publication. Providers cannot arbitrarily alter or delete published clinical history.
- **Timeline Integration:** All data items—whether Patient-uploaded, Doctor-authored, Medical-Org-created, or Lab-published—are linked conceptually to the central Patient Health Timeline indexed by the persistent **Mediverse Health ID**.
