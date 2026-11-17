# 05 — DATABASE ENTITY SPECIFICATIONS

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 5.1 SUMMARY OF DESIGNED ENTITIES

The Mediverse schema specifies 19 relational entities:

1. `roles`
2. `permissions`
3. `role_permissions`
4. `users` (Direct 1:1 role association via `role_id`)
5. `health_id_registry`
6. `patient_profiles`
7. `doctor_profiles`
8. `medical_org_profiles`
9. `laboratory_profiles`
10. `emergency_profiles`
11. `medical_records`
12. `prescriptions`
13. `prescription_items`
14. `appointments`
15. `lab_orders`
16. `medical_reports`
17. `patient_consents`
18. `file_metadata`
19. `audit_logs`

---

## 5.2 DETAILED ENTITY SPECIFICATIONS

### 1. `roles` Table
- **Purpose:** Enumerates system roles (`PATIENT`, `EMERGENCY`, `DOCTOR`, `MEDICAL`, `LAB`, `ADMIN`).
- **Primary Key:** `role_id` `INT` (Auto-Increment).
- **Columns:** `role_id`, `role_name` `VARCHAR(50)`, `description` `VARCHAR(255)`.
- **Unique Constraints:** `uq_roles_name` (`role_name`).

### 2. `permissions` Table
- **Purpose:** Granular system permission strings (e.g. `PATIENT_VIEW_OWN_PROFILE`, `DOCTOR_CREATE_CONSULTATION`).
- **Primary Key:** `permission_id` `INT` (Auto-Increment).
- **Columns:** `permission_id`, `permission_key` `VARCHAR(100)`, `description` `VARCHAR(255)`.
- **Unique Constraints:** `uq_permissions_key` (`permission_key`).

### 3. `role_permissions` Table
- **Purpose:** Junction table mapping static permissions to system roles.
- **Primary Key:** Composite (`role_id`, `permission_id`).
- **Foreign Keys:** `role_id` → `roles(role_id)` (`CASCADE`), `permission_id` → `permissions(permission_id)` (`CASCADE`).

### 4. `users` Table
- **Purpose:** Central authentication identity record with direct primary role association.
- **Primary Key:** `user_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `role_id` → `roles(role_id)` (`RESTRICT`).
- **Columns:** `user_id`, `role_id` `INT`, `email` `VARCHAR(255)`, `password_hash` `VARCHAR(255)`, `full_name` `VARCHAR(100)`, `phone_number` `VARCHAR(20)`, `account_status` `ENUM('PENDING', 'ACTIVE', 'REJECTED', 'SUSPENDED')`, `created_at` `DATETIME`, `updated_at` `DATETIME`.
- **Nullable:** `phone_number` nullable.
- **Unique Constraints:** `uq_users_email` (`email`), `uq_users_phone` (`phone_number`).
- **Indexes:** `idx_users_email` (`email`), `idx_users_role` (`role_id`), `idx_users_status` (`account_status`).

### 5. `health_id_registry` Table
- **Purpose:** Authoritative backend registry for server-side generated Mediverse Health IDs.
- **Primary Key:** `health_id_registry_id` `BIGINT` (Auto-Increment).
- **Foreign Keys:** `patient_user_id` → `users(user_id)` (`CASCADE`).
- **Columns:** `health_id_registry_id`, `health_id` `VARCHAR(30)`, `patient_user_id` `CHAR(36)`, `qr_reference_token` `VARCHAR(255)`, `issued_at` `DATETIME`, `status` `ENUM('ACTIVE', 'SUSPENDED')`.
- **Unique Constraints:** `uq_hir_health_id` (`health_id`), `uq_hir_patient_user` (`patient_user_id`).

### 6. `patient_profiles` Table
- **Purpose:** Patient demographic data, emergency contact references, and Health ID mapping.
- **Primary Key:** `patient_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `user_id` → `users(user_id)` (`CASCADE`).
- **Columns:** `patient_id`, `user_id` `CHAR(36)`, `health_id` `VARCHAR(30)`, `date_of_birth` `DATE`, `gender` `ENUM('MALE', 'FEMALE', 'OTHER')`, `blood_group` `VARCHAR(5)`, `emergency_contact_phone` `VARCHAR(20)`, `address` `TEXT`, `created_at` `DATETIME`, `updated_at` `DATETIME`.
- **Unique Constraints:** `uq_patient_user_id` (`user_id`), `uq_patient_health_id` (`health_id`).

### 7. `doctor_profiles` Table
- **Purpose:** Physician credentials, medical license numbers, and specialization.
- **Primary Key:** `doctor_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `user_id` → `users(user_id)` (`CASCADE`).
- **Columns:** `doctor_id`, `user_id` `CHAR(36)`, `license_number` `VARCHAR(100)`, `specialization` `VARCHAR(100)`, `hospital_affiliation` `VARCHAR(255)`, `verification_status` `ENUM('PENDING', 'ACTIVE', 'REJECTED')`, `created_at` `DATETIME`, `updated_at` `DATETIME`.
- **Unique Constraints:** `uq_doctor_license` (`license_number`).

### 8. `medical_org_profiles` Table
- **Purpose:** Hospital and medical organization facility profiles.
- **Primary Key:** `medical_org_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `user_id` → `users(user_id)` (`CASCADE`).
- **Columns:** `medical_org_id`, `user_id` `CHAR(36)`, `organization_name` `VARCHAR(255)`, `registration_number` `VARCHAR(100)`, `facility_type` `VARCHAR(50)`, `verification_status` `ENUM('PENDING', 'ACTIVE', 'REJECTED')`, `created_at` `DATETIME`.

### 9. `laboratory_profiles` Table
- **Purpose:** Diagnostic laboratory facility profiles and accredited license numbers.
- **Primary Key:** `lab_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `user_id` → `users(user_id)` (`CASCADE`).
- **Columns:** `lab_id`, `user_id` `CHAR(36)`, `lab_name` `VARCHAR(255)`, `accreditation_number` `VARCHAR(100)`, `verification_status` `ENUM('PENDING', 'ACTIVE', 'REJECTED')`, `created_at` `DATETIME`.

### 10. `emergency_profiles` Table
- **Purpose:** Stores **Patient-Owned Emergency Information** (blood group, severe allergies, active critical medications, chronic conditions, emergency contact phone, emergency notes). *Note: This table represents the patient's critical triage parameters, NOT a user profile for the Emergency Responder user account.*
- **Primary Key:** `emergency_profile_id` `BIGINT` (Auto-Increment).
- **Foreign Keys:** `patient_id` → `patient_profiles(patient_id)` (`CASCADE`).
- **Columns:** `emergency_profile_id`, `patient_id` `CHAR(36)`, `severe_allergies` `TEXT`, `active_critical_medications` `TEXT`, `chronic_conditions` `TEXT`, `emergency_notes` `TEXT`, `updated_at` `DATETIME`.

### 11. `medical_records` Table
- **Purpose:** Clinical consultation encounters, chief complaints, symptoms, and diagnoses.
- **Primary Key:** `record_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `patient_id` → `patient_profiles(patient_id)` (`RESTRICT`), `doctor_id` → `doctor_profiles(doctor_id)` (`RESTRICT`).
- **Columns:** `record_id`, `patient_id` `CHAR(36)`, `doctor_id` `CHAR(36)`, `encounter_type` `ENUM('CONSULTATION', 'ADMISSION', 'EMERGENCY')`, `chief_complaint` `TEXT`, `diagnosis_notes` `TEXT`, `encounter_date` `DATETIME`, `created_at` `DATETIME`.

### 12. `prescriptions` Table
- **Purpose:** Master digital prescription header issued by a doctor.
- **Primary Key:** `prescription_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `patient_id` → `patient_profiles(patient_id)` (`RESTRICT`), `doctor_id` → `doctor_profiles(doctor_id)` (`RESTRICT`), `record_id` → `medical_records(record_id)` (`SET NULL`).
- **Columns:** `prescription_id`, `patient_id` `CHAR(36)`, `doctor_id` `CHAR(36)`, `record_id` `CHAR(36)`, `prescription_date` `DATETIME`, `general_instructions` `TEXT`, `created_at` `DATETIME`.

### 13. `prescription_items` Table
- **Purpose:** Individual medication items attached to a digital prescription.
- **Primary Key:** `item_id` `BIGINT` (Auto-Increment).
- **Foreign Keys:** `prescription_id` → `prescriptions(prescription_id)` (`CASCADE`).
- **Columns:** `item_id`, `prescription_id` `CHAR(36)`, `medicine_name` `VARCHAR(150)`, `dosage` `VARCHAR(50)`, `frequency` `VARCHAR(50)`, `duration_days` `INT`, `instructions` `VARCHAR(255)`.

### 14. `appointments` Table
- **Purpose:** Doctor and patient consultation appointment scheduling.
- **Primary Key:** `appointment_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `patient_id` → `patient_profiles(patient_id)` (`RESTRICT`), `doctor_id` → `doctor_profiles(doctor_id)` (`RESTRICT`).
- **Columns:** `appointment_id`, `patient_id` `CHAR(36)`, `doctor_id` `CHAR(36)`, `appointment_date` `DATETIME`, `appointment_type` `ENUM('IN_PERSON', 'TELECONSULT')`, `status` `ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')`, `reason` `TEXT`, `created_at` `DATETIME`, `updated_at` `DATETIME`.

### 15. `lab_orders` Table
- **Purpose:** Diagnostic laboratory test requisitions.
- **Primary Key:** `order_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `patient_id` → `patient_profiles(patient_id)` (`RESTRICT`), `lab_id` → `laboratory_profiles(lab_id)` (`RESTRICT`), `requesting_doctor_id` → `doctor_profiles(doctor_id)` (`SET NULL`).
- **Columns:** `order_id`, `patient_id` `CHAR(36)`, `lab_id` `CHAR(36)`, `requesting_doctor_id` `CHAR(36)`, `test_name` `VARCHAR(150)`, `order_status` `ENUM('ORDERED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')`, `ordered_at` `DATETIME`, `completed_at` `DATETIME`.

### 16. `medical_reports` Table
- **Purpose:** Links lab test results and uploaded medical reports to patient health timeline.
- **Primary Key:** `report_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `patient_id` → `patient_profiles(patient_id)` (`CASCADE`), `file_id` → `file_metadata(file_id)` (`SET NULL`).
- **Columns:** `report_id`, `patient_id` `CHAR(36)`, `file_id` `CHAR(36)`, `report_title` `VARCHAR(255)`, `report_type` `ENUM('LAB_REPORT', 'RADIOLOGY_SCAN', 'DISCHARGE_SUMMARY', 'PATIENT_UPLOAD')`, `structured_metrics_json` `JSON`, `created_at` `DATETIME`.

### 17. `patient_consents` Table
- **Purpose:** Explicit patient access consent permissions granted to providers.
- **Primary Key:** `consent_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `patient_id` → `patient_profiles(patient_id)` (`CASCADE`), `granted_to_user_id` → `users(user_id)` (`CASCADE`).
- **Columns:** `consent_id`, `patient_id` `CHAR(36)`, `granted_to_user_id` `CHAR(36)`, `granted_role` `VARCHAR(20)`, `data_category` `ENUM('FULL_TIMELINE', 'LAB_ONLY', 'CONSULT_ONLY')`, `status` `ENUM('ACTIVE', 'EXPIRED', 'REVOKED')`, `granted_at` `DATETIME`, `expires_at` `DATETIME`, `revoked_at` `DATETIME`.

### 18. `file_metadata` Table
- **Purpose:** Metadata records for binary files stored in private Supabase Storage buckets.
- **Primary Key:** `file_id` `CHAR(36)` (UUID v4).
- **Foreign Keys:** `patient_id` → `patient_profiles(patient_id)` (`CASCADE`), `uploaded_by_user_id` → `users(user_id)` (`RESTRICT`).
- **Columns:** `file_id`, `patient_id` `CHAR(36)`, `uploaded_by_user_id` `CHAR(36)`, `original_file_name` `VARCHAR(255)`, `mime_type` `VARCHAR(100)`, `file_size_bytes` `BIGINT`, `supabase_storage_path` `VARCHAR(500)`, `document_category` `VARCHAR(50)`, `created_at` `DATETIME`.

### 19. `audit_logs` Table
- **Purpose:** Append-only, tamper-resistant security audit log entries.
- **Primary Key:** `audit_id` `BIGINT` (Auto-Increment).
- **Foreign Keys:** None (hard decoupling for tamper resistance).
- **Columns:** `audit_id`, `actor_user_id` `CHAR(36)`, `actor_role` `VARCHAR(20)`, `target_patient_id` `CHAR(36)`, `action_type` `VARCHAR(50)`, `resource_uri` `VARCHAR(255)`, `execution_status` `VARCHAR(20)`, `ip_address` `VARCHAR(45)`, `user_agent` `VARCHAR(255)`, `security_context_json` `TEXT`, `created_at` `DATETIME`.
