package com.mediverse.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.PatientConsentRequestDto;
import com.mediverse.dto.PatientConsentResponseDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.ConsentStatus;
import com.mediverse.entity.DataCategory;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.PatientConsent;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.DoctorProfileRepository;
import com.mediverse.repository.PatientConsentRepository;
import com.mediverse.repository.PatientProfileRepository;
import com.mediverse.repository.UserRepository;

@Service
public class PatientConsentService {

    private final PatientConsentRepository patientConsentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public PatientConsentService(
            PatientConsentRepository patientConsentRepository,
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository) {
        this.patientConsentRepository = patientConsentRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Grants consent to an individual active, verified doctor provider.
     */
    @Transactional
    public PatientConsentResponseDto grantConsent(String patientUserId, PatientConsentRequestDto request) {
        validateSecurityContext(patientUserId, "PATIENT_MANAGE_CONSENT");

        if (patientUserId == null || patientUserId.isBlank()) {
            throw new IllegalArgumentException("Patient user ID must not be null or blank.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Consent request must not be null.");
        }
        if (request.getGrantedToUserId() == null || request.getGrantedToUserId().isBlank()) {
            throw new IllegalArgumentException("Grantee provider user ID is required.");
        }
        if (request.getDataCategory() == null) {
            throw new IllegalArgumentException("Data category is required.");
        }
        if (request.getExpiresAt() == null) {
            throw new IllegalArgumentException("Consent expiry timestamp is required.");
        }
        if (!request.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Consent expiry date must be in the future.");
        }

        // 1. Resolve patient profile
        PatientProfile patient = patientProfileRepository.findByUser_UserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + patientUserId));

        User patientUser = patient.getUser();
        if (patientUser == null || patientUser.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Patient account is not active.");
        }

        // 2. Prevent self-grant
        if (patientUserId.equals(request.getGrantedToUserId())) {
            throw new IllegalArgumentException("Patient cannot grant consent to themselves.");
        }

        // 3. Resolve grantee: must be an active, verified doctor
        DoctorProfile doctor = doctorProfileRepository.findByUser_UserId(request.getGrantedToUserId())
                .orElseThrow(() -> new IllegalArgumentException("Grantee must be a licensed doctor. No doctor found for user ID: " + request.getGrantedToUserId()));

        User doctorUser = doctor.getUser();
        if (doctorUser == null || doctorUser.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Grantee doctor account is not active.");
        }
        if (doctor.getVerificationStatus() != VerificationStatus.ACTIVE) {
            throw new IllegalStateException("Grantee doctor is not verified. Current status: " + doctor.getVerificationStatus());
        }

        // 4. Create and persist consent
        PatientConsent consent = new PatientConsent();
        consent.setPatient(patient);
        consent.setGrantedToUser(doctorUser);
        consent.setDataCategory(request.getDataCategory());
        consent.setStatus(ConsentStatus.ACTIVE);
        consent.setGrantedAt(LocalDateTime.now());
        consent.setExpiresAt(request.getExpiresAt());

        PatientConsent savedConsent = patientConsentRepository.save(consent);

        // 5. Audit logging in same transaction
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(patientUserId);
        auditLog.setTargetPatientId(patientUser.getUserId());
        auditLog.setAction("CONSENT_GRANT");
        String resourceAccessed = "/api/v1/consents/" + savedConsent.getConsentId() + " - Grantee: " + doctorUser.getUserId() + " - Category: " + savedConsent.getDataCategory();
        if (resourceAccessed.length() > 255) {
            resourceAccessed = resourceAccessed.substring(0, 255);
        }
        auditLog.setResourceAccessed(resourceAccessed);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return mapToDto(savedConsent);
    }

    /**
     * Lists all consent records for the authenticated patient.
     */
    @Transactional(readOnly = true)
    public List<PatientConsentResponseDto> getMyConsents(String patientUserId) {
        validateSecurityContext(patientUserId, "PATIENT_MANAGE_CONSENT");

        if (patientUserId == null || patientUserId.isBlank()) {
            throw new IllegalArgumentException("Patient user ID must not be null or blank.");
        }
        patientProfileRepository.findByUser_UserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + patientUserId));

        List<PatientConsent> consents = patientConsentRepository.findAllByPatient_User_UserIdOrderByGrantedAtDesc(patientUserId);
        return consents.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Revokes an existing consent belonging to the authenticated patient.
     */
    @Transactional
    public PatientConsentResponseDto revokeConsent(String patientUserId, String consentId) {
        validateSecurityContext(patientUserId, "PATIENT_MANAGE_CONSENT");

        if (patientUserId == null || patientUserId.isBlank()) {
            throw new IllegalArgumentException("Patient user ID must not be null or blank.");
        }
        if (consentId == null || consentId.isBlank()) {
            throw new IllegalArgumentException("Consent ID must not be null or blank.");
        }

        PatientConsent consent = patientConsentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with ID: " + consentId));

        // Strict ownership check
        if (consent.getPatient() == null || consent.getPatient().getUser() == null
                || !patientUserId.equals(consent.getPatient().getUser().getUserId())) {
            throw new AccessDeniedException("User is not authorized to revoke this consent.");
        }

        if (consent.getStatus() != ConsentStatus.REVOKED) {
            consent.setStatus(ConsentStatus.REVOKED);
            consent.setRevokedAt(LocalDateTime.now());
            consent = patientConsentRepository.save(consent);

            // Audit logging
            AuditLog auditLog = new AuditLog();
            auditLog.setActorUserId(patientUserId);
            auditLog.setTargetPatientId(consent.getPatient().getUser().getUserId());
            auditLog.setAction("CONSENT_REVOKE");
            String resourceAccessed = "/api/v1/consents/" + consentId + " - Status: REVOKED";
            if (resourceAccessed.length() > 255) {
                resourceAccessed = resourceAccessed.substring(0, 255);
            }
            auditLog.setResourceAccessed(resourceAccessed);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        }

        return mapToDto(consent);
    }

    /**
     * Evaluates whether an individual provider user possesses active, unexpired, unrevoked consent
     * for a patient and applicable data category.
     *
     * @param patientId        Target patient profile ID
     * @param providerUserId   Provider user ID (doctor)
     * @param requiredCategory Required category (CONSULT_ONLY, LAB_ONLY, or null for any)
     * @return Optional containing matching active consent, or empty
     */
    @Transactional(readOnly = true)
    public Optional<PatientConsent> validateAndGetActiveConsent(String patientId, String providerUserId, DataCategory requiredCategory) {
        if (patientId == null || providerUserId == null) {
            return Optional.empty();
        }
        LocalDateTime now = LocalDateTime.now();

        Optional<PatientConsent> consentOpt = patientConsentRepository
                .findFirstByPatient_PatientIdAndGrantedToUser_UserIdAndStatusAndExpiresAtAfterAndRevokedAtIsNullOrderByGrantedAtDesc(
                        patientId, providerUserId, ConsentStatus.ACTIVE, now);

        if (consentOpt.isEmpty()) {
            return Optional.empty();
        }

        PatientConsent consent = consentOpt.get();
        // FULL_TIMELINE satisfies all categories
        if (consent.getDataCategory() == DataCategory.FULL_TIMELINE) {
            return Optional.of(consent);
        }

        // If specific category required, verify match
        if (requiredCategory == null || consent.getDataCategory() == requiredCategory) {
            return Optional.of(consent);
        }

        return Optional.empty();
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

    private PatientConsentResponseDto mapToDto(PatientConsent consent) {
        String patientId = consent.getPatient() != null ? consent.getPatient().getPatientId() : null;
        String patientName = consent.getPatient() != null && consent.getPatient().getUser() != null
                ? consent.getPatient().getUser().getFullName() : null;

        String granteeUserId = consent.getGrantedToUser() != null ? consent.getGrantedToUser().getUserId() : null;
        String granteeName = consent.getGrantedToUser() != null ? consent.getGrantedToUser().getFullName() : null;
        String granteeRole = consent.getGrantedToUser() != null && consent.getGrantedToUser().getRole() != null
                ? consent.getGrantedToUser().getRole().getRoleName() : null;

        return new PatientConsentResponseDto(
                consent.getConsentId(),
                patientId,
                patientName,
                granteeUserId,
                granteeName,
                granteeRole,
                consent.getDataCategory() != null ? consent.getDataCategory().name() : null,
                consent.getStatus() != null ? consent.getStatus().name() : null,
                consent.getGrantedAt(),
                consent.getExpiresAt(),
                consent.getRevokedAt()
        );
    }
}
