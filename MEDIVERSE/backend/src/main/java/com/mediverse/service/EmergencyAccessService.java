package com.mediverse.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.EmergencyProfileResponseDto;
import com.mediverse.dto.UpdateEmergencyProfileRequestDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.EmergencyProfile;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.User;
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.EmergencyProfileRepository;
import com.mediverse.repository.PatientProfileRepository;
import com.mediverse.repository.UserRepository;

@Service
public class EmergencyAccessService {

    private final EmergencyProfileRepository emergencyProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public EmergencyAccessService(
            EmergencyProfileRepository emergencyProfileRepository,
            PatientProfileRepository patientProfileRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository) {
        this.emergencyProfileRepository = emergencyProfileRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Emergency break-glass access to minimal triage data by an authenticated emergency responder.
     *
     * <p>Security & Privacy Guarantees:
     * 1. Requires authenticated caller possessing the EMERGENCY role and EMERGENCY_VIEW_EMERGENCY_INFO permission.
     * 2. Possession of Health ID alone NEVER grants authorization.
     * 3. Emergency access reason/justification is mandatory.
     * 4. Returns strictly minimal triage info: allergies, critical meds, blood group, conditions.
     * 5. Populates bloodGroup and emergencyContactPhone from PatientProfile where they reside.
     * 6. Strictly excludes all historical clinical records, prescriptions, and files.
     * 7. Records a mandatory append-only audit log entry (relational audit; cryptographic non-repudiation not implemented).
     * 8. Distinct emergency context mechanism (CAD incident/dispatch entity) is NOT IMPLEMENTED due to schema freeze.
     */
    @Transactional
    public EmergencyProfileResponseDto getEmergencyProfileByHealthId(
            String emergencyActorUserId, String emergencyActorRole, String healthId, String emergencyReason) {

        validateSecurityContext(emergencyActorUserId, "EMERGENCY_VIEW_EMERGENCY_INFO");

        if (emergencyActorUserId == null || emergencyActorUserId.isBlank()) {
            throw new IllegalArgumentException("Emergency actor user ID must not be null or blank.");
        }
        if (healthId == null || healthId.isBlank()) {
            throw new IllegalArgumentException("Patient Health ID must not be null or blank.");
        }
        if (emergencyReason == null || emergencyReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Emergency access justification / reason is mandatory.");
        }

        // 1. Role verification: caller must possess EMERGENCY role
        String normalizedRole = emergencyActorRole != null
                ? emergencyActorRole.toUpperCase().replace("ROLE_", "").trim()
                : "";
        if (!"EMERGENCY".equals(normalizedRole)) {
            throw new AccessDeniedException("Access denied: Caller does not possess the EMERGENCY role.");
        }

        // 2. Account status check of emergency responder
        User emergencyUser = userRepository.findById(emergencyActorUserId)
                .orElseThrow(() -> new AccessDeniedException("Emergency responder account not found."));
        if (emergencyUser.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Emergency responder account is not active.");
        }

        // 3. Resolve Patient by Health ID locator
        PatientProfile patient = patientProfileRepository.findByHealthId(healthId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for Health ID: " + healthId));

        User patientUser = patient.getUser();
        if (patientUser == null || patientUser.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Target patient account is not active.");
        }

        // 4. Resolve Emergency Profile (minimal triage data)
        Optional<EmergencyProfile> epOpt = emergencyProfileRepository.findByPatientProfile_PatientId(patient.getPatientId());

        // 5. Mandatory Audit Logging
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(emergencyActorUserId);
        auditLog.setTargetPatientId(patientUser.getUserId());
        auditLog.setAction("EMERGENCY_PROFILE_VIEW");
        String resourceAccessed = "/api/v1/emergency/patients/" + healthId.trim() + " - Justification: " + emergencyReason.trim();
        if (resourceAccessed.length() > 255) {
            resourceAccessed = resourceAccessed.substring(0, 255);
        }
        auditLog.setResourceAccessed(resourceAccessed);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return mapToDto(patient, epOpt.orElse(null));
    }

    /**
     * Retrieves the emergency profile for the authenticated patient (Self-View).
     */
    @Transactional(readOnly = true)
    public EmergencyProfileResponseDto getPatientEmergencyProfile(String patientUserId) {
        validateSecurityContext(patientUserId, "PATIENT_VIEW_OWN_PROFILE");

        if (patientUserId == null || patientUserId.isBlank()) {
            throw new IllegalArgumentException("Patient user ID must not be null or blank.");
        }
        PatientProfile patient = patientProfileRepository.findByUser_UserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + patientUserId));

        Optional<EmergencyProfile> epOpt = emergencyProfileRepository.findByPatientProfile_PatientId(patient.getPatientId());
        return mapToDto(patient, epOpt.orElse(null));
    }

    /**
     * Updates the emergency profile triage information for the authenticated patient.
     */
    @Transactional
    public EmergencyProfileResponseDto updatePatientEmergencyProfile(
            String patientUserId, UpdateEmergencyProfileRequestDto request) {

        validateSecurityContext(patientUserId, "PATIENT_UPDATE_OWN_PROFILE");

        if (patientUserId == null || patientUserId.isBlank()) {
            throw new IllegalArgumentException("Patient user ID must not be null or blank.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Update request must not be null.");
        }

        PatientProfile patient = patientProfileRepository.findByUser_UserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + patientUserId));

        EmergencyProfile ep = emergencyProfileRepository.findByPatientProfile_PatientId(patient.getPatientId())
                .orElseGet(() -> {
                    EmergencyProfile newEp = new EmergencyProfile();
                    newEp.setPatientProfile(patient);
                    return newEp;
                });

        ep.setSevereAllergies(request.getSevereAllergies());
        ep.setActiveCriticalMedications(request.getActiveCriticalMedications());
        ep.setChronicConditions(request.getChronicConditions());
        ep.setEmergencyNotes(request.getEmergencyNotes());

        EmergencyProfile saved = emergencyProfileRepository.save(ep);

        // Audit update
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(patientUserId);
        auditLog.setTargetPatientId(patient.getUser().getUserId());
        auditLog.setAction("EMERGENCY_PROFILE_UPDATE");
        auditLog.setResourceAccessed("/api/v1/patients/me/emergency-profile");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return mapToDto(patient, saved);
    }

    private void validateSecurityContext(String expectedUserId, String requiredPermission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("User is not authenticated.");
        }
        String authenticatedUserId = auth.getName();
        if (expectedUserId != null && !expectedUserId.isBlank() && !expectedUserId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Authenticated user does not match the requested actor ID.");
        }
        if (requiredPermission != null) {
            boolean hasPerm = auth.getAuthorities().stream()
                    .anyMatch(a -> requiredPermission.equals(a.getAuthority()));
            if (!hasPerm) {
                throw new AccessDeniedException("Access denied: Missing required permission " + requiredPermission);
            }
        }
    }

    private EmergencyProfileResponseDto mapToDto(PatientProfile patient, EmergencyProfile ep) {
        String patientName = patient.getUser() != null ? patient.getUser().getFullName() : null;

        Long epId = ep != null ? ep.getEmergencyProfileId() : null;
        String allergies = ep != null ? ep.getSevereAllergies() : null;
        String meds = ep != null ? ep.getActiveCriticalMedications() : null;
        String conditions = ep != null ? ep.getChronicConditions() : null;
        String notes = ep != null ? ep.getEmergencyNotes() : null;
        LocalDateTime updated = ep != null ? ep.getUpdatedAt() : (patient.getUpdatedAt() != null ? patient.getUpdatedAt() : LocalDateTime.now());

        return new EmergencyProfileResponseDto(
                epId,
                patient.getPatientId(),
                patient.getHealthId(),
                patientName,
                patient.getBloodGroup(),             // Populated from PatientProfile
                patient.getEmergencyContactPhone(),  // Populated from PatientProfile
                allergies,
                meds,
                conditions,
                notes,
                updated
        );
    }
}
