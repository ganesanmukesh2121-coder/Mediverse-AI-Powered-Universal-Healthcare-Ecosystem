# 11 — FILE STORAGE ARCHITECTURE SPECIFICATION

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 11.1 DUAL STORAGE ARCHITECTURE

Mediverse segregates structured relational data from binary file objects using a hybrid dual storage pattern:

```
+-----------------------------------------------------------------------------------+
|                            DUAL FILE STORAGE PATTERN                              |
+-----------------------------------------------------------------------------------+
| UNSTRUCTURED BINARY FILES                 STRUCTURED FILE METADATA                |
| (Private Supabase Storage Object Buckets) (MySQL 8.x `file_metadata` Table)       |
| ├── PDFs (Lab reports, Discharge papers)  ├── file_id (CHAR(36) UUID v4)          |
| ├── Scanned Documents (Patient uploads)   ├── patient_id (CHAR(36) UUID v4)       |
| ├── JPEGs / PNGs (Radiology, ECG scans)   ├── uploaded_by_user_id (CHAR(36) UUID)  |
| └── Medical Certificates                  ├── original_file_name                  |
|                                           ├── mime_type                           |
|                                           ├── file_size_bytes                     |
|                                           ├── supabase_storage_path               |
|                                           ├── document_category                   |
|                                           └── created_at                          |
+-----------------------------------------------------------------------------------+
```

---

## 11.2 PRIVATE STORAGE ACCESS RULES

1. **Private Buckets Only:** Supabase Storage buckets (`patient-documents`, `lab-reports`, `hospital-files`) are configured as **PRIVATE**. Public bucket access is completely disabled.
2. **No Permanent Public URLs:** Direct public access URLs (e.g. `https://supabase.co/storage/v1/object/public/...`) are forbidden.
3. **Short-Lived Signed/Temporary Storage Access:** File retrieval requires the Spring Boot backend to validate user identity, RBAC, and active patient consent before granting short-lived signed/temporary storage access (with a short expiration duration, e.g. $\le 15$ mins).

---

## 11.3 FILE UPLOAD & RETRIEVAL FLOWS

```
+-----------------------------------------------------------------------------------+
|                             FILE UPLOAD SEQUENCE                                  |
+-----------------------------------------------------------------------------------+
| 1. Client posts file binary + metadata to `POST /api/v1/files/upload`.            |
| 2. Spring Boot backend validates MIME type (`application/pdf`, `image/jpeg`), size|
|    limit ($\le 15 \text{ MB}$), and user authorization permissions.                |
| 3. Backend uploads binary stream to Private Supabase Storage bucket.              |
| 4. Backend inserts metadata row into MySQL `file_metadata` table.                 |
| 5. Backend returns `file_id` and metadata confirmation to client.                 |
+-----------------------------------------------------------------------------------+
```

```
+-----------------------------------------------------------------------------------+
|                            FILE DOWNLOAD SEQUENCE                                 |
+-----------------------------------------------------------------------------------+
| 1. Client requests file view: `GET /api/v1/files/{fileId}/presigned-url`.        |
| 2. Spring Boot validates JWT, RBAC, and active patient consent in `patient_consents`|
| 3. If authorized, backend provides short-lived signed/temporary storage access    |
|    URL (Valid for 15 minutes).                                                    |
| 4. Backend logs append-only audit log entry (`FILE_DOWNLOAD_REQUEST`).            |
| 5. Client receives short-lived signed URL to render/view file securely.           |
+-----------------------------------------------------------------------------------+
```
