# MEDIVERSE — AI-Powered Universal Healthcare Ecosystem

![Status](https://img.shields.io/badge/Phase-Phase_1:_Requirements_&_Foundation-blue)
![Architecture](https://img.shields.io/badge/Architecture-3--Tier_Modular_Monolith-emerald)
![Tech Stack](https://img.shields.io/badge/Stack-Spring_Boot_%7C_Flutter_%7C_React_%7C_MySQL_%7C_Supabase-orange)

Mediverse is a secure, patient-centric digital healthcare ecosystem designed for a final-year B.Tech IT engineering project. It solves the critical problem of fragmented medical histories by establishing a persistent **Mediverse Health ID** and aggregating all authorized clinical interactions into a unified digital healthcare timeline.

---

## 📌 PHASE 1 NOTICE

> **CURRENT STATUS: PHASE 1 — REQUIREMENTS & PROJECT FOUNDATION**
> 
> As per project requirements, **no application code (Spring Boot, Flutter, React, SQL, AI integration)** is implemented in Phase 1. This repository currently contains the complete, authoritative documentation suite and foundational architecture specifications.

---

## 📚 DOCUMENTATION INDEX

All Phase 1 specification documents are located under `documentation/phase-1-requirements/`:

| Document | Title | Description |
| :--- | :--- | :--- |
| [`01-project-overview.md`](./documentation/phase-1-requirements/01-project-overview.md) | Project Overview | Vision, core ecosystem pillars, and high-level architecture. |
| [`02-problem-statement.md`](./documentation/phase-1-requirements/02-problem-statement.md) | Problem Statement | Industry analysis of fragmented records, security risks, and emergency access gaps. |
| [`03-objectives.md`](./documentation/phase-1-requirements/03-objectives.md) | Project Objectives | Primary and secondary engineering goals with success metrics. |
| [`04-scope.md`](./documentation/phase-1-requirements/04-scope.md) | Ecosystem Scope | Comprehensive in-scope role capabilities and explicit out-of-scope boundaries. |
| [`05-user-roles.md`](./documentation/phase-1-requirements/05-user-roles.md) | User Roles & Portals | In-depth breakdown of Patient, Emergency, Doctor, Medical, Lab, and Admin roles. |
| [`06-functional-requirements.md`](./documentation/phase-1-requirements/06-functional-requirements.md) | Functional Requirements | Detailed functional requirements categorized by role, Health ID, consent, and storage. |
| [`07-non-functional-requirements.md`](./documentation/phase-1-requirements/07-non-functional-requirements.md) | Non-Functional Requirements | Security, privacy, performance, reliability, scalability, and usability standards. |
| [`08-user-workflows.md`](./documentation/phase-1-requirements/08-user-workflows.md) | End-to-End Workflows | Step-by-step text workflows and Mermaid sequence diagrams for all 6 roles. |
| [`09-ai-requirements.md`](./documentation/phase-1-requirements/09-ai-requirements.md) | AI & Document Intelligence | Mediverse Healthcare Record Agent rules, RAG workflow, OCR, and AI safety guardrails. |
| [`10-security-requirements.md`](./documentation/phase-1-requirements/10-security-requirements.md) | Security & Audit Architecture | JWT, BCrypt, RBAC, consent enforcement, emergency overrides, and audit log schema. |
| [`11-assumptions-and-limitations.md`](./documentation/phase-1-requirements/11-assumptions-and-limitations.md) | Assumptions & Open Questions | Technical assumptions, operational constraints, and items marked `TO BE DECIDED IN A LATER PHASE`. |
| [`12-phase-1-acceptance-criteria.md`](./documentation/phase-1-requirements/12-phase-1-acceptance-criteria.md) | Acceptance Criteria & Report | Phase 1 completion checklist and formal Phase 1 Completion Report. |

---

## 👥 SYSTEM ROLES & PORTAL MAPPING

Mediverse features **six user roles** distributed across **six specialized portals**:

```
+-------------------------------------------------------------------------+
|                           MEDIVERSE BACKEND API                         |
|                     (Java 21 / Spring Boot / REST API)                  |
+-------------------------------------------------------------------------+
                                     ^
                                     |  JSON / HTTPS (JWT Auth)
    +--------------------------------+--------------------------------+
    |                                                                 |
+---+---------------------------------+   +---------------------------+---+
|       MOBILE PORTALS (Flutter)      |   |       WEB PORTALS (React)     |
+-------------------------------------+   +-------------------------------+---+
| 1. PATIENT PORTAL                   |   | 3. DOCTOR PORTAL              |
|    - Personal Profile & Timeline    |   |    - Clinical Notes & Consult |
|    - Document Upload & Consent      |   |    - Digital Prescriptions    |
|    - Healthcare AI Assistant        |   |    - AI Patient Summaries     |
|                                     |   |                               |
| 2. EMERGENCY PORTAL                 |   | 4. MEDICAL ORG PORTAL         |
|    - Triage / Critical Info Access  |   |    - Hospital Admissions      |
|    - Audited Emergency Override     |   |    - Discharge Summaries      |
|                                     |   |                               |
|                                     |   | 5. LABORATORY PORTAL          |
|                                     |   |    - Test Record Creation     |
|                                     |   |    - Lab Report Uploads       |
|                                     |   |                               |
|                                     |   | 6. ADMINISTRATIVE PORTAL      |
|                                     |   |    - Provider Verification    |
|                                     |   |    - Account Lifecycle & Security|
+-------------------------------------+   +-------------------------------+---+
```

---

## 🛠️ APPROVED TECHNOLOGY BASELINE

- **Mobile App:** Flutter + Dart
- **Web Portals:** React + HTML/CSS/JavaScript
- **Backend Service:** Spring Boot + Java 21
- **Database:** MySQL 8.x
- **Authentication:** JWT (JSON Web Tokens)
- **Password Security:** BCrypt Algorithm
- **Authorization:** Role-Based Access Control (RBAC) + Consent Verification
- **Object Storage:** Supabase Storage
- **AI Ecosystem:** LLM + RAG (LangChain / LangGraph) + OCR Engine
- **Protocol:** REST API over HTTPS

---

## 🔒 SECURITY & PRIVACY PRINCIPLES

1. **Patient Sovereignty:** Patient owns their Health ID and controls clinical record visibility.
2. **Data Minimization:** Healthcare providers receive only purpose-bound, minimal data access.
3. **No Direct DB Access:** Clients connect exclusively through Spring Boot REST APIs.
4. **Immutable Audit Logs:** Every sensitive read, write, consent change, and emergency override is logged.
5. **No Autonomous AI Decisions:** AI acts purely as an assistive synthesizer; clinical decisions remain 100% human-controlled.

---

## 📄 LICENSE & PROJECT METADATA

- **Project Type:** B.Tech IT Final Year Academic Project
- **Current Phase:** Phase 1 — Requirements Documentation
- **Core Reference Document:** [`PROJECT_CONTEXT.md`](./PROJECT_CONTEXT.md)
