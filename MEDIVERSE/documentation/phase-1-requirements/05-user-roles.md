# 05 — USER ROLES AND PORTALS

## 5.1 OVERVIEW OF SYSTEM ROLES

Mediverse defines six distinct roles operating across six specialized portals. Every user in the system is assigned exactly one primary role, which dictates their access permissions, portal interface, and authorization boundaries.

```
+-----------------------------------------------------------------------------------+
|                                 MEDIVERSE ROLES                                   |
+-------------------+--------------------+-------------------+----------------------+
| ROLE              | FRONTEND PLATFORM  | PORTAL TYPE       | CORE FOCUS           |
+-------------------+--------------------+-------------------+----------------------+
| 1. PATIENT        | Mobile (Flutter)   | Patient Portal    | Personal Health & ID |
| 2. EMERGENCY      | Mobile (Flutter)   | Emergency Portal  | Vital Triage Info    |
| 3. DOCTOR         | Web (React)        | Doctor Portal     | Consults & Rx        |
| 4. MEDICAL        | Web (React)        | Medical Org Portal| Admissions & Care    |
| 5. LAB            | Web (React)        | Laboratory Portal | Diagnostic Reports   |
| 6. ADMIN          | Web (React)        | Admin Portal      | Verification & Ops   |
+-------------------+--------------------+-------------------+----------------------+
```

---

## 5.2 DETAILED ROLE SPECIFICATIONS

### ROLE 1 — PATIENT

- **Platform:** Flutter Mobile Application
- **Target User:** Individual patients seeking digital control over personal health data.
- **Responsibilities:**
  - Register account and receive backend-generated Mediverse Health ID.
  - Maintain personal demographic details and emergency information.
  - Upload personal medical files (prescriptions, discharge papers, lab reports, imaging scans).
  - View chronological health timeline aggregated from all healthcare providers.
  - Grant, review, and modify access consent permissions for doctors and organizations.
  - View personal data access logs (see which provider accessed records).
  - Interact with the **Patient AI Assistant** for self-service personal record explanations.
  - Manage emergency information.
- **Access Privileges:** Full read/write access to personal profile, documents, and consent settings. Read-only access to provider-created clinical notes and lab results.
- **Security Boundary:** Patient cannot alter provider-authored clinical diagnoses, lab report values, or official prescriptions. Cannot view administrative audit logs of other users.

---

### ROLE 2 — EMERGENCY

- **Platform:** Flutter Mobile Application
- **Target User:** First responders, paramedic staff, emergency room triage nurses, ambulance personnel.
- **Responsibilities:**
  - Authenticate securely via designated emergency access credentials.
  - Scan patient emergency QR code or enter Mediverse Health ID under emergency override.
  - Rapidly retrieve critical, life-saving health parameters.
  - Review emergency contacts, severe drug allergies, blood group, and active high-risk medications.
- **Access Privileges:** Read-only access restricted strictly to emergency profile parameters.
- **Security Boundary:** **Emergency role does NOT grant unrestricted access to full clinical records, historical prescriptions, or private clinical notes.** All emergency sessions trigger append-only, tamper-resistant audit logs.

---

### ROLE 3 — DOCTOR

- **Platform:** Web Portal (React)
- **Target User:** Licensed medical practitioners, general physicians, specialist doctors.
- **Responsibilities:**
  - Securely log in and maintain active provider verification status.
  - Identify patients using Health ID or QR code lookup reference.
  - View authorized patient medical history, past prescriptions, and diagnostic lab reports.
  - Record consultation encounters: clinical notes, symptoms, physical examination findings, diagnosis.
  - Issue official digital prescriptions (drug name, dosage, frequency, instructions).
  - Upload relevant consultation documents or clinical images.
  - Request AI-generated patient history summaries from the **Mediverse Healthcare Record Agent**.
- **Access Privileges:** Read/write access to consultation notes and prescriptions. Read access to patient health timeline under active authorization/consent.
- **Security Boundary:** Access restricted to authorized/consented patients. Cannot modify lab-published reports or hospital administrative records. Cannot perform admin tasks.

---

### ROLE 4 — MEDICAL (MEDICAL ORGANIZATION / HOSPITAL)

- **Platform:** Web Portal (React)
- **Target User:** Hospitals, outpatient clinics, surgical centers, healthcare organizations.
- **Responsibilities:**
  - Organization login and security credentials management.
  - Patient lookup for hospital admissions and inpatient management.
  - Log inpatient admission records, attending physician assignments, and room metadata.
  - Create and publish official hospital discharge summaries.
  - Upload hospital clinical documents (surgical reports, consent forms, discharge PDFs).
  - Manage internal organization staff accounts.
- **Access Privileges:** Read/write access to organizational admission, treatment, and discharge records. Read access to patient background history under organization consent.
- **Security Boundary:** Cannot modify independent external lab reports or private external doctor consults. Access requires organizational consent binding.

---

### ROLE 5 — LAB (DIAGNOSTIC LABORATORY)

- **Platform:** Web Portal (React)
- **Target User:** Diagnostic centers, pathology labs, radiology centers, clinical technicians.
- **Responsibilities:**
  - Secure lab technician/organization login.
  - Patient lookup via Mediverse Health ID.
  - Register new diagnostic test requisitions.
  - Upload official lab report files (PDFs, high-resolution radiology images).
  - Input structured numerical test results (e.g., blood glucose levels, lipid profile metrics).
  - Link published reports directly to the patient's digital health timeline.
- **Access Privileges:** Read/write access to lab test orders and lab report uploads.
- **Security Boundary:** **Lab users DO NOT receive unrestricted access to the patient's entire medical history, past doctor consultation notes, or hospital discharge summaries.** Access is strictly purpose-bound to laboratory testing workflows.

---

### ROLE 6 — ADMIN (SYSTEM ADMINISTRATOR)

- **Platform:** Web Portal (React)
- **Target User:** System security administrators, verification officers, platform operators.
- **Responsibilities:**
  - Authenticate securely via multi-factor administrative authentication.
  - Review provider registration applications for Doctors, Medical Organizations, and Labs.
  - Verify credentials and execute status changes (`PENDING` → `ACTIVE` / `REJECTED`).
  - Activate, deactivate, or suspend user accounts involved in suspicious activities.
  - Monitor global system security dashboard and inspect append-only, tamper-resistant audit logs.
  - Manage global platform configurations.
- **Access Privileges:** Full read/write access to user identity records, verification queues, account statuses, and system audit logs. READ access to Patient Profile is strictly LIMITED to administrative identity/account info required for account management.
- **Security Boundary:** **Admin MUST NOT have access to medical history, diagnoses, consultation notes, digital prescriptions, laboratory reports, private medical documents, or clinical timelines.** Admin operations are strictly limited to identity, security, verification, and platform management.

---

## 5.3 SUMMARY PERMISSION MATRIX

| Permission / Action | Patient | Emergency | Doctor | Medical | Lab | Admin |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Register & Receive Health ID** | ✅ Self | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Account Identity Info** | ✅ Own | 🔒 Vitals | ✅ Consented | ✅ Consented | 🔒 Order | 🔒 Admin Identity Only |
| **View Full Clinical Timeline** | ✅ Own | ❌ | 🔒 Consented | 🔒 Consented | ❌ | ❌ DENIED |
| **View Emergency Profile** | ✅ Own | ✅ Audited | ✅ Consented | ✅ Consented | ❌ | ❌ DENIED |
| **Add Consult Note / Rx** | ❌ | ❌ | ✅ Own | ❌ | ❌ | ❌ DENIED |
| **Add Admission / Discharge** | ❌ | ❌ | ❌ | ✅ Own | ❌ | ❌ DENIED |
| **Upload Lab Report / Results** | ❌ | ❌ | ❌ | ❌ | ✅ Own | ❌ DENIED |
| **Upload Personal Document** | ✅ Own | ❌ | ✅ Clinical | ✅ Hospital | ❌ | ❌ DENIED |
| **Manage Patient Consent** | ✅ Own | ❌ | ❌ | ❌ | ❌ | ❌ DENIED |
| **Verify Provider Accounts** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ System |
| **View System Audit Logs** | 🔒 Own Log | ❌ | ❌ | ❌ | ❌ | ✅ Global |
| **Patient AI Assistant** | ✅ Patient | ❌ | ❌ | ❌ | ❌ | ❌ DENIED |
| **Healthcare Record Agent (RAG)**| ❌ | ❌ | 🔒 Consented | 🔒 Consented | ❌ | ❌ DENIED |
| **View Private Clinical Files** | ✅ Own | ❌ | 🔒 Consented | 🔒 Consented | 🔒 Lab-only | ❌ DENIED |
