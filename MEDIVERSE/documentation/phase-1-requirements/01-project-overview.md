# 01 — PROJECT OVERVIEW

## 1.1 PROJECT VISION

**MEDIVERSE — AI-Powered Universal Healthcare Ecosystem** is an enterprise-grade, patient-centric digital health platform designed to transform personal healthcare management and clinical data accessibility.

The core philosophy of Mediverse is that a patient's healthcare data should be unified, accessible, secure, and under the explicit governance of the patient. In traditional healthcare systems, medical records are fragmented across multiple clinics, diagnostic laboratories, hospitals, and physical paper files. Mediverse solves this fragmentation by assigning every registered patient a unique, persistent **Mediverse Health ID**.

The **Mediverse Health ID** serves as the authoritative single key connecting authorized medical interactions, prescriptions, lab results, clinical consultations, hospital discharge summaries, and uploaded medical documents into a continuous, chronological **Patient Health Timeline**.

---

## 1.2 KEY ECOSYSTEM PILLARS

Mediverse is anchored by four foundational pillars:

```
+-----------------------------------------------------------------------------------+
|                                  MEDIVERSE PILLARS                                |
+------------------+------------------+-------------------+-------------------------+
| 1. PATIENT       | 2. UNIFIED       | 3. ROBUST         | 4. GROUNDED AGENTIC     |
|    SOVEREIGNTY   |    TIMELINE      |    SECURITY       |    AI ASSISTANCE        |
+------------------+------------------+-------------------+-------------------------+
| Patient owns     | Consolidates     | Multi-tier RBAC,  | Source-grounded RAG     |
| profile and      | prescriptions,   | consent checks,   | patient summaries for   |
| permission rules | labs, uploads,   | JWT auth, and     | doctors; zero           |
| for providers.   | and clinical     | immutable audit   | autonomous medical      |
|                  | notes into one.  | log trail.        | decision-making.        |
+------------------+------------------+-------------------+-------------------------+
```

1. **Patient-Centric Data Sovereignty:** Patients maintain complete ownership of their digital healthcare records. Access by doctors, hospitals, and diagnostic labs is controlled through explicit roles and permissions.
2. **Unified Healthcare History:** Eliminates data silos by aggregating disparate health events (prescriptions, lab tests, hospital admissions, user-uploaded PDFs/images) into a single, structured digital timeline.
3. **Multi-Layered Security & Consent Framework:** Every data access request passes through rigorous Authentication (JWT) → Role-Based Access Control (RBAC) → Patient Consent / Emergency Authorization Rules → Audit Logging.
4. **Assisted Agentic AI Architecture:** Incorporates the **Mediverse Healthcare Record Agent** to assist doctors and patients in reviewing complex health timelines, synthesizing lab reports, and summarizing clinical histories without making autonomous diagnoses or treatment decisions.

---

## 1.3 HIGH-LEVEL SYSTEM ARCHITECTURE OVERVIEW

Mediverse follows a clean 3-tier modular monologic/services architecture:

```
+-----------------------------------------------------------------------------------+
|                                FRONTEND TIER                                      |
|                                                                                   |
|   [ Patient App ]       [ Emergency App ]      [ Doctor / Medical / Lab / Admin ]  |
|   (Flutter Mobile)      (Flutter Mobile)             (React Web Portals)          |
+-----------------------------------------+-----------------------------------------+
                                          |
                                          | REST API / HTTPS (JWT Bearer Token)
                                          v
+-----------------------------------------------------------------------------------+
|                                BACKEND API TIER                                   |
|                                                                                   |
|                            Spring Boot (Java 21) API                              |
|   +-------------------+  +-------------------+  +-----------------------------+   |
|   | Auth & RBAC Module|  | Timeline Service  |  | Health ID Generator         |   |
|   +-------------------+  +-------------------+  +-----------------------------+   |
|   | Consent Engine    |  | Storage Gateway   |  | Audit Logging Service       |   |
|   +-------------------+  +-------------------+  +-----------------------------+   |
|   | AI Orchestrator   |  | Verification Hub  |  | Emergency Override Handler  |   |
|   +-------------------+  +-------------------+  +-----------------------------+   |
+-----------------------------------------+-----------------------------------------+
                                          |
                     +--------------------+--------------------+
                     |                                         |
                     v                                         v
+-----------------------------------------+   +-------------------------------------+
|            DATABASE TIER                |   |          STORAGE TIER               |
|            MySQL 8.x                    |   |          Supabase Storage           |
|  - Users, Roles, Credentials            |   |  - Encrypted PDFs & Scans           |
|  - Health ID Registry                   |   |  - Clinical Images                  |
|  - Metadata & References                |   |  - Lab Reports & Prescriptions      |
|  - Audit Log Records                    |   |  - Private Bucket Storage           |
+-----------------------------------------+   +-------------------------------------+
```

---

## 1.4 PORTAL STRUCTURE & ACCESS SUMMARY

Mediverse serves six distinct system roles through six purpose-built frontend interfaces:

| Portal Name | Target Role | Technology | Primary Purpose |
| :--- | :--- | :--- | :--- |
| **Patient Portal** | Patient | Flutter (Mobile) | Profile management, document upload, timeline viewing, AI assistant, consent management. |
| **Emergency Portal** | Emergency User | Flutter (Mobile) | Rapid, audited access to vital medical parameters during life-threatening emergencies. |
| **Doctor Portal** | Doctor | React (Web) | Patient search via Health ID/QR, clinical consultations, e-prescriptions, timeline access, AI summaries. |
| **Medical Org Portal** | Medical | React (Web) | Hospital admission records, inpatient treatment tracking, discharge summaries, hospital document uploads. |
| **Laboratory Portal** | Lab | React (Web) | Patient identification, lab test record creation, diagnostic report uploads, structured result publishing. |
| **Admin Portal** | Admin | React (Web) | Healthcare provider verification lifecycle, user account management, security audit log monitoring. |

---

## 1.5 PROJECT BOUNDARIES & ACADEMIC CONTEXT

Mediverse is developed as a final-year B.Tech IT engineering project. It balances production-grade architectural standards with realistic academic constraints:
- **Synthetic Data:** Utilizes mock/synthetic clinical datasets for demonstration.
- **Controlled Scope:** Focuses on core ecosystem workflows without requiring real-world government registry API integrations or commercial hospital EMR integrations.
- **Strict Separation of Concerns:** Application frontends communicate exclusively via the Spring Boot API; direct database connections from mobile/web clients are strictly prohibited.
