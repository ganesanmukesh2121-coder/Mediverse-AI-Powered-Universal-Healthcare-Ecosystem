package com.mediverse.service;

import java.security.SecureRandom;
import java.time.Year;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.RegisterRequestDto;
import com.mediverse.dto.RegisterResponseDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.Gender;
import com.mediverse.entity.HealthIdRegistry;
import com.mediverse.entity.HealthIdStatus;
import com.mediverse.entity.LaboratoryProfile;
import com.mediverse.entity.MedicalOrgProfile;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;
import com.mediverse.repository.DoctorProfileRepository;
import com.mediverse.repository.HealthIdRegistryRepository;
import com.mediverse.repository.LaboratoryProfileRepository;
import com.mediverse.repository.MedicalOrgProfileRepository;
import com.mediverse.repository.PatientProfileRepository;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HealthIdRegistryRepository healthIdRegistryRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final MedicalOrgProfileRepository medicalOrgProfileRepository;
    private final LaboratoryProfileRepository laboratoryProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new SecureRandom();

    public RegistrationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            HealthIdRegistryRepository healthIdRegistryRepository,
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            MedicalOrgProfileRepository medicalOrgProfileRepository,
            LaboratoryProfileRepository laboratoryProfileRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.healthIdRegistryRepository = healthIdRegistryRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.medicalOrgProfileRepository = medicalOrgProfileRepository;
        this.laboratoryProfileRepository = laboratoryProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponseDto registerUser(RegisterRequestDto request) {
        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }

        String normalizedRole = request.getRole().trim().toUpperCase().replace("ROLE_", "");

        if ("ADMIN".equals(normalizedRole)) {
            throw new IllegalArgumentException("ADMIN registration is strictly prohibited.");
        }

        if ("EMERGENCY".equals(normalizedRole)) {
            throw new IllegalArgumentException("EMERGENCY role registration is not publicly available.");
        }

        if (!"PATIENT".equals(normalizedRole) && !"DOCTOR".equals(normalizedRole)
                && !"MEDICAL".equals(normalizedRole) && !"LAB".equals(normalizedRole)) {
            throw new IllegalArgumentException("Invalid role specified.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        Role role = roleRepository.findByRoleName(normalizedRole)
                .orElseGet(() -> roleRepository.save(new Role(normalizedRole, normalizedRole + " role")));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(role);

        String healthId = null;

        if ("PATIENT".equals(normalizedRole)) {
            user.setAccountStatus(AccountStatus.ACTIVE);
            user = userRepository.save(user);

            healthId = generateUniqueHealthId();

            HealthIdRegistry hir = new HealthIdRegistry();
            hir.setHealthId(healthId);
            hir.setPatientUser(user);
            hir.setStatus(HealthIdStatus.ACTIVE);
            healthIdRegistryRepository.save(hir);
            user.setHealthIdRegistry(hir);

            PatientProfile profile = new PatientProfile();
            profile.setUser(user);
            profile.setHealthId(healthId);
            profile.setDateOfBirth(request.getDateOfBirth());
            if (request.getGender() != null && !request.getGender().isBlank()) {
                try {
                    profile.setGender(Gender.valueOf(request.getGender().trim().toUpperCase()));
                } catch (Exception e) {
                    // Ignore or fallback
                }
            }
            profile.setBloodGroup(request.getBloodGroup());
            profile.setEmergencyContactPhone(request.getEmergencyContactPhone());
            profile.setAddress(request.getAddress());
            patientProfileRepository.save(profile);

        } else if ("DOCTOR".equals(normalizedRole)) {
            user.setAccountStatus(AccountStatus.PENDING);
            user = userRepository.save(user);

            DoctorProfile profile = new DoctorProfile();
            profile.setUser(user);
            profile.setLicenseNumber(request.getLicenseNumber() != null ? request.getLicenseNumber() : "PENDING");
            profile.setSpecialization(request.getSpecialization());
            profile.setHospitalAffiliation(request.getHospitalAffiliation());
            profile.setVerificationStatus(VerificationStatus.PENDING);
            doctorProfileRepository.save(profile);

        } else if ("MEDICAL".equals(normalizedRole)) {
            user.setAccountStatus(AccountStatus.PENDING);
            user = userRepository.save(user);

            MedicalOrgProfile profile = new MedicalOrgProfile();
            profile.setUser(user);
            profile.setOrganizationName(request.getOrganizationName() != null ? request.getOrganizationName() : request.getFullName());
            profile.setRegistrationNumber(request.getRegistrationNumber() != null ? request.getRegistrationNumber() : "PENDING");
            profile.setFacilityType(request.getFacilityType());
            profile.setVerificationStatus(VerificationStatus.PENDING);
            medicalOrgProfileRepository.save(profile);

        } else if ("LAB".equals(normalizedRole)) {
            user.setAccountStatus(AccountStatus.PENDING);
            user = userRepository.save(user);

            LaboratoryProfile profile = new LaboratoryProfile();
            profile.setUser(user);
            profile.setLabName(request.getLabName() != null ? request.getLabName() : request.getFullName());
            profile.setAccreditationNumber(request.getAccreditationNumber() != null ? request.getAccreditationNumber() : "PENDING");
            profile.setVerificationStatus(VerificationStatus.PENDING);
            laboratoryProfileRepository.save(profile);
        }

        String message = "PATIENT".equals(normalizedRole)
                ? "Patient registered successfully."
                : "Provider registration submitted successfully and is pending verification.";

        return new RegisterResponseDto(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                normalizedRole,
                user.getAccountStatus().name(),
                healthId,
                message
        );
    }

    private String generateUniqueHealthId() {
        String year = String.valueOf(Year.now().getValue());
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String healthId;
        do {
            StringBuilder sb = new StringBuilder("MV-").append(year).append("-");
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            healthId = sb.toString();
        } while (healthIdRegistryRepository.existsByHealthId(healthId));

        return healthId;
    }
}
