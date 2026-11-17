# ADR-04: FLUTTER MOBILE APPLICATION FRAMEWORK

## STATUS: ACCEPTED

### DECISION
Use Flutter + Dart for developing the Patient Portal and Emergency Portal mobile applications.

### REASON
Flutter provides cross-platform compilation (iOS and Android) from a single Dart codebase, high-performance 60fps rendering, rich material design UI widgets, robust local secure storage plugins for JWT tokens, and hardware integration for camera-based QR code scanning and biometrics.

### ALTERNATIVES CONSIDERED
- **React Native:** Good alternative, but Flutter provides superior rendering consistency and smoother mobile UI performance.
- **Native Android (Kotlin):** Rejected due to double development effort required for iOS support.

### CONSEQUENCES
- Team must maintain Dart codebase alongside React JavaScript/TypeScript web portals.
