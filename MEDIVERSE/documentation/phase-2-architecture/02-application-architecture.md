# 02 — APPLICATION ARCHITECTURE SPECIFICATION

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 2.1 FRONTEND APPLICATION ARCHITECTURE

Mediverse employs two specialized presentation frameworks tailored to mobile and web user workflows.

```
+-----------------------------------------------------------------------------------+
|                        FRONTEND APPLICATION ARCHITECTURE                          |
+-----------------------------------------------------------------------------------+
| FLUTTER MOBILE APP (Patient / Emergency)                                          |
| ├── Core State Management (Provider / BLoC pattern)                               |
| ├── UI Views Layer (Patient Dashboard, Emergency Triage, Timeline, Consent)       |
| ├── Security Layer (Secure Storage for JWT, Biometric Prompt)                     |
| └── API Client Layer (HTTP Services -> Spring Boot REST endpoints)                 |
|                                                                                   |
| REACT WEB PORTALS (Doctor / Medical / Lab / Admin)                                |
| ├── Component Layer (Modular UI Components, Responsive Vanilla CSS Design)       |
| ├── Routing Layer (Protected Routes guarded by Role-based JWT validation)         |
| ├── State Layer (Context API / Redux Toolkit for active session & portal state)   |
| └── API Client Layer (Axios / Fetch Interceptors injecting Bearer Token)          |
+-----------------------------------------------------------------------------------+
```

---

## 2.2 MOBILE APPLICATION DESIGN (FLUTTER)

- **Target Roles:** `PATIENT`, `EMERGENCY`
- **Architecture Pattern:** Clean Architecture with BLoC / Provider state management.
- **Key Modules:**
  - `auth`: Registration, Login, Biometric Unlock, JWT Secure Token Storage.
  - `profile`: Demographics, Emergency Contacts, Health ID QR display.
  - `timeline`: Chronological health record feeds, filterable document list.
  - `consent`: Consent grant/revoke toggles, active access log viewer.
  - `emergency`: One-touch emergency triage view with rapid QR reader.
  - `patient_ai`: Interactive conversational interface for Patient AI Assistant.

---

## 2.3 WEB APPLICATION DESIGN (REACT)

- **Target Roles:** `DOCTOR`, `MEDICAL`, `LAB`, `ADMIN`
- **Architecture Pattern:** Component-Driven Architecture with Role-Based Route Guards.
- **Key Modules:**
  - `Doctor Portal`: Health ID search bar, patient clinical timeline, consult notes form, e-prescription editor, RAG Healthcare Record Agent panel.
  - `Medical Portal`: Hospital admission registrar, inpatient ward dashboard, discharge summary editor.
  - `Lab Portal`: Test order creation, structured metrics editor, diagnostic PDF uploader.
  - `Admin Portal`: Provider verification queue, account status toggle modal, global audit log inspector.
