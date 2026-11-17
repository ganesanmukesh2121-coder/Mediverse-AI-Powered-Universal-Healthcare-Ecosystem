# 12 — AUDIT ARCHITECTURE SPECIFICATION

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 12.1 APPEND-ONLY TAMPER-RESISTANT AUDIT MODEL

All security-sensitive operations across all six portals MUST produce an immutable, insert-only audit record in MySQL `audit_logs`.

```
+-----------------------------------------------------------------------------------+
|                        AUDIT LOG DATA SCHEMA SPECIFICATION                        |
+---------------------+-------------------+-----------------------------------------+
| COLUMN NAME         | DATA TYPE         | DESCRIPTION                             |
+---------------------+-------------------+-----------------------------------------+
| audit_id            | BIGINT (PK, Auto) | Unique log entry identifier             |
| timestamp           | DATETIME (UTC)    | Exact execution timestamp               |
| actor_user_id       | CHAR(36)          | UUID of user executing action (actor)   |
| actor_role          | VARCHAR(20)       | Role (`PATIENT`, `EMERGENCY`, `DOCTOR`) |
| target_patient_id   | CHAR(36)          | UUID of target patient (data subject)   |
| action_type         | VARCHAR(50)       | Standardized action code                |
| resource_type       | VARCHAR(50)       | Entity type (`RECORD`, `FILE`, `CONSENT`)|
| resource_id         | VARCHAR(36)       | ID of accessed resource                 |
| execution_status    | VARCHAR(20)       | `SUCCESS`, `FAILURE`, `DENIED`          |
| ip_address          | VARCHAR(45)       | Client IPv4/IPv6 address                |
| user_agent          | VARCHAR(255)      | Client application user agent string    |
| security_context    | TEXT (JSON)       | Additional metadata (e.g. override reason)|
+---------------------+-------------------+-----------------------------------------+
```

---

## 12.2 MANDATORY AUDITED EVENTS

The system MUST automatically capture audit entries for the following 12 critical event categories:

1. `USER_LOGIN_SUCCESS`: Successful user authentication.
2. `USER_LOGIN_FAILURE`: Invalid credentials attempt.
3. `PATIENT_RECORD_VIEW`: Doctor or Medical Org views patient clinical timeline/record.
4. `REPORT_VIEW`: Viewing or downloading diagnostic lab reports.
5. `DOCUMENT_UPLOAD`: Uploading personal files, hospital discharge papers, or lab PDFs.
6. `PRESCRIPTION_CREATE`: Issuing a digital e-prescription.
7. `CONSENT_GRANT`: Patient approves new provider consent.
8. `CONSENT_REVOKE`: Patient revokes existing provider consent.
9. `EMERGENCY_OVERRIDE_ACCESS`: Emergency Responder user (`actor_user_id`) executes emergency triage lookup for target patient (`target_patient_id`).
10. `PROVIDER_VERIFY_APPROVE`: Admin approves provider application.
11. `PROVIDER_VERIFY_REJECT`: Admin rejects provider application.
12. `ACCOUNT_STATUS_CHANGE`: Admin suspends or reactivates a user account.

---

## 12.3 AUDIT TAMPER-RESISTANCE ENFORCEMENT

- **Insert-Only Execution:** MySQL table permissions for application database users enforce `INSERT` and `SELECT` rights only. `UPDATE` and `DELETE` SQL operations are revoked on `audit_logs`.
- **No Sensitive Data Leaks:** Plaintext passwords, secret JWT keys, and raw clinical notes MUST NEVER be written into audit log payloads.
