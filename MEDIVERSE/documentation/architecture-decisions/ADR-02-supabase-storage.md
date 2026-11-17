# ADR-02: SUPABASE PRIVATE OBJECT STORAGE FOR MEDICAL DOCUMENTS

## STATUS: ACCEPTED

### DECISION
Use Supabase Storage with **Private** object buckets for storing all binary medical documents (PDFs, lab reports, radiology images, scanned certificates).

### REASON
Storing large binary files directly in MySQL causes severe performance degradation and database bloat. Supabase Storage provides cloud-native S3-compatible private object storage with native API support for pre-signed short-lived URLs ($\le 15$ mins), ensuring documents remain completely private and inaccessible via public static URLs.

### ALTERNATIVES CONSIDERED
- **Storing BLOBs in MySQL:** Rejected due to performance degradation during database backup and query execution.
- **Local Server Filesystem:** Rejected due to lack of scalability and security risks associated with direct server file access.

### CONSEQUENCES
- Hybrid storage pattern required: binary files in Supabase Storage, file metadata in MySQL `file_metadata`.
- API backend must handle pre-signed URL generation following consent verification.
