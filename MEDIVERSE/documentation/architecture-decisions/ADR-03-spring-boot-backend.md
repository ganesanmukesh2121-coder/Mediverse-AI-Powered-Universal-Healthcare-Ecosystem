# ADR-03: SPRING BOOT (JAVA 21) MODULAR BACKEND FRAMEWORK

## STATUS: ACCEPTED

### DECISION
Use Spring Boot (Java 21) as the unified REST API backend framework structured as a Modular Monolith.

### REASON
Spring Boot provides industry-standard security (Spring Security), seamless relational data access (Spring Data JPA), declarative transaction management (`@Transactional`), robust dependency injection, and clean module packaging suitable for complex healthcare enterprise platforms.

### ALTERNATIVES CONSIDERED
- **Node.js / Express:** Rejected due to lack of compile-time type safety and weaker out-of-the-box enterprise security frameworks compared to Spring Security.
- **Microservices Architecture:** Rejected as unnecessarily complex for a final-year project baseline; Modular Monolith provides clean boundaries without distributed system operational overhead.

### CONSEQUENCES
- Requires strict package structure discipline across backend modules.
- Memory consumption higher than lightweight Go/Node apps, but well within server capacity.
