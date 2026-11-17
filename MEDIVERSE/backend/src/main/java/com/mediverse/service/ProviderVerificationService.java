package com.mediverse.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.PendingProviderResponseDto;
import com.mediverse.dto.ProviderVerificationRequestDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.LaboratoryProfile;
import com.mediverse.entity.MedicalOrgProfile;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.DoctorProfileRepository;
import com.mediverse.repository.LaboratoryProfileRepository;
import com.mediverse.repository.MedicalOrgProfileRepository;
import com.mediverse.repository.UserRepository;

@Service
public class ProviderVerificationService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final MedicalOrgProfileRepository medicalOrgProfileRepository;
    private final LaboratoryProfileRepository laboratoryProfileRepository;
    private final AuditLogRepository auditLogRepository;

    public ProviderVerificationService(
            UserRepository userRepository,
            DoctorProfileRepository doctorProfileRepository,
            MedicalOrgProfileRepository medicalOrgProfileRepository,
            LaboratoryProfileRepository laboratoryProfileRepository,
            AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.medicalOrgProfileRepository = medicalOrgProfileRepository;
        this.laboratoryProfileRepository = laboratoryProfileRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<PendingProviderResponseDto> getPendingProviders() {
        List<PendingProviderResponseDto> result = new ArrayList<>();

        // 1. Pending Doctors
        List<DoctorProfile> pendingDoctors = doctorProfileRepository.findAllByVerificationStatus(VerificationStatus.PENDING);
        for (DoctorProfile doctor : pendingDoctors) {
            result.add(mapDoctorToDto(doctor));
        }

        // 2. Pending Medical Organizations
        List<MedicalOrgProfile> pendingMedicalOrgs = medicalOrgProfileRepository.findAllByVerificationStatus(VerificationStatus.PENDING);
        for (MedicalOrgProfile medicalOrg : pendingMedicalOrgs) {
            result.add(mapMedicalOrgToDto(medicalOrg));
        }

        // 3. Pending Laboratories
        List<LaboratoryProfile> pendingLabs = laboratoryProfileRepository.findAllByVerificationStatus(VerificationStatus.PENDING);
        for (LaboratoryProfile lab : pendingLabs) {
            result.add(mapLabToDto(lab));
        }

        return result;
    }

    @Transactional
    public PendingProviderResponseDto verifyProvider(String userId, ProviderVerificationRequestDto request, String adminUserId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be null or blank.");
        }
        if (adminUserId == null || adminUserId.isBlank()) {
            throw new IllegalArgumentException("Admin user ID must not be null or blank.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Verification request must not be null.");
        }

        // Validate decision
        if (request.getDecision() == null || request.getDecision().isBlank()) {
            throw new IllegalArgumentException("Decision is required.");
        }
        String normalizedDecision = request.getDecision().trim().toUpperCase();
        if (!"ACTIVE".equals(normalizedDecision) && !"REJECTED".equals(normalizedDecision)) {
            throw new IllegalArgumentException("Invalid decision: " + request.getDecision() + ". Allowed decisions are ACTIVE or REJECTED.");
        }
        if ("REJECTED".equals(normalizedDecision) && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new IllegalArgumentException("Rejection reason is required when decision is REJECTED.");
        }

        // 1. Find provider user account
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider user account not found with ID: " + userId));

        // 2. Determine provider type and reject non-provider roles
        if (user.getRole() == null || user.getRole().getRoleName() == null) {
            throw new IllegalStateException("User does not have an assigned role.");
        }
        String roleName = user.getRole().getRoleName().toUpperCase().replace("ROLE_", "");
        if (!"DOCTOR".equals(roleName) && !"MEDICAL".equals(roleName) && !"LAB".equals(roleName)) {
            throw new IllegalArgumentException("User with role " + roleName + " is not a healthcare provider application.");
        }

        // 3. Confirm that account is PENDING
        if (user.getAccountStatus() != AccountStatus.PENDING) {
            throw new IllegalStateException("Cannot verify account that is not pending. Current status: " + user.getAccountStatus());
        }

        PendingProviderResponseDto updatedDto;
        String action;

        // 4. Update status according to provider role
        if ("DOCTOR".equals(roleName)) {
            DoctorProfile doctor = doctorProfileRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user ID: " + userId));

            if (doctor.getVerificationStatus() != VerificationStatus.PENDING) {
                throw new IllegalStateException("Doctor profile is not pending verification. Current status: " + doctor.getVerificationStatus());
            }

            if ("ACTIVE".equals(normalizedDecision)) {
                user.setAccountStatus(AccountStatus.ACTIVE);
                doctor.setVerificationStatus(VerificationStatus.ACTIVE);
                action = "PROVIDER_VERIFY_APPROVE";
            } else {
                user.setAccountStatus(AccountStatus.REJECTED);
                doctor.setVerificationStatus(VerificationStatus.REJECTED);
                action = "PROVIDER_VERIFY_REJECT";
            }

            userRepository.save(user);
            DoctorProfile savedDoctor = doctorProfileRepository.save(doctor);
            updatedDto = mapDoctorToDto(savedDoctor);

        } else if ("MEDICAL".equals(roleName)) {
            MedicalOrgProfile medical = medicalOrgProfileRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Medical organization profile not found for user ID: " + userId));

            if (medical.getVerificationStatus() != VerificationStatus.PENDING) {
                throw new IllegalStateException("Medical organization profile is not pending verification. Current status: " + medical.getVerificationStatus());
            }

            if ("ACTIVE".equals(normalizedDecision)) {
                user.setAccountStatus(AccountStatus.ACTIVE);
                medical.setVerificationStatus(VerificationStatus.ACTIVE);
                action = "PROVIDER_VERIFY_APPROVE";
            } else {
                user.setAccountStatus(AccountStatus.REJECTED);
                medical.setVerificationStatus(VerificationStatus.REJECTED);
                action = "PROVIDER_VERIFY_REJECT";
            }

            userRepository.save(user);
            MedicalOrgProfile savedMedical = medicalOrgProfileRepository.save(medical);
            updatedDto = mapMedicalOrgToDto(savedMedical);

        } else { // LAB
            LaboratoryProfile lab = laboratoryProfileRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Laboratory profile not found for user ID: " + userId));

            if (lab.getVerificationStatus() != VerificationStatus.PENDING) {
                throw new IllegalStateException("Laboratory profile is not pending verification. Current status: " + lab.getVerificationStatus());
            }

            if ("ACTIVE".equals(normalizedDecision)) {
                user.setAccountStatus(AccountStatus.ACTIVE);
                lab.setVerificationStatus(VerificationStatus.ACTIVE);
                action = "PROVIDER_VERIFY_APPROVE";
            } else {
                user.setAccountStatus(AccountStatus.REJECTED);
                lab.setVerificationStatus(VerificationStatus.REJECTED);
                action = "PROVIDER_VERIFY_REJECT";
            }

            userRepository.save(user);
            LaboratoryProfile savedLab = laboratoryProfileRepository.save(lab);
            updatedDto = mapLabToDto(savedLab);
        }

        // 5. Mandatory Audit Logging in the same transaction
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(adminUserId);
        auditLog.setTargetPatientId(userId);
        auditLog.setAction(action);

        String resourceAccessed = "/api/v1/admin/providers/" + userId + "/verify";
        if (request.getRejectionReason() != null && !request.getRejectionReason().isBlank()) {
            resourceAccessed = resourceAccessed + " - Reason: " + request.getRejectionReason().trim();
        }
        if (resourceAccessed.length() > 255) {
            resourceAccessed = resourceAccessed.substring(0, 255);
        }
        auditLog.setResourceAccessed(resourceAccessed);
        auditLog.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(auditLog);

        return updatedDto;
    }

    private PendingProviderResponseDto mapDoctorToDto(DoctorProfile doctor) {
        User user = doctor.getUser();
        return new PendingProviderResponseDto(
                user != null ? user.getUserId() : null,
                user != null ? user.getFullName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhoneNumber() : null,
                user != null && user.getRole() != null ? user.getRole().getRoleName() : "DOCTOR",
                user != null && user.getAccountStatus() != null ? user.getAccountStatus().name() : null,
                doctor.getVerificationStatus() != null ? doctor.getVerificationStatus().name() : null,
                user != null ? user.getFullName() : null,
                doctor.getHospitalAffiliation(),
                doctor.getLicenseNumber(),
                null,
                doctor.getSpecialization(),
                doctor.getHospitalAffiliation(),
                null,
                null,
                doctor.getCreatedAt() != null ? doctor.getCreatedAt() : (user != null ? user.getCreatedAt() : null)
        );
    }

    private PendingProviderResponseDto mapMedicalOrgToDto(MedicalOrgProfile medical) {
        User user = medical.getUser();
        return new PendingProviderResponseDto(
                user != null ? user.getUserId() : null,
                user != null ? user.getFullName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhoneNumber() : null,
                user != null && user.getRole() != null ? user.getRole().getRoleName() : "MEDICAL",
                user != null && user.getAccountStatus() != null ? user.getAccountStatus().name() : null,
                medical.getVerificationStatus() != null ? medical.getVerificationStatus().name() : null,
                medical.getOrganizationName(),
                medical.getOrganizationName(),
                null,
                medical.getRegistrationNumber(),
                null,
                null,
                medical.getFacilityType(),
                null,
                medical.getCreatedAt() != null ? medical.getCreatedAt() : (user != null ? user.getCreatedAt() : null)
        );
    }

    private PendingProviderResponseDto mapLabToDto(LaboratoryProfile lab) {
        User user = lab.getUser();
        return new PendingProviderResponseDto(
                user != null ? user.getUserId() : null,
                user != null ? user.getFullName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhoneNumber() : null,
                user != null && user.getRole() != null ? user.getRole().getRoleName() : "LAB",
                user != null && user.getAccountStatus() != null ? user.getAccountStatus().name() : null,
                lab.getVerificationStatus() != null ? lab.getVerificationStatus().name() : null,
                lab.getLabName(),
                lab.getLabName(),
                null,
                null,
                null,
                null,
                null,
                lab.getAccreditationNumber(),
                lab.getCreatedAt() != null ? lab.getCreatedAt() : (user != null ? user.getCreatedAt() : null)
        );
    }
}
