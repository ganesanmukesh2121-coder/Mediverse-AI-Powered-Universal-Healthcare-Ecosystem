# 04 — DATABASE ARCHITECTURE SPECIFICATION

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 4.1 RELATIONAL DATABASE OVERVIEW

Mediverse uses **MySQL 8.x** as its primary relational database management system. MySQL handles structured domain entities, user identity accounts, Health ID registries, consent rules, file metadata, and append-only tamper-resistant security audit logs.

```
+-----------------------------------------------------------------------------------+
|                        MYSQL RELATIONAL DATABASE ARCHITECTURE                     |
+-----------------------------------------------------------------------------------+
| ENGINE            : InnoDB Storage Engine (ACID Transactions, Foreign Keys)       |
| CHARACTER SET     : utf8mb4 (Full Unicode Support)                                |
| COLLATION         : utf8mb4_unicode_ci                                            |
| PRIMARY KEYS      : CHAR(36) UUID v4 for Security-Sensitive Public Entities       |
|                     BIGINT Auto-Increment for High-Volume Internal Logs/Items     |
| INDEXING STRATEGY : Unique Indexes on Health ID, Email, Phone, Verification IDs   |
|                     B-Tree Composite Indexes on (patient_id, created_at)          |
+-----------------------------------------------------------------------------------+
```

---

## 4.2 DATABASE MODULE CLUSTERS

The schema is logically partitioned into 6 domain clusters:

1. **Identity & Authentication Cluster:** `users`, `roles`, `permissions`, `role_permissions`, `user_roles`.
2. **Role Profile Cluster:** `patient_profiles`, `doctor_profiles`, `medical_org_profiles`, `laboratory_profiles`, `emergency_profiles`.
3. **Clinical Records Cluster:** `health_id_registry`, `medical_records`, `consultation_notes`, `prescriptions`, `prescription_items`, `appointments`.
4. **Diagnostic & Hospital Cluster:** `lab_orders`, `lab_results`, `medical_reports`, `admission_records`, `discharge_summaries`.
5. **Consent & File Metadata Cluster:** `patient_consents`, `file_metadata`.
6. **Security & Audit Cluster:** `audit_logs`, `provider_verification_requests`, `notifications`.
