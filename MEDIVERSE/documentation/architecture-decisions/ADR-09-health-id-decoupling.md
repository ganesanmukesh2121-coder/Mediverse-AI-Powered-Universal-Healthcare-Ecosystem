# ADR-09: HEALTH ID DECOUPLING & BACKEND GENERATION

## STATUS: ACCEPTED

### DECISION
Generate Mediverse Health IDs (`MV-YYYY-XXXXXXXX`) strictly server-side upon patient registration and decouple the Health ID string from the internal database primary key (`user_id` UUID).

### REASON
Decoupling internal UUID primary keys from public Health ID strings prevents sequence enumeration attacks and allows patients to present an immutable lookup index to providers without exposing database internal keys. Furthermore, Health ID serves strictly as a lookup index identifier and NEVER grants implicit authorization to view records without patient consent.

### ALTERNATIVES CONSIDERED
- **Using Social Security / National ID as Health ID:** Rejected due to severe privacy risks and regulatory compliance issues.
- **Using Database Auto-Increment ID:** Rejected due to predictable enumeration vulnerabilities.

### CONSEQUENCES
- Backend must maintain a `health_id_registry` table enforcing a global MySQL unique index on `health_id`.
