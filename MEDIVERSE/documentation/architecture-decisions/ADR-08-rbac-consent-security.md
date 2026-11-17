# ADR-08: COMBINED RBAC AND PATIENT CONSENT SECURITY MODEL

## STATUS: ACCEPTED

### DECISION
Enforce a 4-factor authorization engine combining static Role-Based Access Control (RBAC), explicit Patient Consent verification, purpose context, and emergency overrides.

### REASON
In healthcare systems, static RBAC permissions alone (e.g. `DOCTOR_VIEW_RECORD`) are insufficient. A doctor should NOT have global access to all patients in the database. Authorization MUST require an active, unexpired consent grant issued by the specific patient (`patient_consents`), unless an audited emergency triage rule applies.

### ALTERNATIVES CONSIDERED
- **Pure RBAC Model:** Rejected because it allows any authenticated doctor to query any patient record without consent.

### CONSEQUENCES
- All clinical query endpoints must execute dual authorization checks: RBAC check first, followed by a consent database lookup.
