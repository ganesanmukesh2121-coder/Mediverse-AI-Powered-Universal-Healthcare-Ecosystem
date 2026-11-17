# 14 — DUAL AI SUB-SYSTEM ARCHITECTURE

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 14.1 DUAL AI SYSTEM OVERVIEW

Mediverse features a dual AI architecture separated cleanly by target audience, presentation platform, data scope, and security boundaries.

```
+-----------------------------------------------------------------------------------+
|                            DUAL AI SUBSYSTEM ARCHITECTURE                         |
+-----------------------------------------------------------------------------------+
| 1. PATIENT AI ASSISTANT (Flutter Mobile App)                                      |
|    - Target User: Patients                                                        |
|    - Data Scope: Patient-owned and patient-authorized records only                |
|    - Purpose: Plain-language explanation of own prescriptions, lab terms, timeline|
|    - Guardrails: NO clinical advice, NO diagnosis, NO prescribing                 |
|                                                                                   |
| 2. MEDIVERSE HEALTHCARE RECORD AGENT (React Web Doctor Portal)                    |
|    - Target User: Authorized Healthcare Professionals (Doctors)                   |
|    - Data Scope: Consented patient records retrieved via backend API tools        |
|    - Purpose: Source-grounded RAG timeline synthesis & lab metric trend analysis  |
|    - Guardrails: NO autonomous diagnosis, NO autonomous prescribing, NO override  |
+-----------------------------------------------------------------------------------+
```

---

## 14.2 HEALTHCARE RECORD AGENT WORKFLOW & RAG TOOLS

The **Mediverse Healthcare Record Agent** operates using a tool-augmented Retrieval-Augmented Generation (RAG) architecture:

```
+-----------------------------------------------------------------------------------+
|                 MEDIVERSE HEALTHCARE RECORD AGENT RAG WORKFLOW                    |
+-----------------------------------------------------------------------------------+
| 1. Doctor submits query via Doctor Web Portal: "Summarize cardiac history".       |
| 2. Spring Boot API verifies Doctor Authentication, RBAC, and active Patient Consent|
| 3. Agent invokes Authorized Backend Retrieval Tools:                              |
|    ├── `RecordRetrievalTool`  : Fetches past consult notes & diagnoses            |
|    ├── `ReportRetrievalTool`  : Fetches lab reports & OCR text chunks             |
|    └── `TimelineTool`        : Fetches hospital admissions & discharge summaries  |
| 4. Vector Store / Context Engine retrieves relevant embedding chunks.              |
| 5. LLM generates source-grounded response with explicit document citations.       |
| 6. Doctor reviews summary; summary is stored as assistive output.                 |
+-----------------------------------------------------------------------------------+
```

---

## 14.3 NON-NEGOTIABLE AI SAFETY GUARDRAILS

- **RBAC & Consent Enforcement:** The AI engine has zero direct access to MySQL or vector databases. All data reads execute through Spring Boot API services enforcing RBAC and patient consent checks.
- **Audit Logging:** Every AI synthesis request logs an append-only audit entry (`AI_RAG_SUMMARY_REQUEST`).
- **No Autonomous Actions:** AI models are strictly prohibited from generating standalone diagnoses, issuing e-prescriptions, or making autonomous clinical treatment decisions.
- **Source Citation:** Every clinical statement generated for physicians MUST cite source `report_id`, `record_id`, or `file_id`.
