package com.mediverse.service;

import com.mediverse.dto.RegisterRequestDto;
import com.mediverse.dto.RegisterResponseDto;
import com.mediverse.entity.*;
import com.mediverse.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HealthIdRegistryRepository healthIdRegistryRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private MedicalOrgProfileRepository medicalOrgProfileRepository;

    @Autowired
    private LaboratoryProfileRepository laboratoryProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void test1_PatientRegistrationSucceeds() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("patient1_" + UUID.randomUUID() + "@mediverse.com");
        request.setPassword("SecretPass123!");
        request.setFullName("Jane Doe");
        request.setRole("PATIENT");
        request.setDateOfBirth(LocalDate.of(1995, 5, 15));
        request.setGender("FEMALE");
        request.setBloodGroup("O+");

        RegisterResponseDto response = registrationService.registerUser(request);

        assertNotNull(response);
        assertNotNull(response.getUserId());
        assertEquals("PATIENT", response.getRole());
        assertEquals("ACTIVE", response.getAccountStatus());
        assertNotNull(response.getHealthId());
    }

    @Test
    void test2_PatientBecomesActive() {
        String email = "patient2_" + UUID.randomUUID() + "@mediverse.com";
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(email);
        request.setPassword("SecretPass123!");
        request.setFullName("John Active");
        request.setRole("PATIENT");

        registrationService.registerUser(request);

        User user = userRepository.findByEmail(email).orElseThrow();
        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
    }

    @Test
    void test3_PatientProfileIsCreated() {
        String email = "patient3_" + UUID.randomUUID() + "@mediverse.com";
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(email);
        request.setPassword("SecretPass123!");
        request.setFullName("Alice Smith");
        request.setRole("PATIENT");

        registrationService.registerUser(request);

        User user = userRepository.findByEmail(email).orElseThrow();
        PatientProfile profile = patientProfileRepository.findAll().stream()
                .filter(p -> p.getUser().getUserId().equals(user.getUserId()))
                .findFirst()
                .orElse(null);

        assertNotNull(profile);
        assertNotNull(profile.getHealthId());
    }

    @Test
    void test4_5_6_HealthIdGeneratedAndStoredInRegistryWithApprovedFormat() {
        String email = "patient4_" + UUID.randomUUID() + "@mediverse.com";
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(email);
        request.setPassword("SecretPass123!");
        request.setFullName("Bob Builder");
        request.setRole("PATIENT");

        RegisterResponseDto response = registrationService.registerUser(request);
        String healthId = response.getHealthId();

        assertNotNull(healthId);
        String expectedPrefix = "MV-" + Year.now().getValue() + "-";
        assertTrue(healthId.startsWith(expectedPrefix), "Health ID should start with " + expectedPrefix + ", but was: " + healthId);
        assertEquals(expectedPrefix.length() + 8, healthId.length(), "Health ID length should be MV-YYYY- (11) + 8 chars = 19");

        assertTrue(healthIdRegistryRepository.existsByHealthId(healthId));
        HealthIdRegistry registry = healthIdRegistryRepository.findByHealthId(healthId).orElseThrow();
        assertEquals(HealthIdStatus.ACTIVE, registry.getStatus());
    }

    @Test
    void test7_PasswordIsBcryptHashed() {
        String email = "patient_pass_" + UUID.randomUUID() + "@mediverse.com";
        String rawPassword = "MySecretPassword123!";
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(email);
        request.setPassword(rawPassword);
        request.setFullName("Hash User");
        request.setRole("PATIENT");

        registrationService.registerUser(request);

        User user = userRepository.findByEmail(email).orElseThrow();
        assertNotEquals(rawPassword, user.getPasswordHash());
        assertTrue(passwordEncoder.matches(rawPassword, user.getPasswordHash()));
    }

    @Test
    void test8_DuplicateEmailIsRejected() {
        String email = "duplicate_" + UUID.randomUUID() + "@mediverse.com";
        RegisterRequestDto request1 = new RegisterRequestDto();
        request1.setEmail(email);
        request1.setPassword("SecretPass123!");
        request1.setFullName("First User");
        request1.setRole("PATIENT");

        registrationService.registerUser(request1);

        RegisterRequestDto request2 = new RegisterRequestDto();
        request2.setEmail(email);
        request2.setPassword("SecretPass123!");
        request2.setFullName("Second User");
        request2.setRole("PATIENT");

        assertThrows(IllegalArgumentException.class, () -> registrationService.registerUser(request2));
    }

    @Test
    void test9_DoctorRegistrationBecomesPending() {
        String email = "doc_" + UUID.randomUUID() + "@mediverse.com";
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(email);
        request.setPassword("SecretPass123!");
        request.setFullName("Dr. Strange");
        request.setRole("DOCTOR");
        request.setLicenseNumber("DOC-LIC-999");
        request.setSpecialization("Cardiology");

        RegisterResponseDto response = registrationService.registerUser(request);

        assertEquals("PENDING", response.getAccountStatus());
        assertNull(response.getHealthId());

        User user = userRepository.findByEmail(email).orElseThrow();
        assertEquals(AccountStatus.PENDING, user.getAccountStatus());

        DoctorProfile profile = doctorProfileRepository.findAll().stream()
                .filter(d -> d.getUser().getUserId().equals(user.getUserId()))
                .findFirst()
                .orElse(null);

        assertNotNull(profile);
        assertEquals(VerificationStatus.PENDING, profile.getVerificationStatus());
    }

    @Test
    void test10_MedicalRegistrationBecomesPending() {
        String email = "med_" + UUID.randomUUID() + "@mediverse.com";
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(email);
        request.setPassword("SecretPass123!");
        request.setFullName("City Hospital");
        request.setRole("MEDICAL");
        request.setOrganizationName("City Hospital");
        request.setRegistrationNumber("REG-HOSP-100");

        RegisterResponseDto response = registrationService.registerUser(request);

        assertEquals("PENDING", response.getAccountStatus());

        User user = userRepository.findByEmail(email).orElseThrow();
        assertEquals(AccountStatus.PENDING, user.getAccountStatus());

        MedicalOrgProfile profile = medicalOrgProfileRepository.findAll().stream()
                .filter(m -> m.getUser().getUserId().equals(user.getUserId()))
                .findFirst()
                .orElse(null);

        assertNotNull(profile);
        assertEquals(VerificationStatus.PENDING, profile.getVerificationStatus());
    }

    @Test
    void test11_LabRegistrationBecomesPending() {
        String email = "lab_" + UUID.randomUUID() + "@mediverse.com";
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(email);
        request.setPassword("SecretPass123!");
        request.setFullName("Apex Labs");
        request.setRole("LAB");
        request.setLabName("Apex Labs");
        request.setAccreditationNumber("ACC-LAB-500");

        RegisterResponseDto response = registrationService.registerUser(request);

        assertEquals("PENDING", response.getAccountStatus());

        User user = userRepository.findByEmail(email).orElseThrow();
        assertEquals(AccountStatus.PENDING, user.getAccountStatus());

        LaboratoryProfile profile = laboratoryProfileRepository.findAll().stream()
                .filter(l -> l.getUser().getUserId().equals(user.getUserId()))
                .findFirst()
                .orElse(null);

        assertNotNull(profile);
        assertEquals(VerificationStatus.PENDING, profile.getVerificationStatus());
    }

    @Test
    void test12_AdminRegistrationIsRejected() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("admin_attempt_" + UUID.randomUUID() + "@mediverse.com");
        request.setPassword("SecretPass123!");
        request.setFullName("Illegal Admin");
        request.setRole("ADMIN");

        assertThrows(IllegalArgumentException.class, () -> registrationService.registerUser(request));
    }

    @Test
    void test13_EmergencyRegistrationIsRejected() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("emergency_attempt_" + UUID.randomUUID() + "@mediverse.com");
        request.setPassword("SecretPass123!");
        request.setFullName("Illegal Emergency");
        request.setRole("EMERGENCY");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> registrationService.registerUser(request));
        assertEquals("EMERGENCY role registration is not publicly available.", ex.getMessage());
    }
}
