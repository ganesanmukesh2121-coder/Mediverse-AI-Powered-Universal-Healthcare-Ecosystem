# 09 — ROLE-BASED ACCESS CONTROL (RBAC) ARCHITECTURE

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 9.1 RBAC ARCHITECTURE OVERVIEW

Mediverse enforces RBAC across six system roles. Permissions are explicitly assigned to roles via `role_permissions` mapping table.

```
+-----------------------------------------------------------------------------------+
|                           4-FACTOR AUTHORIZATION MODEL                            |
+-----------------------------------------------------------------------------------+
|  AUTHORIZATION DECISION = RBAC PERMISSION  (Does role have static permission?)    |
|                           + PATIENT CONSENT (Is active consent granted?)          |
|                           + CONTEXT / SCOPE (Is purpose bound, e.g. lab order?)   |
|                           + EMERGENCY RULE  (Is active emergency override set?)   |
+-----------------------------------------------------------------------------------+
```

---

## 9.2 GRANULAR PERMISSION ENUMERATION

### Patient Permissions:
- `PATIENT_VIEW_OWN_PROFILE`
- `PATIENT_UPDATE_OWN_PROFILE`
- `PATIENT_UPLOAD_DOCUMENT`
- `PATIENT_MANAGE_CONSENT`
- `PATIENT_VIEW_OWN_AUDIT`
- `PATIENT_USE_AI_ASSISTANT`

### Emergency Permissions:
- `EMERGENCY_VIEW_EMERGENCY_INFO`

### Doctor Permissions:
- `DOCTOR_VIEW_AUTHORIZED_RECORD`
- `DOCTOR_CREATE_CONSULTATION`
- `DOCTOR_CREATE_PRESCRIPTION`
- `DOCTOR_ATTACH_DOCUMENT`
- `DOCTOR_USE_RAG_AGENT`

### Medical Organization Permissions:
- `MEDICAL_CREATE_ADMISSION`
- `MEDICAL_CREATE_TREATMENT`
- `MEDICAL_CREATE_DISCHARGE`
- `MEDICAL_UPLOAD_DOCUMENT`

### Laboratory Permissions:
- `LAB_CREATE_TEST_ORDER`
- `LAB_ENTER_RESULTS`
- `LAB_UPLOAD_REPORT`

### Admin Permissions:
- `ADMIN_VERIFY_PROVIDER`
- `ADMIN_MANAGE_ACCOUNT`
- `ADMIN_VIEW_AUDIT`

---

## 9.3 DETAILED ROLE-PERMISSION MATRIX

| Permission Key | PATIENT | EMERGENCY | DOCTOR | MEDICAL | LAB | ADMIN |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| `PATIENT_VIEW_OWN_PROFILE` | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `PATIENT_UPLOAD_DOCUMENT` | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `PATIENT_MANAGE_CONSENT` | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `PATIENT_USE_AI_ASSISTANT` | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `EMERGENCY_VIEW_EMERGENCY_INFO` | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `DOCTOR_VIEW_AUTHORIZED_RECORD` | ❌ | ❌ | ✅ (Consent) | ❌ | ❌ | ❌ |
| `DOCTOR_CREATE_CONSULTATION` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| `DOCTOR_CREATE_PRESCRIPTION` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| `DOCTOR_USE_RAG_AGENT` | ❌ | ❌ | ✅ (Consent) | ❌ | ❌ | ❌ |
| `MEDICAL_CREATE_ADMISSION` | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| `MEDICAL_CREATE_DISCHARGE` | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| `LAB_CREATE_TEST_ORDER` | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| `LAB_UPLOAD_REPORT` | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| `ADMIN_VERIFY_PROVIDER` | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| `ADMIN_MANAGE_ACCOUNT` | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| `ADMIN_VIEW_AUDIT` | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

---

## 9.4 EMERGENCY ROLE ACCESS FLOW & DATA BOUNDARY

### 1. Conceptual Distinction:
- **EMERGENCY Role User (System Actor):** Authenticated Emergency Responder user account (`users.role_id` mapping to `EMERGENCY` role).
- **Patient Emergency Information (Data Subject):** Patient-owned life-saving triage parameters stored in `emergency_profiles` attached to `patient_profiles.patient_id`.

### 2. Access Execution Flow:
```
EMERGENCY Responder User (System Actor)
  ↳ 1. Authentication (Valid JWT containing role = EMERGENCY)
  ↳ 2. RBAC Check (`EMERGENCY_VIEW_EMERGENCY_INFO`)
  ↳ 3. Emergency Access Rule Verification & Target Patient Lookup (`healthId` or QR)
  ↳ 4. Query `emergency_profiles` (Returns minimal vitals: blood group, severe allergies, critical meds, emergency contacts)
  ↳ 5. Mandatory Audit Log Recording (`EMERGENCY_OVERRIDE_ACCESS`)
```

### 3. Strict Boundary Restrictions:
The `EMERGENCY` role receives **ONLY** the patient-owned `emergency_profiles` triage parameters. An EMERGENCY role token **MUST NEVER** receive historical consultation notes, e-prescriptions, lab reports, radiology scans, or uploaded medical PDFs.
