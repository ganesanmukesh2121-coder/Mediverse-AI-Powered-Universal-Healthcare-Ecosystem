# ADR-01: MYSQL 8.X RELATIONAL DATABASE ENGINE

## STATUS: ACCEPTED

### DECISION
Use MySQL 8.x as the primary relational database management system for structured domain entities, user accounts, Health ID registries, consent rules, file metadata, and append-only audit logs.

### REASON
MySQL 8.x provides robust ACID transaction compliance (InnoDB engine), full Unicode `utf8mb4` support, strong foreign key relational integrity, excellent performance for read-heavy healthcare workloads, and extensive ecosystem tooling suitable for enterprise modular monolith architectures.

### ALTERNATIVES CONSIDERED
- **PostgreSQL:** Excellent alternative, but MySQL is selected for simple deployment alignment in final-year B.Tech IT curriculum standards and native performance with Spring Data JPA.
- **MongoDB (NoSQL):** Rejected due to lack of native ACID multi-table foreign key constraints necessary for medical record governance.

### CONSEQUENCES
- Schema changes require structured SQL migration scripts.
- Binary document objects must be stored off-database in Supabase Storage to avoid database bloat.
