# 17 — REGISTER OF PHASE 2 OPEN QUESTIONS & FUTURE DECISIONS

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 17.1 OPEN QUESTION GOVERNANCE

The following technical decisions remain open and are explicitly deferred to Phase 3 design/implementation specifications. No unapproved assumptions have been made.

```
+-----------------------------------------------------------------------------------+
|                     REGISTER OF PHASE 2 OPEN QUESTIONS                            |
+-----------------------------------------------------------------------------------+
| OQ-01: Emergency Override Session Expiration Timeout                              |
| OQ-02: Patient Consent Handshake Protocol (OTP vs. In-App vs. QR Scan)            |
| OQ-03: Medical Organization & Diagnostic Lab Sub-Role Hierarchies                 |
| OQ-04: QR Code Security Payload Digital Signature Algorithm (HMAC vs. RSA)        |
| OQ-05: Selected OCR Engine for PDF Parsing (Tesseract vs. AWS Textract)           |
| OQ-06: Selected LLM Provider (Groq / OpenAI / Local Ollama)                       |
| OQ-07: Selected Vector Store (pgvector vs. Qdrant vs. Chroma)                     |
| OQ-08: Real-Time Patient Push Notification Engine                                 |
| OQ-09: Standardized Medical Document Category Taxonomy                            |
| OQ-10: Medical Certificate & Discharge Summary Digital Signature Standard         |
+-----------------------------------------------------------------------------------+
```

---

## 17.2 DETAILED OPEN DECISION SHEET

1. **OQ-01: Emergency Session Expiration Timeout:** Fixed 15-min session vs. 30-min auto-expiring session vs. single lookup. (Deferred to Phase 3 Security Config).
2. **OQ-02: Patient Consent Handshake Protocol:** In-App Push Notification vs. SMS OTP vs. Patient QR Scan. (Deferred to Phase 3 API Config).
3. **OQ-03: Medical/Lab Sub-Roles:** Single role (`MEDICAL`, `LAB`) vs. granular sub-roles (`LAB_TECH`, `HEAD_PATHOLOGIST`). (Deferred to Phase 3 Entity Config).
4. **OQ-04: QR Signature Algorithm:** HMAC-SHA256 vs. RSA-256 digital signature. (Deferred to Phase 3 Crypto Spec).
5. **OQ-05: OCR Engine Selection:** Open-source Tesseract vs. AWS Textract vs. EasyOCR. (Deferred to Phase 3 AI Pipeline).
6. **OQ-06: LLM Provider Selection:** Cloud Groq/OpenAI API vs. Local Ollama (Llama 3). (Deferred to Phase 3 AI Pipeline).
7. **OQ-07: Vector Store Implementation:** PostgreSQL `pgvector` extension vs. Qdrant vs. Chroma DB. (Deferred to Phase 3 Vector Spec).
8. **OQ-08: Push Notification Provider:** Firebase Cloud Messaging (FCM) vs. WebSockets. (Deferred to Phase 3 Integration).
9. **OQ-09: Document Category Taxonomy:** Enum array vs. dynamic category lookup table. (Deferred to Phase 3 Schema Finalization).
10. **OQ-10: Digital Certificate Signature Standard:** Server metadata hash vs. PKI PDF digital signatures. (Deferred to Phase 3 Spec).
