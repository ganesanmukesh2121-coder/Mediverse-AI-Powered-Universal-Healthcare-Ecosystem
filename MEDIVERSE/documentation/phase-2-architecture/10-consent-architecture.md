# 10 — CONSENT ARCHITECTURE SPECIFICATION

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 10.1 CONCEPTUAL CONSENT MODEL

Patient data sovereignty is a core foundation of Mediverse. Healthcare providers cannot access historical patient medical records unless active consent is granted by the patient in `patient_consents`.

```
+-----------------------------------------------------------------------------------+
|                           CONSENT RECORD ENTITY MODEL                             |
+-----------------------------------------------------------------------------------+
| consent_id          : CHAR(36) UUID v4                                            |
| patient_id          : CHAR(36) Foreign Key -> patient_profiles(patient_id)        |
| granted_to_user_id  : CHAR(36) Foreign Key -> users(user_id)                     |
| granted_role        : VARCHAR(20) ('DOCTOR', 'MEDICAL')                           |
| data_category       : ENUM('FULL_TIMELINE', 'LAB_ONLY', 'CONSULT_ONLY')           |
| permission_level    : ENUM('READ_ONLY', 'READ_WRITE')                             |
| status              : ENUM('ACTIVE', 'EXPIRED', 'REVOKED')                        |
| granted_at          : DATETIME (UTC)                                              |
| expires_at          : DATETIME (UTC)                                              |
| revoked_at          : DATETIME (UTC, Nullable)                                    |
+-----------------------------------------------------------------------------------+
```

---

## 10.2 GOVERNANCE & LIFECYCLE RULES

1. **Who Can Grant Consent:**
   - **Exclusively the Patient** via the Flutter Mobile App.
   - Patients can grant consent directly to a specific Doctor or Medical Organization using their user account ID or by scanning a provider QR code.

2. **Who Can Revoke Consent:**
   - **Exclusively the Patient** at any time. Revocation instantly updates status to `REVOKED`, sets `revoked_at`, and invalidates future provider API access attempts.

3. **What Consent Applies To:**
   - Access to historical consultation notes, digital prescriptions, lab report PDFs, hospital discharge summaries, and uploaded personal files.

4. **Consent Expiration Mechanics:**
   - Every consent record includes an explicit `expires_at` timestamp (e.g. 24 hours, 30 days, or duration of hospital stay).
   - During API authorization validation, the backend evaluates: `status == 'ACTIVE' AND NOW() < expires_at`.

5. **How Emergency Access Differs:**
   - Authenticated Emergency Responder users (`EMERGENCY` role) access patient emergency information under emergency access override rules without requiring explicit patient consent.
   - Emergency access is strictly constrained to **Patient-Owned Emergency Information** (`emergency_profiles`: blood group, severe allergies, critical medications, emergency contacts).
   - Emergency access **MUST NEVER** reveal full historical clinical notes, e-prescriptions, lab reports, or private uploaded files, and triggers mandatory append-only audit logging (`EMERGENCY_OVERRIDE_ACCESS`).
