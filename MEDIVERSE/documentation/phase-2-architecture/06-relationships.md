# 06 — DATABASE RELATIONSHIPS & CARDINALITY SPECIFICATION

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 6.1 USER IDENTITY, SYSTEM ROLES & ACTOR PROFILES

Mediverse uses a central authentication identity table (`users`) with a direct foreign key `role_id` → `roles(role_id)` mapping each authenticated user account to exactly one primary system role. The `users` table is extended via **1-to-1 relationships** to role-specific domain profile tables:

```
                                  +-------------------+
                                  |       roles       |
                                  +-------------------+
                                            | (1:N)
                                            v
                                  +-------------------+
                                  |       users       |
                                  +-------------------+
                                            |
         +-----------------+----------------+-----------------+
         | (1:1)           | (1:1)          | (1:1)           | (1:1)
         v                 v                v                 v
+------------------+ +---------------+ +------------------+ +-----------------+
| patient_profiles | | doctor_profiles| |medical_org_profiles| laboratory_profiles|
+------------------+ +---------------+ +------------------+ +-----------------+
```

- **EMERGENCY Role User (Actor):** Authenticated Emergency Responder account in `users` (`role_id` = `EMERGENCY`). Uses basic account identity in `users`. Does NOT have or require a separate user profile table.
- **ADMIN Role User (Actor):** Authenticated Administrator account in `users` (`role_id` = `ADMIN`). Uses basic account identity in `users`. Does NOT have or require a separate user profile table.

---

## 6.2 PATIENT CENTRIC ENTITY RELATIONSHIPS (PATIENT-OWNED DATA)

All clinical records, appointments, file metadata, and **Patient Emergency Information** link back to the central `patient_profiles` entity (`patient_id` `CHAR(36)`):

```
                                +------------------+
                                | patient_profiles |
                                +------------------+
                                         |
     +---------+-------+-------+---------+-------+--------+----------------+------------------+
     | (1:1)   | (1:N) | (1:N) | (1:N)   | (1:N) | (1:N)  | (1:N)          | (1:N)            | (1:N)
     v         v       v       v         v       v        v                v                  v
+----------+ +-----+ +---+ +-------+ +-------+ +----+ +---------------+ +---------------+ +---------------+
|emergency_| |med  | |apt| |rx     | |reports| |lab | |patient_       | |file_metadata  | |audit_logs     |
|profiles  | |recs | +---+ +-------+ +-------+ |ords| |consents       | +---------------+ |(target_id)    |
+----------+ +-----+       |                   +----+ +---------------+                   +---------------+
                           | (1:N)
                           v
                   +--------------------+
                   | prescription_items |
                   +--------------------+
```

- **`emergency_profiles` Clarification:** Belongs 1-to-1 to `patient_profiles` (`patient_id`). Represents **Patient-Owned Emergency Information** (blood group, severe allergies, active critical medications, chronic conditions, emergency contacts). It is **NOT** a profile table for the Emergency Responder user account.

---

## 6.3 KEY CARDINALITY SUMMARY

1. `Role` (1) ── (N) `User` (via `users.role_id`)
2. `User` (1) ── (1) `PatientProfile`
3. `User` (1) ── (1) `DoctorProfile`
4. `User` (1) ── (1) `MedicalOrgProfile`
5. `User` (1) ── (1) `LaboratoryProfile`
6. `PatientProfile` (1) ── (1) `EmergencyProfile` (Patient-Owned Emergency Vitals)
7. `PatientProfile` (1) ── (N) `MedicalRecord`
8. `DoctorProfile` (1) ── (N) `MedicalRecord`
9. `PatientProfile` (1) ── (N) `Appointment`
10. `DoctorProfile` (1) ── (N) `Appointment`
11. `PatientProfile` (1) ── (N) `Prescription`
12. `DoctorProfile` (1) ── (N) `Prescription`
13. `Prescription` (1) ── (N) `PrescriptionItem`
14. `PatientProfile` (1) ── (N) `LabOrder`
15. `LaboratoryProfile` (1) ── (N) `LabOrder`
16. `PatientProfile` (1) ── (N) `MedicalReport`
17. `FileMetadata` (1) ── (1) `MedicalReport`
18. `PatientProfile` (1) ── (N) `PatientConsent`
19. `User` (1) ── (N) `PatientConsent` (`granted_to_user_id`)
20. `Role` (N) ── (M) `Permission` (via `role_permissions`)

---

## 6.4 REDUNDANCY & ARCHITECTURAL CLARITY REASONING

- **Distinct Emergency Concepts:** Clearly decouples the `EMERGENCY` Responder User (System Actor) from `emergency_profiles` (Patient-Owned Triage Data attached to `patient_profiles`).
- **No Duplicate Junction Tables:** Direct `users.role_id` mapping accelerates JWT session verification.
- **Audit Decoupling:** `audit_logs` retains plain `CHAR(36)` fields without foreign key cascades to guarantee immutable audit logs.
