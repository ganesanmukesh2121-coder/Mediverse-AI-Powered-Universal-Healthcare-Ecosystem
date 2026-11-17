# 16 — CONCEPTUAL DEPLOYMENT ARCHITECTURE

> **PHASE 2 DESIGN ARTIFACT — NOT APPROVED FOR CODE IMPLEMENTATION**

---

## 16.1 CONCEPTUAL DEPLOYMENT TOPOLOGY

```
+-----------------------------------------------------------------------------------+
|                        CONCEPTUAL DEPLOYMENT ENVIRONMENT                          |
+-----------------------------------------------------------------------------------+
| CLIENT PLACEMENT:                                                                 |
|   Mobile Devices (iOS / Android)     |  Client Web Browsers (Chrome / Firefox)    |
|   ├── Patient Flutter App            |  ├── Doctor Portal (React Web)             |
|   └── Emergency Flutter App          |  ├── Medical Org Portal (React Web)        |
|                                      |  ├── Laboratory Portal (React Web)         |
|                                      |  └── Admin Portal (React Web)              |
|                                                                                   |
|                               ↓ TLS 1.3 / HTTPS                                   |
|                                                                                   |
| BACKEND CONTAINER / SERVER TIER:                                                  |
| [ Spring Boot REST API Service Container (Java 21 Runtime) ]                      |
|   ├── Embedded Tomcat Server (Port 8080)                                          |
|   ├── Spring Security Gateway & Consent Engine                                    |
|   └── HikariCP Database Connection Pool                                           |
|                                                                                   |
|       +-------------------------------+-------------------------------+           |
|       |                               |                               |           |
|       v                               v                               v           |
| DATA MANAGED TIER:            OBJECT STORAGE TIER:            EXTERNAL AI SERVICE:|
| [ Managed MySQL 8.x Database ] [ Supabase Storage Cloud ]     [ LLM / RAG Service ]|
| - Relational Entities         - Private Object Buckets        - Grounded Context  |
| - Append-only Audit Logs      - Pre-signed Timed URLs         - Zero Auto-Diag    |
+-----------------------------------------------------------------------------------+
```

---

## 16.2 DEPLOYMENT CONSTRAINTS (DESIGN ONLY)

- **Containerization Readiness:** Backend designed for standard Docker container packaging (`java -jar mediverse-backend.jar`).
- **No Active Deployment:** No cloud instances, Docker containers, MySQL servers, or Supabase buckets are to be provisioned during Phase 2.
