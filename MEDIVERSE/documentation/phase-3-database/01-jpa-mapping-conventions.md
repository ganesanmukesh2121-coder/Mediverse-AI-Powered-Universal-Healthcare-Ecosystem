# 01 — JPA IMPLEMENTATION CONVENTIONS FOR MEDIVERSE

> **PHASE 3 DATABASE SPECIFICATION — DESIGN CONVENTIONS FOR JPA ENTITY IMPLEMENTATION**

---

## 1. OVERVIEW

This document establishes the mandatory JPA mapping rules and conventions for all 19 relational entities specified in the approved Phase 2 Mediverse architecture. All future JPA entities created in `com.mediverse.entity` must strictly follow these rules.

---

## 2. PRIMARY KEY (ID) MAPPING CONVENTIONS

1. **`CHAR(36)` UUID Primary Keys**
   - **Java Type:** `String`
   - **Annotation:** `@Id`, `@GeneratedValue(strategy = GenerationType.UUID)`
   - **Column Specification:** `@Column(name = "<id_column>", length = 36, columnDefinition = "CHAR(36)", nullable = false, updatable = false)`
   - **Applies to:** `users`, `patient_profiles`, `doctor_profiles`, `medical_org_profiles`, `laboratory_profiles`, `medical_records`, `prescriptions`, `appointments`, `lab_orders`, `medical_reports`, `patient_consents`, `file_metadata`.

2. **`BIGINT` Auto-Increment Primary Keys**
   - **Java Type:** `Long`
   - **Annotation:** `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`
   - **Column Specification:** `@Column(name = "<id_column>", nullable = false, updatable = false)`
   - **Applies to:** `health_id_registry`, `emergency_profiles`, `prescription_items`, `audit_logs`.

3. **`INT` Auto-Increment Primary Keys**
   - **Java Type:** `Integer`
   - **Annotation:** `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`
   - **Column Specification:** `@Column(name = "<id_column>", nullable = false, updatable = false)`
   - **Applies to:** `roles`, `permissions`.

4. **Composite Primary Keys (`role_permissions`)**
   - **Java Structure:** `@Embeddable` class `RolePermissionId` containing `roleId` (`Integer`) and `permissionId` (`Integer`).
   - **Entity Mapping:** `@EmbeddedId` in `RolePermission` entity.

---

## 3. TABLE & COLUMN MAPPING CONVENTIONS

- **Entity Annotation:** `@Entity`
- **Table Annotation:** Explicit `@Table(name = "<snake_case_table_name>")`
- **Field Naming:** Java `camelCase` mapped explicitly to MySQL `snake_case` column names using `@Column(name = "...")`.
- **Length & Nullability:** Explicit `@Column(name = "...", nullable = false/true, length = ...)` matching approved schema.

---

## 4. RELATIONSHIP CONVENTIONS

1. **Lazy Loading Default:** All relationship annotations (`@ManyToOne`, `@OneToOne`, `@OneToMany`, `@ManyToMany`) must specify `fetch = FetchType.LAZY`.
2. **`@OneToOne` Mappings:**
   - Profile extension tables (`patient_profiles`, `doctor_profiles`, `medical_org_profiles`, `laboratory_profiles`, `health_id_registry`) link to `User` via `@OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)`.
   - `emergency_profiles` links to `PatientProfile` via `@OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "patient_id", nullable = false)`.
   - `medical_reports` links optionally to `FileMetadata` via `@OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "file_id")`.
3. **`@ManyToOne` & `@OneToMany` Mappings:**
   - Standard parent-child clinical relationships (`patient_profiles` ── `medical_records`, `appointments`, `prescriptions`, `lab_orders`, `medical_reports`, `patient_consents`, `file_metadata`).
   - `prescriptions` ── `prescription_items` uses `@OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)`.
4. **`role_permissions` Junction Mapping:**
   - Mapped explicitly as a dedicated entity `RolePermission` with `@EmbeddedId` and `@ManyToOne @MapsId("roleId")` / `@MapsId("permissionId")`.

---

## 5. ENUM MAPPING CONVENTIONS

All status and enumeration fields must be mapped using `@Enumerated(EnumType.STRING)` using exact approved values:

- `AccountStatus`: `PENDING`, `ACTIVE`, `REJECTED`, `SUSPENDED`
- `HealthIdStatus`: `ACTIVE`, `SUSPENDED`
- `Gender`: `MALE`, `FEMALE`, `OTHER`
- `VerificationStatus`: `PENDING`, `ACTIVE`, `REJECTED`
- `EncounterType`: `CONSULTATION`, `ADMISSION`, `EMERGENCY`
- `AppointmentType`: `IN_PERSON`, `TELECONSULT`
- `AppointmentStatus`: `SCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`
- `OrderStatus`: `ORDERED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
- `ReportType`: `LAB_REPORT`, `RADIOLOGY_SCAN`, `DISCHARGE_SUMMARY`, `PATIENT_UPLOAD`
- `DataCategory`: `FULL_TIMELINE`, `LAB_ONLY`, `CONSULT_ONLY`
- `ConsentStatus`: `ACTIVE`, `EXPIRED`, `REVOKED`

---

## 6. TIMESTAMP & DATE CONVENTIONS

- **`DATETIME` Columns:** Mapped to Java `java.time.LocalDateTime` (`created_at`, `updated_at`, `issued_at`, `encounter_date`, `prescription_date`, `appointment_date`, `ordered_at`, `completed_at`, `granted_at`, `expires_at`, `revoked_at`).
- **`DATE` Columns:** Mapped to Java `java.time.LocalDate` (`date_of_birth`).
- No additional timestamp or date fields may be added beyond the approved schema.

---

## 7. CONSTRAINT MAPPING CONVENTIONS

- **UNIQUE Constraints:** Defined at entity table level via `@Table(name = "...", uniqueConstraints = { @UniqueConstraint(name = "<uq_name>", columnNames = { ... }) })` or field level `@Column(unique = true)`.
- **NOT NULL Constraints:** Defined explicitly via `@Column(nullable = false)`.
- **Foreign Keys:** Mapped via `@JoinColumn(name = "<fk_column>", foreignKey = @ForeignKey(name = "fk_..."))`.

---

## 8. AUDIT LOG CONVENTION (CRITICAL)

- `audit_logs` table fields `actor_user_id` (`CHAR(36)`) and `target_patient_id` (`CHAR(36)`) **MUST** be mapped as plain Java `String` fields (`@Column(name = "actor_user_id", length = 36)`).
- **DO NOT** create `@ManyToOne` foreign key annotations referencing `User` or `PatientProfile` in the `AuditLog` entity. Audit logs must remain hard-decoupled and immutable.

---

## 9. EXECUTION BOUNDARY CONFIRMATION

- No JPA entity classes (`.java`) have been created in this step.
- No existing backend application code, controllers, or configurations were modified.
