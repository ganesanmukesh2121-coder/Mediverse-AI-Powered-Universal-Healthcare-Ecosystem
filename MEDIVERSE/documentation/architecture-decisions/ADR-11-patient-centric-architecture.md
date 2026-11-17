# ADR-11: PATIENT-CENTRIC UNIFIED TIMELINE ARCHITECTURE

## STATUS: ACCEPTED

### DECISION
Design Mediverse around a unified patient-centric health record timeline where all consultation notes, digital prescriptions, lab reports, hospital admissions, and uploaded documents attach to the central `patient_id`.

### REASON
Traditional healthcare software isolates data inside siloed hospital databases or doctor EMR systems. Mediverse unifies all records under the patient's identity, providing a single source of truth accessible across providers subject to explicit patient consent.

### ALTERNATIVES CONSIDERED
- **Provider-Centric Architecture:** Rejected because records remain fragmented across individual clinic silos.

### CONSEQUENCES
- Database queries for timeline aggregation require efficient B-Tree composite indexing on `(patient_id, created_at)`.
