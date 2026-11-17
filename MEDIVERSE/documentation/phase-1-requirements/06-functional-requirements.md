# 06 — FUNCTIONAL REQUIREMENTS

## 6.1 REQUIREMENT IDENTIFICATION CONVENTION

Functional requirements are uniquely formatted as `FR-[MODULE]-[NUMBER]` (e.g., `FR-HID-01`, `FR-DOC-02`).

---

## 6.2 SYSTEM IDENTITY & HEALTH ID GENERATION (FR-HID)

```
+-----------------------------------------------------------------------------------+
|                        HEALTH ID GENERATION ARCHITECTURE                          |
+-----------------------------------------------------------------------------------+
| Patient Registration Request -> Spring Boot API Validation -> BCrypt Password Hash|
| -> DB User Creation -> Unique Health ID Generation Service -> Save to MySQL       |
| -> Return JWT + Health ID to Mobile Client                                        |
+-----------------------------------------------------------------------------------+
```

- **FR-HID-01 (Backend Generation):** The Mediverse Health ID must be generated exclusively by the Spring Boot backend service during patient registration. Frontend clients (Flutter/React) are strictly prohibited from generating Health IDs.
- **FR-HID-02 (Uniqueness & Immutability):** The Health ID must be globally unique, persistent, immutable after creation, and protected by a database unique index constraint in MySQL.
- **FR-HID-03 (Identifier Decoupling):** The system must maintain a strict conceptual and database distinction between internal surrogate database primary keys (`user_id`) and public `health_id`.
- **FR-HID-04 (No Direct Authorization):** The Health ID serves strictly as a patient lookup reference. Presenting a Health ID alone must NOT grant automatic authorization to view private clinical records.
- **FR-HID-05 (QR Reference Generation):** The system must support generating a secure QR code containing an encrypted/signed Health ID reference token. The QR code must NEVER contain raw clinical history, diagnoses, or prescriptions.

---

## 6.3 PATIENT PROFILE & TIMELINE MANAGEMENT (FR-PAT)

- **FR-PAT-01 (Registration & Login):** Patients must be able to register via the Flutter mobile app by providing full name, email, phone number, date of birth, and password.
- **FR-PAT-02 (Profile Management):** Patients must be able to view and update demographic information, emergency contacts, blood group, and known allergies.
- **FR-PAT-03 (Unified Timeline View):** The mobile app must provide a chronological health timeline aggregating all past doctor consultations, digital prescriptions, lab test reports, hospital admissions, discharge summaries, and user uploads.
- **FR-PAT-04 (Document Upload):** Patients must be able to upload medical documents (PDFs, JPEGs, PNGs) from their mobile device, tagging them with categories (e.g., Prescription, Lab Report, Discharge Summary, Imaging).
- **FR-PAT-05 (Consent Management Panel):** Patients must be able to view active access permissions granted to healthcare providers, approve new access requests, and revoke existing permissions.
- **FR-PAT-06 (Personal Audit View):** Patients must be able to view a simplified access history log showing which provider accessed their profile, when, and for what purpose.
- **FR-PAT-07 (Patient AI Assistant):** Patients must be able to query the Patient AI Assistant on the mobile app to explain personal records and navigate timeline context within patient-authorized boundaries.

---

## 6.4 EMERGENCY ACCESS ENGINE (FR-EMG)

- **FR-EMG-01 (Controlled Triage Access):** The Emergency Flutter portal must allow authorized first responders to look up a patient via Health ID or emergency QR code reference.
- **FR-EMG-02 (Minimal Vital Parameters):** Emergency access must return ONLY critical life-saving parameters: blood group, severe allergies, active high-risk medications, chronic conditions, and emergency contact numbers.
- **FR-EMG-03 (Clinical Exclusion):** Emergency access must strictly exclude non-emergency clinical consultation notes, mental health records, full historical lab trends, and private financial documents.
- **FR-EMG-04 (Mandatory Emergency Audit):** Every emergency lookup must write an append-only, tamper-resistant audit log recording responder ID, patient Health ID, timestamp, device metadata, and access trigger rationale.

---

## 6.5 DOCTOR CLINICAL CONSULTATION (FR-DOC)

- **FR-DOC-01 (Provider Authentication):** Doctors must authenticate via the React Web Portal using email/password + JWT. Only doctors with `ACTIVE` verification status are permitted to execute clinical operations.
- **FR-DOC-02 (Patient Lookup & Authorization Check):** Doctors look up patients via Health ID. The backend must enforce a consent check before returning clinical records.
- **FR-DOC-03 (Consultation Entry):** Doctors must be able to record consultation sessions, entering chief complaints, symptoms, physical examination findings, clinical diagnosis, and follow-up advice.
- **FR-DOC-04 (Digital E-Prescription Generation):** Doctors must be able to create structured digital prescriptions containing drug name, dosage, administration route, frequency, duration, and specific instructions.
- **FR-DOC-05 (Clinical Document Upload):** Doctors must be able to attach clinical documents, ECG tracings, or diagnostic imaging directly to the consultation encounter.
- **FR-DOC-06 (AI Healthcare Record Agent Trigger):** Doctors must be able to request an AI-generated record synthesis from the **Mediverse Healthcare Record Agent** for authorized patients.

---

## 6.6 MEDICAL ORGANIZATION & ADMISSIONS (FR-MED)

- **FR-MED-01 (Organization Portal Login):** Authorized hospital administrators/staff log into the Medical Web Portal.
- **FR-MED-02 (Inpatient Admission Recording):** Staff can record patient admissions, specifying admission timestamp, admitting department, attending physician, room/bed metadata, and initial diagnosis.
- **FR-MED-03 (Discharge Summary Publication):** Hospital staff can compose and publish official digital discharge summaries, attaching discharge instructions, medication changes, and follow-up schedules.
- **FR-MED-04 (Hospital Document Upload):** Support uploading hospital-generated PDFs (surgical notes, consent forms, discharge papers) into the patient's timeline.

---

## 6.7 LABORATORY TEST & REPORT MANAGEMENT (FR-LAB)

- **FR-LAB-01 (Lab Authentication):** Diagnostic lab staff log into the Lab Web Portal.
- **FR-LAB-02 (Test Requisition Creation):** Lab staff register lab orders linked to a patient's Health ID.
- **FR-LAB-03 (Structured Result Entry):** Lab technicians enter structured numerical test parameters (e.g., Hemoglobin: 14.2 g/dL, Fasting Blood Glucose: 95 mg/dL) alongside reference ranges.
- **FR-LAB-04 (Diagnostic PDF Upload):** Upload signed diagnostic report PDFs directly to Supabase Storage, linking file metadata to the lab test record.
- **FR-LAB-05 (Data Minimization):** Lab users must NOT be granted access to doctor consultation notes, mental health records, or hospital discharge summaries outside diagnostic scope.

---

## 6.8 ADMINISTRATIVE PROVIDER VERIFICATION & SYSTEM OPERATIONS (FR-ADM)

```
+-----------------------------------------------------------------------------------+
|                        PROVIDER VERIFICATION LIFECYCLE                            |
+-----------------------------------------------------------------------------------+
| Provider Registration -> Status: PENDING -> Admin Inspects Licenses & Credentials |
| -> Admin Approves: Status ACTIVE  OR  Admin Rejects: Status REJECTED              |
+-----------------------------------------------------------------------------------+
```

- **FR-ADM-01 (Admin Security Login):** Administrators authenticate via secure Web Portal with administrative RBAC credentials.
- **FR-ADM-02 (Provider Verification Queue):** Admin panel must display a pending queue of newly registered Doctors, Medical Organizations, and Diagnostic Labs.
- **FR-ADM-03 (Lifecycle Status Management):** Admin must be able to update provider registration statuses:
  - `PENDING`: Awaiting credential review.
  - `ACTIVE`: Verified; full portal access granted.
  - `REJECTED`: Credentials invalid; access denied.
  - `SUSPENDED`: Temporarily deactivated due to security review.
- **FR-ADM-04 (Account Status Controls):** Admin can suspend or reactivate any user account.
- **FR-ADM-05 (Audit Log Dashboard):** Admin can query, filter, and inspect system audit logs by date, user ID, role, action type, and status code.
- **FR-ADM-06 (No Clinical Access Boundary):** Admin READ access to Patient Profile is strictly LIMITED to administrative identity/account info (User ID, status flags, email) required for account management. Admin MUST NOT have access to medical history, diagnoses, consultation notes, prescriptions, laboratory reports, private medical documents, or clinical timelines.

---

## 6.9 DIGITAL DOCUMENT STORAGE & METADATA (FR-STR)

- **FR-STR-01 (Supabase Storage Upload):** All binary files (PDFs, JPEGs, PNGs) must be uploaded directly to private object buckets in Supabase Storage via backend API orchestration.
- **FR-STR-02 (MySQL Metadata Linkage):** For every uploaded file, Spring Boot must record structured metadata in MySQL: `file_id`, `patient_id`, `uploader_id`, `uploader_role`, `original_file_name`, `mime_type`, `file_size_bytes`, `supabase_storage_path`, `document_category`, and `created_at`.
- **FR-STR-03 (Private Storage Security):** Supabase buckets must remain private. Clients access files strictly through short-lived pre-signed URLs or Spring Boot backend streaming proxies.

---

## 6.10 CONSENT & AUDIT LOGGING ENGINES (FR-CNS / FR-AUD)

- **FR-CNS-01 (Explicit Consent Verification):** The Spring Boot backend must verify active patient consent before releasing clinical records to Doctors or Medical Organizations.
- **FR-AUD-01 (Mandatory Append-Only Audit Capture):** The backend must capture append-only, tamper-resistant audit logs for all sensitive operations (`LOGIN`, `FAILED_LOGIN`, `PATIENT_LOOKUP`, `RECORD_VIEW`, `DOCUMENT_UPLOAD`, `DOCUMENT_DOWNLOAD`, `PRESCRIPTION_CREATE`, `EMERGENCY_OVERRIDE`, `PROVIDER_VERIFY`).
- **FR-AUD-02 (Audit Fields):** Every audit entry must record: `audit_id`, `actor_user_id`, `actor_role`, `target_patient_id`, `action_type`, `resource_id`, `execution_status`, `ip_address`, `user_agent`, and `timestamp`.
