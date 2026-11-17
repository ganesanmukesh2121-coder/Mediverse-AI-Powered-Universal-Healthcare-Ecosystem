# ADR-12: DUAL AI ARCHITECTURE & SAFETY BOUNDARIES

## STATUS: ACCEPTED

### DECISION
Implement a Dual AI Architecture separating the **Patient AI Assistant** (Flutter Mobile) from the **Mediverse Healthcare Record Agent** (React Web Doctor Portal), governed by strict non-negotiable safety boundaries.

### REASON
Patients and healthcare providers have vastly different information needs and safety parameters. Patients require plain-language explanations of their own medical records without diagnostic speculation. Physicians require source-grounded RAG timeline summaries with document citations to digest complex patient histories quickly.

### ALTERNATIVES CONSIDERED
- **Single Universal AI Chatbot:** Rejected due to high risk of leaking clinical complexity to patients or providing non-grounded summaries to doctors.

### CONSEQUENCES
- Both AI models are strictly prohibited from autonomous medical diagnosis, prescribing, or overriding healthcare professionals.
- All AI queries execute through Spring Boot APIs enforcing RBAC, consent, and audit logging.
