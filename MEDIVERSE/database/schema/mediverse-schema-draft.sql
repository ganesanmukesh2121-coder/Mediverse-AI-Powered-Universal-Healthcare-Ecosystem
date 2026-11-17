-- MEDIVERSE DATABASE SCHEMA
-- STATUS: DRAFT
-- NOT APPROVED FOR EXECUTION
-- PHASE 2 DESIGN ONLY

-- ==========================================
-- 1. IDENTITY & AUTHENTICATION MODULE
-- ==========================================

CREATE TABLE roles (
    role_id INT AUTO_INCREMENT NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_name UNIQUE (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE permissions (
    permission_id INT AUTO_INCREMENT NOT NULL,
    permission_key VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    CONSTRAINT pk_permissions PRIMARY KEY (permission_id),
    CONSTRAINT uq_permissions_key UNIQUE (permission_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    user_id CHAR(36) NOT NULL,
    role_id INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NULL,
    account_status ENUM('PENDING', 'ACTIVE', 'REJECTED', 'SUSPENDED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_phone UNIQUE (phone_number),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 2. HEALTH ID & ROLE PROFILE MODULES
-- ==========================================

CREATE TABLE health_id_registry (
    health_id_registry_id BIGINT AUTO_INCREMENT NOT NULL,
    health_id VARCHAR(30) NOT NULL,
    patient_user_id CHAR(36) NOT NULL,
    qr_reference_token VARCHAR(255) NOT NULL,
    issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT pk_health_id_registry PRIMARY KEY (health_id_registry_id),
    CONSTRAINT uq_hir_health_id UNIQUE (health_id),
    CONSTRAINT uq_hir_patient_user UNIQUE (patient_user_id),
    CONSTRAINT fk_hir_patient_user FOREIGN KEY (patient_user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE patient_profiles (
    patient_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    health_id VARCHAR(30) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    blood_group VARCHAR(5) NULL,
    emergency_contact_phone VARCHAR(20) NULL,
    address TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_patient_profiles PRIMARY KEY (patient_id),
    CONSTRAINT uq_pp_user_id UNIQUE (user_id),
    CONSTRAINT uq_pp_health_id UNIQUE (health_id),
    CONSTRAINT fk_pp_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE doctor_profiles (
    doctor_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    license_number VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    hospital_affiliation VARCHAR(255) NULL,
    verification_status ENUM('PENDING', 'ACTIVE', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_doctor_profiles PRIMARY KEY (doctor_id),
    CONSTRAINT uq_dp_user_id UNIQUE (user_id),
    CONSTRAINT uq_dp_license UNIQUE (license_number),
    CONSTRAINT fk_dp_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medical_org_profiles (
    medical_org_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    organization_name VARCHAR(255) NOT NULL,
    registration_number VARCHAR(100) NOT NULL,
    facility_type VARCHAR(50) NOT NULL,
    verification_status ENUM('PENDING', 'ACTIVE', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_medical_org_profiles PRIMARY KEY (medical_org_id),
    CONSTRAINT uq_mop_user_id UNIQUE (user_id),
    CONSTRAINT uq_mop_reg_no UNIQUE (registration_number),
    CONSTRAINT fk_mop_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE laboratory_profiles (
    lab_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    lab_name VARCHAR(255) NOT NULL,
    accreditation_number VARCHAR(100) NOT NULL,
    verification_status ENUM('PENDING', 'ACTIVE', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_laboratory_profiles PRIMARY KEY (lab_id),
    CONSTRAINT uq_lp_user_id UNIQUE (user_id),
    CONSTRAINT uq_lp_accreditation UNIQUE (accreditation_number),
    CONSTRAINT fk_lp_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- NOTE: emergency_profiles stores PATIENT-OWNED emergency medical information attached to patient_profiles.patient_id.
-- It is NOT a profile table for Emergency Responder user accounts (which are authenticated user accounts in users table).
CREATE TABLE emergency_profiles (
    emergency_profile_id BIGINT AUTO_INCREMENT NOT NULL,
    patient_id CHAR(36) NOT NULL,
    severe_allergies TEXT NULL,
    active_critical_medications TEXT NULL,
    chronic_conditions TEXT NULL,
    emergency_notes TEXT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_emergency_profiles PRIMARY KEY (emergency_profile_id),
    CONSTRAINT uq_ep_patient_id UNIQUE (patient_id),
    CONSTRAINT fk_ep_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(patient_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 3. CLINICAL RECORDS, PRESCRIPTIONS & APPOINTMENTS
-- ==========================================

CREATE TABLE medical_records (
    record_id CHAR(36) NOT NULL,
    patient_id CHAR(36) NOT NULL,
    doctor_id CHAR(36) NOT NULL,
    encounter_type ENUM('CONSULTATION', 'ADMISSION', 'EMERGENCY') NOT NULL DEFAULT 'CONSULTATION',
    chief_complaint TEXT NOT NULL,
    diagnosis_notes TEXT NOT NULL,
    encounter_date DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_medical_records PRIMARY KEY (record_id),
    CONSTRAINT fk_mr_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_mr_doctor FOREIGN KEY (doctor_id) REFERENCES doctor_profiles(doctor_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prescriptions (
    prescription_id CHAR(36) NOT NULL,
    patient_id CHAR(36) NOT NULL,
    doctor_id CHAR(36) NOT NULL,
    record_id CHAR(36) NULL,
    prescription_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    general_instructions TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_prescriptions PRIMARY KEY (prescription_id),
    CONSTRAINT fk_rx_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_rx_doctor FOREIGN KEY (doctor_id) REFERENCES doctor_profiles(doctor_id) ON DELETE RESTRICT,
    CONSTRAINT fk_rx_record FOREIGN KEY (record_id) REFERENCES medical_records(record_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prescription_items (
    item_id BIGINT AUTO_INCREMENT NOT NULL,
    prescription_id CHAR(36) NOT NULL,
    medicine_name VARCHAR(150) NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    duration_days INT NOT NULL,
    instructions VARCHAR(255) NULL,
    CONSTRAINT pk_prescription_items PRIMARY KEY (item_id),
    CONSTRAINT fk_pi_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(prescription_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE appointments (
    appointment_id CHAR(36) NOT NULL,
    patient_id CHAR(36) NOT NULL,
    doctor_id CHAR(36) NOT NULL,
    appointment_date DATETIME NOT NULL,
    appointment_type ENUM('IN_PERSON', 'TELECONSULT') NOT NULL DEFAULT 'IN_PERSON',
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') NOT NULL DEFAULT 'SCHEDULED',
    reason TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_appointments PRIMARY KEY (appointment_id),
    CONSTRAINT fk_apt_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_apt_doctor FOREIGN KEY (doctor_id) REFERENCES doctor_profiles(doctor_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 4. DIAGNOSTICS & FILE METADATA MODULE
-- ==========================================

CREATE TABLE file_metadata (
    file_id CHAR(36) NOT NULL,
    patient_id CHAR(36) NOT NULL,
    uploaded_by_user_id CHAR(36) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    supabase_storage_path VARCHAR(500) NOT NULL,
    document_category VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_file_metadata PRIMARY KEY (file_id),
    CONSTRAINT fk_fm_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_fm_user FOREIGN KEY (uploaded_by_user_id) REFERENCES users(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lab_orders (
    order_id CHAR(36) NOT NULL,
    patient_id CHAR(36) NOT NULL,
    lab_id CHAR(36) NOT NULL,
    requesting_doctor_id CHAR(36) NULL,
    test_name VARCHAR(150) NOT NULL,
    order_status ENUM('ORDERED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ORDERED',
    ordered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME NULL,
    CONSTRAINT pk_lab_orders PRIMARY KEY (order_id),
    CONSTRAINT fk_lo_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_lo_lab FOREIGN KEY (lab_id) REFERENCES laboratory_profiles(lab_id) ON DELETE RESTRICT,
    CONSTRAINT fk_lo_doctor FOREIGN KEY (requesting_doctor_id) REFERENCES doctor_profiles(doctor_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medical_reports (
    report_id CHAR(36) NOT NULL,
    patient_id CHAR(36) NOT NULL,
    file_id CHAR(36) NULL,
    report_title VARCHAR(255) NOT NULL,
    report_type ENUM('LAB_REPORT', 'RADIOLOGY_SCAN', 'DISCHARGE_SUMMARY', 'PATIENT_UPLOAD') NOT NULL,
    structured_metrics_json JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_medical_reports PRIMARY KEY (report_id),
    CONSTRAINT fk_mr_patient_rep FOREIGN KEY (patient_id) REFERENCES patient_profiles(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_mr_file FOREIGN KEY (file_id) REFERENCES file_metadata(file_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 5. CONSENT & AUDIT LOG MODULES
-- ==========================================

CREATE TABLE patient_consents (
    consent_id CHAR(36) NOT NULL,
    patient_id CHAR(36) NOT NULL,
    granted_to_user_id CHAR(36) NOT NULL,
    granted_role VARCHAR(20) NOT NULL,
    data_category ENUM('FULL_TIMELINE', 'LAB_ONLY', 'CONSULT_ONLY') NOT NULL DEFAULT 'FULL_TIMELINE',
    status ENUM('ACTIVE', 'EXPIRED', 'REVOKED') NOT NULL DEFAULT 'ACTIVE',
    granted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    CONSTRAINT pk_patient_consents PRIMARY KEY (consent_id),
    CONSTRAINT fk_pc_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_pc_user FOREIGN KEY (granted_to_user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
    audit_id BIGINT AUTO_INCREMENT NOT NULL,
    actor_user_id CHAR(36) NOT NULL,
    actor_role VARCHAR(20) NOT NULL,
    target_patient_id CHAR(36) NULL,
    action_type VARCHAR(50) NOT NULL,
    resource_uri VARCHAR(255) NULL,
    execution_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    security_context_json TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_audit_logs PRIMARY KEY (audit_id),
    INDEX idx_audit_actor (actor_user_id),
    INDEX idx_audit_patient (target_patient_id),
    INDEX idx_audit_action (action_type),
    INDEX idx_audit_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
