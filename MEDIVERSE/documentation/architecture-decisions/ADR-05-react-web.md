# ADR-05: REACT WEB PORTAL FRAMEWORK

## STATUS: ACCEPTED

### DECISION
Use React + HTML/CSS/JavaScript for building the four web portals (Doctor, Medical Organization, Laboratory, Admin).

### REASON
React provides component-driven UI architecture, fast virtual DOM rendering, widespread developer adoption, rich ecosystem for data tables and form handling, and clean state management suitable for complex clinical web dashboards.

### ALTERNATIVES CONSIDERED
- **Angular:** Rejected due to steeper learning curve and boilerplate overhead.
- **Thymeleaf / Server-Rendered HTML:** Rejected due to poor user experience compared to Single Page Applications (SPA).

### CONSEQUENCES
- Web client state must be managed cleanly using Context API or Redux.
- Protected routes must enforce client-side JWT token validation mirroring Spring Security rules.
