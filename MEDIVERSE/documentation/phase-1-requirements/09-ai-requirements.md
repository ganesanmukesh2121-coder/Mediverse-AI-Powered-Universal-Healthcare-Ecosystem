# 09 — AI REQUIREMENTS & DOCUMENT INTELLIGENCE

## 9.1 DUAL AI ARCHITECTURE OVERVIEW

Mediverse incorporates a dual AI architecture designed to support both patients and healthcare professionals while maintaining strict security, privacy, and clinical safety boundaries.

```
+-----------------------------------------------------------------------------------+
|                            MEDIVERSE DUAL AI ARCHITECTURE                         |
+-----------------------------------------------------------------------------------+
| 1. PATIENT AI ASSISTANT (Flutter Mobile App)                                      |
|    - Target User: Patients                                                        |
|    - Scope: Patient-authorized personal data context                              |
|    - Purpose: Self-service timeline navigation & personal record Q&A              |
|    - Boundary: No unrestricted clinical access, no diagnosis, no prescriptions   |
|                                                                                   |
| 2. MEDIVERSE HEALTHCARE RECORD AGENT (React Web Doctor Portal)                    |
|    - Target User: Authorized Healthcare Professionals (Doctors, Medical Staff)    |
|    - Scope: Authorized patient clinical records under RBAC & patient consent      |
|    - Purpose: Source-grounded RAG record summarization & lab trend synthesis      |
|    - Boundary: No autonomous diagnosis, no autonomous treatment/prescriptions    |
+-----------------------------------------------------------------------------------+
```

---

## 9.2 CORE AI USE CASES & CAPABILITIES

### 1. Patient AI Assistant (Mobile Flutter App)
- **Target Audience:** Registered Patients using the mobile app.
- **Data Access Context:** Restricted strictly to records and documents owned by or explicitly authorized for the authenticated patient.
- **Capabilities:**
  - Navigating personal medical history in plain language.
  - Explaining complex medical terms found in own lab reports or discharge summaries.
  - Summarizing personal prescription histories and doctor advice.
- **Strict Guardrails:** Must NOT provide unrestricted clinical advice, must NOT perform medical diagnosis, and must NOT suggest drug prescriptions.

### 2. Mediverse Healthcare Record Agent (Web Doctor Portal)
- **Target Audience:** Authorized Healthcare Professionals (Doctors, Hospital Medical Staff) using the React Web Portal.
- **Data Access Context:** Operates strictly over patient records authorized via active patient consent or valid clinical context.
- **Capabilities:**
  - **Chronological Health Timeline Summarization:** Synthesizes past doctor consultations, hospital discharge summaries, and uploaded documents into structured medical timelines.
  - **Diagnostic Lab Result Synthesis:** Analyzes historical lab report values (e.g., tracking HbA1c over 12 months) to identify trends, flag out-of-range metrics, and format tabular summaries.
  - **Source-Grounded Question Answering (RAG):** Allows doctors to query specific patient history questions with direct citations linking back to original clinical source documents.
- **Strict Guardrails:** Respects RBAC, consent, and audit requirements. Must NOT diagnose autonomously, prescribe drugs, or make autonomous clinical treatment decisions.

---

## 9.3 TECHNICAL ARCHITECTURE & RAG PIPELINE

The RAG workflow for the Mediverse Healthcare Record Agent follows a multi-stage retrieval architecture:

```
+-----------------------------------------------------------------------------------+
|                             RAG & OCR PIPELINE ARCHITECTURE                       |
+-----------------------------------------------------------------------------------+
| [ Scanned PDF / Image Upload ]                                                    |
|        |                                                                          |
|        v                                                                          |
| [ OCR Engine ] ------------> Extracted Plaintext                                  |
|                                     |                                             |
|                                     v                                             |
|                             [ Text Chunking & Embeddings ]                        |
|                                     |                                             |
|                                     v                                             |
|                             [ Vector Store Index ]                                |
|                                     |                                             |
| [ Doctor AI Query ]                 v                                             |
|        |                  [ Similarity Retrieval ]                                |
|        |                            |                                             |
|        v                            v                                             |
| [ RBAC & Consent Engine ] -> [ Relevant Context Chunks ]                          |
|                                     |                                             |
|                                     v                                             |
|                             [ LLM Prompt Synthesis ]                              |
|                                     |                                             |
|                                     v                                             |
|                             [ Grounded AI Summary + Citations ]                   |
+-----------------------------------------------------------------------------------+
```

---

## 9.4 OCR / DOCUMENT INTELLIGENCE WORKFLOW

```
Upload Scanned Document 
  ↳ File Type & Integrity Validation 
  ↳ Save File to Supabase Private Bucket 
  ↳ Pass File Stream to OCR Engine 
  ↳ Extract Raw Text & Confidence Metrics 
  ↳ Parse Key-Value Metadata (Date, Facility, Metrics) 
  ↳ Generate Vector Embeddings 
  ↳ Store Metadata in MySQL & Embeddings in Vector Store 
  ↳ Mark Document "AI-Searchable" in Timeline
```

---

## 9.5 STRICT AI SAFETY GUARDRAILS & BOUNDARIES

To prevent patient harm, liability risks, and clinical errors, both AI components must strictly enforce the following non-negotiable guardrails:

```
+-----------------------------------------------------------------------------------+
|                             STRICT AI BOUNDARY RULES                              |
+-----------------------------------------------------------------------------------+
| ⛔ RULE 1: ZERO AUTONOMOUS DIAGNOSIS   -> AI cannot diagnose conditions.          |
| ⛔ RULE 2: ZERO AUTONOMOUS PRESCRIPTION-> AI cannot issue prescriptions or drugs. |
| ⛔ RULE 3: ZERO DOCTOR OVERRIDE        -> AI cannot override human physicians.     |
| ⛔ RULE 4: ZERO SECURITY BYPASS        -> AI cannot access unapproved records.    |
| ⛔ RULE 5: MANDATORY SOURCE CITATION   -> Healthcare Agent must cite source IDs.  |
| ⛔ RULE 6: ASSISTANT DISCLAIMER        -> AI outputs are assistive, not final.    |
+-----------------------------------------------------------------------------------+
```

1. **NO Autonomous Diagnosis:** Neither AI model may provide independent or definitive medical diagnoses to patients or doctors.
2. **NO Autonomous Treatment / Prescriptions:** Neither AI model may generate autonomous treatment plans, suggest drug dosages, or issue e-prescriptions.
3. **NO Overriding Human Physicians:** AI suggestions are strictly secondary inputs. The attending doctor maintains 100% decision authority.
4. **NO Bypassing Authorization:** The AI RAG engine MUST NOT retrieve or process documents for a patient unless the requesting user has verified RBAC and active consent permissions.
5. **Mandatory Source Citation:** Every summary statement generated by the Mediverse Healthcare Record Agent must cite underlying source document IDs and dates.
6. **Mandatory Clinical Disclaimer:** All AI-generated responses displayed on the Doctor or Patient portals must include an explicit disclaimer:
   > *"Notice: AI summaries are generated for informational assistance only. They do not constitute a formal medical diagnosis or treatment plan. Always consult a qualified physician for clinical decisions."*
