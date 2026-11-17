# SYSTEM ARCHITECTURE MERMAID DIAGRAM

```mermaid
graph TD
    subgraph Presentation_Tier ["Presentation Tier"]
        FlutterMobile["Flutter Mobile App<br/>(Patient & Emergency)"]
        ReactWeb["React Web Portals<br/>(Doctor, Medical, Lab, Admin)"]
    end

    subgraph Backend_API_Tier ["Backend API Tier (Spring Boot Java 21)"]
        RestGateway["REST Controller Gateway"]
        AuthFilter["JWT & Security Filter"]
        RbacGuard["RBAC Authorization Guard"]
        ConsentEngine["Patient Consent & Emergency Engine"]
        ModularServices["Modular Business Logic Services"]
        AuditLogger["Append-Only Security Audit Logger"]
    end

    subgraph Data_Storage_AI_Tier ["Data, Storage & AI Tier"]
        MySQL [("MySQL 8.x Database<br/>- Relational Entities<br/>- Health ID Registry<br/>- Append-Only Audit Logs")]
        Supabase [("Supabase Storage<br/>- Private File Buckets<br/>- Encrypted Medical PDFs/Scans")]
        AiEngine ["Dual AI Subsystem<br/>- Patient AI Assistant<br/>- Healthcare Record Agent (RAG)"]
    end

    FlutterMobile -->|HTTPS / REST| RestGateway
    ReactWeb -->|HTTPS / REST| RestGateway

    RestGateway --> AuthFilter
    AuthFilter --> RbacGuard
    RbacGuard --> ConsentEngine
    ConsentEngine --> ModularServices
    ModularServices --> AuditLogger

    ModularServices -->|Spring Data JPA| MySQL
    ModularServices -->|Pre-Signed URLs| Supabase
    ModularServices -->|Consented Context| AiEngine
    AuditLogger -->|Insert Only| MySQL
```
