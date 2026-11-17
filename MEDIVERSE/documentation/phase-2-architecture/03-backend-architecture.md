# 03 — MODULAR BACKEND ARCHITECTURE

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 3.1 BACKEND ARCHITECTURE OVERVIEW

The Mediverse backend is designed as a Spring Boot (Java 21) Modular Monolith. The codebase is organized into 19 distinct functional modules with strict boundary separation and clear package ownership.

```
+-----------------------------------------------------------------------------------+
|                        SPRING BOOT MODULAR BACKEND LAYERS                         |
+-----------------------------------------------------------------------------------+
| REST CONTROLLER LAYER   -> HTTP Request Mapping, DTO Validation, OpenAPI Annotations|
| SECURITY & AUTH LAYER   -> JWT Authentication Filter, Spring Security RBAC Guards |
| SERVICE LAYER           -> Business Logic, Consent Validation, Health ID Engine  |
| REPOSITORY DATA LAYER   -> Spring Data JPA Repositories, Custom Criteria Queries  |
| ENTITY LAYER            -> JPA Annotations, MySQL Schema Mappings, Constraints    |
+-----------------------------------------------------------------------------------+
```

---

## 3.2 DETAILED EVALUATION OF THE 19 BACKEND MODULES

### 1. `authentication` Module
- **Purpose:** Manages user authentication, token issuance, and password security.
- **Responsibilities:** Validating login credentials, hashing passwords via BCrypt, issuing JWT tokens, handling refresh tokens.
- **Major Entities:** None (uses `User` entity from `users` module).
- **Expected Services:** `AuthService`, `JwtTokenProvider`, `CustomUserDetailsService`.
- **Dependencies:** `users`, `audit`.
- **APIs Owned:** `/api/v1/auth/login`, `/api/v1/auth/refresh`, `/api/v1/auth/logout`.

### 2. `authorization` Module
- **Purpose:** Enforces RBAC permissions, patient consent rules, and context authorization.
- **Responsibilities:** Intercepting HTTP requests, evaluating role authorities, validating active patient consent, validating provider account status (`ACTIVE`).
- **Major Entities:** None (reads `Role`, `Permission`, `Consent`).
- **Expected Services:** `AuthorizationService`, `ConsentValidator`, `RbacEvaluator`.
- **Dependencies:** `users`, `consent`, `audit`.
- **APIs Owned:** Internal security filter chain components.

### 3. `users` Module
- **Purpose:** Central identity management for all user accounts across system roles.
- **Responsibilities:** Creating base user accounts, updating profile metadata, managing password changes, tracking account status (`PENDING`, `ACTIVE`, `REJECTED`, `SUSPENDED`).
- **Major Entities:** `User`, `Role`, `Permission`, `UserRole`.
- **Expected Services:** `UserService`, `RoleService`.
- **Dependencies:** None (foundation module).
- **APIs Owned:** `/api/v1/users/me`, `/api/v1/users/{userId}/status`.

### 4. `patients` Module
- **Purpose:** Manages patient-specific demographic profiles and health identifiers.
- **Responsibilities:** Registering patient profiles, updating emergency contacts/allergies, linking Health ID references.
- **Major Entities:** `PatientProfile`.
- **Expected Services:** `PatientService`.
- **Dependencies:** `users`, `health-id`.
- **APIs Owned:** `/api/v1/patients/profile`, `/api/v1/patients/{healthId}/demographics`.

### 5. `doctors` Module
- **Purpose:** Manages doctor profile credentials and clinical registration applications.
- **Responsibilities:** Managing physician profiles, license numbers, specialization metadata, verification applications.
- **Major Entities:** `DoctorProfile`.
- **Expected Services:** `DoctorService`.
- **Dependencies:** `users`.
- **APIs Owned:** `/api/v1/doctors/profile`, `/api/v1/doctors/register`.

### 6. `medical-organizations` Module
- **Purpose:** Manages hospital and medical organization profiles and admissions.
- **Responsibilities:** Managing hospital profiles, facility registration, inpatient admission logging, discharge summary publishing.
- **Major Entities:** `MedicalOrgProfile`, `AdmissionRecord`, `DischargeSummary`.
- **Expected Services:** `MedicalOrgService`, `AdmissionService`.
- **Dependencies:** `users`, `patients`, `health-id`.
- **APIs Owned:** `/api/v1/medical/profile`, `/api/v1/medical/admissions`, `/api/v1/medical/discharge`.

### 7. `laboratories` Module
- **Purpose:** Manages laboratory facility profiles and diagnostic test workflows.
- **Responsibilities:** Diagnostic lab profiling, test order registration, result entry, lab report publishing.
- **Major Entities:** `LaboratoryProfile`, `LabOrder`, `LabResultItem`.
- **Expected Services:** `LaboratoryService`, `LabTestService`.
- **Dependencies:** `users`, `patients`, `file-storage`.
- **APIs Owned:** `/api/v1/labs/profile`, `/api/v1/labs/orders`, `/api/v1/labs/results`.

### 8. `emergency` Module
- **Purpose:** Handles rapid emergency triage data lookup under audited conditions.
- **Responsibilities:** Parsing Health ID / QR reference, extracting minimal vital triage parameters, triggering mandatory emergency audit logs.
- **Major Entities:** `EmergencyProfileView` (derived view / subset).
- **Expected Services:** `EmergencyTriageService`.
- **Dependencies:** `patients`, `audit`.
- **APIs Owned:** `/api/v1/emergency/triage/{healthId}`.

### 9. `admin` Module
- **Purpose:** Administrative operations, provider verification queues, and audit monitoring.
- **Responsibilities:** Reviewing pending provider applications, updating provider account statuses, inspecting system audit logs. **Admin READ access to Patient Profile is strictly limited to account identity info.**
- **Major Entities:** `ProviderVerificationRequest`.
- **Expected Services:** `AdminVerificationService`, `AdminAuditService`.
- **Dependencies:** `users`, `doctors`, `medical-organizations`, `laboratories`, `audit`.
- **APIs Owned:** `/api/v1/admin/providers/pending`, `/api/v1/admin/providers/{id}/verify`, `/api/v1/admin/users/{id}/status`.

### 10. `health-id` Module
- **Purpose:** Backend generator and registry for unique Mediverse Health IDs.
- **Responsibilities:** Generating unique, persistent, immutable Health IDs, producing signed QR reference tokens, performing Health ID lookups.
- **Major Entities:** `HealthIdRegistry`.
- **Expected Services:** `HealthIdGeneratorService`, `QrCodeService`.
- **Dependencies:** `users`.
- **APIs Owned:** `/api/v1/health-id/generate`, `/api/v1/health-id/verify/{healthId}`, `/api/v1/health-id/qr/{healthId}`.

### 11. `medical-records` Module
- **Purpose:** Core clinical consultation records, diagnoses, and unified health timeline aggregation.
- **Responsibilities:** Recording doctor consult notes, chief complaints, diagnostic findings, aggregating timeline feeds.
- **Major Entities:** `MedicalRecord`, `ConsultationNote`.
- **Expected Services:** `MedicalRecordService`, `TimelineAggregationService`.
- **Dependencies:** `patients`, `doctors`, `consent`.
- **APIs Owned:** `/api/v1/records/consultations`, `/api/v1/records/timeline/{healthId}`.

### 12. `reports` Module
- **Purpose:** Handles diagnostic lab reports and medical document reports.
- **Responsibilities:** Linking report metadata, managing report categories, interfacing with file storage.
- **Major Entities:** `MedicalReport`.
- **Expected Services:** `ReportService`.
- **Dependencies:** `patients`, `file-storage`, `consent`.
- **APIs Owned:** `/api/v1/reports`, `/api/v1/reports/{reportId}`.

### 13. `prescriptions` Module
- **Purpose:** Digital e-prescription generation and item management.
- **Responsibilities:** Composing prescriptions, adding prescription items (drug name, dosage, frequency), patient prescription view.
- **Major Entities:** `Prescription`, `PrescriptionItem`.
- **Expected Services:** `PrescriptionService`.
- **Dependencies:** `patients`, `doctors`, `consent`.
- **APIs Owned:** `/api/v1/prescriptions`, `/api/v1/prescriptions/patient/{healthId}`.

### 14. `appointments` Module
- **Purpose:** Doctor and patient consultation scheduling management.
- **Responsibilities:** Booking consultation slots, updating appointment statuses (`SCHEDULED`, `COMPLETED`, `CANCELLED`).
- **Major Entities:** `Appointment`.
- **Expected Services:** `AppointmentService`.
- **Dependencies:** `patients`, `doctors`.
- **APIs Owned:** `/api/v1/appointments`, `/api/v1/appointments/{id}/status`.

### 15. `consent` Module
- **Purpose:** Manages explicit patient consent grants and revocations for providers.
- **Responsibilities:** Recording consent rules, evaluating active consent for provider access requests, managing consent expiration.
- **Major Entities:** `PatientConsent`.
- **Expected Services:** `ConsentService`.
- **Dependencies:** `patients`, `users`.
- **APIs Owned:** `/api/v1/consents`, `/api/v1/consents/{consentId}/revoke`.

### 16. `audit` Module
- **Purpose:** Append-only, tamper-resistant security audit log recording and querying.
- **Responsibilities:** Capturing security audit entries for all sensitive API actions, providing admin audit query interface.
- **Major Entities:** `AuditLog`.
- **Expected Services:** `AuditLogService`.
- **Dependencies:** None (independent logger module).
- **APIs Owned:** `/api/v1/audit/logs`, `/api/v1/audit/my-logs`.

### 17. `file-storage` Module
- **Purpose:** Manages private Supabase Storage object bucket interactions and MySQL file metadata.
- **Responsibilities:** Uploading binary files to Supabase, storing metadata in MySQL, generating 15-minute pre-signed URLs.
- **Major Entities:** `FileMetadata`.
- **Expected Services:** `SupabaseStorageService`, `FileMetadataService`.
- **Dependencies:** `users`, `consent`.
- **APIs Owned:** `/api/v1/files/upload`, `/api/v1/files/{fileId}/presigned-url`.

### 18. `notifications` Module
- **Purpose:** Handles patient real-time and in-app security notifications.
- **Responsibilities:** Alerting patients when records are accessed or when emergency overrides occur.
- **Major Entities:** `Notification`.
- **Expected Services:** `NotificationService`.
- **Dependencies:** `users`.
- **APIs Owned:** `/api/v1/notifications`, `/api/v1/notifications/{id}/read`.

### 19. `ai` Module
- **Purpose:** Dual AI Subsystem integration (Patient AI Assistant & Mediverse Healthcare Record Agent).
- **Responsibilities:** Orchestrating RAG context retrieval over authorized records, coordinating OCR text extraction, interacting with LLM APIs under strict guardrails.
- **Major Entities:** `AiConversationLog`, `DocumentVectorIndex`.
- **Expected Services:** `PatientAiAssistantService`, `HealthcareRecordAgentService`, `RagRetrievalService`, `OcrProcessingService`.
- **Dependencies:** `medical-records`, `reports`, `file-storage`, `authorization`.
- **APIs Owned:** `/api/v1/ai/patient-assistant`, `/api/v1/ai/record-agent/summarize`.
