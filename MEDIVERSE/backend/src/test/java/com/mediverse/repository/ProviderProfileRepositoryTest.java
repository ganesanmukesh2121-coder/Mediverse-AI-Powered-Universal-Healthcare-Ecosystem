package com.mediverse.repository;

import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.LaboratoryProfile;
import com.mediverse.entity.MedicalOrgProfile;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class ProviderProfileRepositoryTest {

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private MedicalOrgProfileRepository medicalOrgProfileRepository;

    @Autowired
    private LaboratoryProfileRepository laboratoryProfileRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role doctorRole;
    private Role medicalRole;
    private Role labRole;

    @BeforeEach
    void setUp() {
        doctorRole = roleRepository.findByRoleName("DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("DOCTOR", "Doctor role")));
        medicalRole = roleRepository.findByRoleName("MEDICAL")
                .orElseGet(() -> roleRepository.save(new Role("MEDICAL", "Medical org role")));
        labRole = roleRepository.findByRoleName("LAB")
                .orElseGet(() -> roleRepository.save(new Role("LAB", "Laboratory role")));
    }

    private User createUser(String emailPrefix, Role role) {
        User user = new User();
        user.setEmail(emailPrefix + "_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash(passwordEncoder.encode("Password@123"));
        user.setFullName("Provider " + emailPrefix);
        user.setPhoneNumber("987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7));
        user.setRole(role);
        user.setAccountStatus(AccountStatus.PENDING);
        return userRepository.save(user);
    }

    @Test
    @DisplayName("Find pending doctors and lookup by user ID")
    void testDoctorProfileRepository_FindPendingAndFindByUserId() {
        User user = createUser("doc", doctorRole);

        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setLicenseNumber("DOC-LIC-" + UUID.randomUUID().toString().substring(0, 8));
        profile.setSpecialization("Cardiology");
        profile.setHospitalAffiliation("City Care Hospital");
        profile.setVerificationStatus(VerificationStatus.PENDING);
        DoctorProfile savedProfile = doctorProfileRepository.save(profile);

        // Find by user ID
        Optional<DoctorProfile> byUserId = doctorProfileRepository.findByUser_UserId(user.getUserId());
        assertTrue(byUserId.isPresent());
        assertEquals(savedProfile.getDoctorId(), byUserId.get().getDoctorId());
        assertEquals("Cardiology", byUserId.get().getSpecialization());

        // Find all pending
        List<DoctorProfile> pendingList = doctorProfileRepository.findAllByVerificationStatus(VerificationStatus.PENDING);
        assertFalse(pendingList.isEmpty());
        assertTrue(pendingList.stream().anyMatch(d -> d.getDoctorId().equals(savedProfile.getDoctorId())));

        // Find single by status
        Optional<DoctorProfile> singlePending = doctorProfileRepository.findByVerificationStatus(VerificationStatus.PENDING);
        assertTrue(singlePending.isPresent());
    }

    @Test
    @DisplayName("Find pending medical organizations and lookup by user ID")
    void testMedicalOrgProfileRepository_FindPendingAndFindByUserId() {
        User user = createUser("med", medicalRole);

        MedicalOrgProfile profile = new MedicalOrgProfile();
        profile.setUser(user);
        profile.setOrganizationName("Metro General Hospital");
        profile.setRegistrationNumber("REG-" + UUID.randomUUID().toString().substring(0, 8));
        profile.setFacilityType("HOSPITAL");
        profile.setVerificationStatus(VerificationStatus.PENDING);
        MedicalOrgProfile savedProfile = medicalOrgProfileRepository.save(profile);

        // Find by user ID
        Optional<MedicalOrgProfile> byUserId = medicalOrgProfileRepository.findByUser_UserId(user.getUserId());
        assertTrue(byUserId.isPresent());
        assertEquals(savedProfile.getMedicalOrgId(), byUserId.get().getMedicalOrgId());
        assertEquals("Metro General Hospital", byUserId.get().getOrganizationName());

        // Find all pending
        List<MedicalOrgProfile> pendingList = medicalOrgProfileRepository.findAllByVerificationStatus(VerificationStatus.PENDING);
        assertFalse(pendingList.isEmpty());
        assertTrue(pendingList.stream().anyMatch(m -> m.getMedicalOrgId().equals(savedProfile.getMedicalOrgId())));

        // Find single by status
        Optional<MedicalOrgProfile> singlePending = medicalOrgProfileRepository.findByVerificationStatus(VerificationStatus.PENDING);
        assertTrue(singlePending.isPresent());
    }

    @Test
    @DisplayName("Find pending laboratories and lookup by user ID")
    void testLaboratoryProfileRepository_FindPendingAndFindByUserId() {
        User user = createUser("lab", labRole);

        LaboratoryProfile profile = new LaboratoryProfile();
        profile.setUser(user);
        profile.setLabName("Apex Diagnostics Lab");
        profile.setAccreditationNumber("NABL-" + UUID.randomUUID().toString().substring(0, 8));
        profile.setVerificationStatus(VerificationStatus.PENDING);
        LaboratoryProfile savedProfile = laboratoryProfileRepository.save(profile);

        // Find by user ID
        Optional<LaboratoryProfile> byUserId = laboratoryProfileRepository.findByUser_UserId(user.getUserId());
        assertTrue(byUserId.isPresent());
        assertEquals(savedProfile.getLabId(), byUserId.get().getLabId());
        assertEquals("Apex Diagnostics Lab", byUserId.get().getLabName());

        // Find all pending
        List<LaboratoryProfile> pendingList = laboratoryProfileRepository.findAllByVerificationStatus(VerificationStatus.PENDING);
        assertFalse(pendingList.isEmpty());
        assertTrue(pendingList.stream().anyMatch(l -> l.getLabId().equals(savedProfile.getLabId())));

        // Find single by status
        Optional<LaboratoryProfile> singlePending = laboratoryProfileRepository.findByVerificationStatus(VerificationStatus.PENDING);
        assertTrue(singlePending.isPresent());
    }

    @Test
    @DisplayName("Return empty Optional when provider does not exist")
    void testProviderRepositories_ReturnEmptyWhenNotExists() {
        String nonExistentUserId = UUID.randomUUID().toString();

        assertTrue(doctorProfileRepository.findByUser_UserId(nonExistentUserId).isEmpty());
        assertTrue(medicalOrgProfileRepository.findByUser_UserId(nonExistentUserId).isEmpty());
        assertTrue(laboratoryProfileRepository.findByUser_UserId(nonExistentUserId).isEmpty());
    }

    @Test
    @DisplayName("AuditLogRepository saves and retrieves audit records")
    void testAuditLogRepository_SaveAndFind() {
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(UUID.randomUUID().toString());
        auditLog.setAction("PROVIDER_VERIFY_APPROVE");
        auditLog.setResourceAccessed("/api/v1/admin/providers/test/verify");
        auditLog.setIpAddress("127.0.0.1");
        auditLog.setUserAgent("JUnit 5 Test Runner");
        auditLog.setTimestamp(LocalDateTime.now());

        AuditLog saved = auditLogRepository.save(auditLog);
        assertNotNull(saved.getLogId());

        Optional<AuditLog> found = auditLogRepository.findById(saved.getLogId());
        assertTrue(found.isPresent());
        assertEquals("PROVIDER_VERIFY_APPROVE", found.get().getAction());
        assertEquals(saved.getActorUserId(), found.get().getActorUserId());
    }
}
