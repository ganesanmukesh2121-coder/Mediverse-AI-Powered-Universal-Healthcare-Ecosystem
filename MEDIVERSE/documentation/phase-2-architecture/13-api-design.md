# 13 — REST API DESIGN CONTRACT SPECIFICATION

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 13.1 REST API DESIGN PRINCIPLES

- **Base URL:** `https://api.mediverse.local/api/v1`
- **Protocol:** HTTPS over TLS 1.3
- **Authentication:** HTTP Bearer Header containing stateless JWT access token.
- **Request/Response Format:** JSON (`Content-Type: application/json`)
- **Standard Error Model:** Standardized RFC 7807 error payload.

---

## 13.2 CORE REST ENDPOINT SPECIFICATION

### Group 1: Authentication & Identity (`/auth`)
- `POST /api/v1/auth/register/patient` — Register new Patient account & issue server-generated Health ID.
- `POST /api/v1/auth/register/provider` — Submit provider application (Doctor, Medical Org, Lab). Account created in `PENDING` status.
- `POST /api/v1/auth/login` — Validate credentials via BCrypt & issue JWT token containing `sub`, `role_id`, `role`, and `health_id`.

### Group 2: Patient Health Profile & Health ID (`/patients`)
- `GET /api/v1/patients/me` — Fetch authenticated patient's profile & Health ID.
- `GET /api/v1/patients/lookup/{healthId}` — Lookup patient demographic index by Health ID (Requires Doctor/Medical role + Consent).
- `GET /api/v1/patients/me/qr` — Generate signed QR reference token.

### Group 3: Clinical Encounters & Prescriptions (`/records`, `/prescriptions`)
- `POST /api/v1/records/consultations` — Conduct consultation encounter & create medical record (Doctor role + Consent).
- `GET /api/v1/records/timeline/{healthId}` — Fetch aggregated chronological health timeline (Doctor/Patient + Consent).
- `POST /api/v1/prescriptions` — Issue e-prescription header & items (Doctor role + Consent).
- `GET /api/v1/prescriptions/me` — Fetch patient's e-prescriptions (Patient role).

### Group 4: Appointments Module (`/appointments`)
- `POST /api/v1/appointments` — Schedule consultation appointment (Patient/Doctor role).
- `GET /api/v1/appointments/me` — List patient or doctor appointments (Authenticated user).
- `PUT /api/v1/appointments/{id}/status` — Update appointment status (SCHEDULED, COMPLETED, CANCELLED, NO_SHOW).

### Group 5: Diagnostics & File Management (`/lab`, `/files`)
- `POST /api/v1/lab/orders` — Create diagnostic lab order (Doctor/Lab role).
- `POST /api/v1/files/upload` — Upload medical document binary to Supabase private bucket & save metadata in MySQL.
- `GET /api/v1/files/{fileId}/presigned-url` — Request short-lived signed/temporary storage access URL (Requires Consent).

### Group 6: Consent Management (`/consents`)
- `POST /api/v1/consents` — Grant patient access consent to provider.
- `DELETE /api/v1/consents/{id}` — Revoke patient access consent.
- `GET /api/v1/consents/active` — List active patient consents.

### Group 7: Emergency Triage (`/emergency`)
- `GET /api/v1/emergency/triage/{healthId}` — Fetch minimal emergency vitals (Emergency role only; logs emergency audit entry).

### Group 8: Administrative Operations (`/admin`)
- `GET /api/v1/admin/providers/pending` — List pending provider registration queue (Admin role only).
- `POST /api/v1/admin/providers/{id}/verify` — Verify/Approve or Reject provider application (Admin role only).
- `GET /api/v1/admin/audit-logs` — Query append-only security audit logs (Admin role only).

---

## 13.3 STANDARDIZED ERROR RESPONSE SCHEMA

```json
{
  "timestamp": "2026-10-04T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Patient consent required to access health timeline",
  "path": "/api/v1/records/timeline/MV-2026-8A3F9B12",
  "errorCode": "CONSENT_REQUIRED"
}
```
