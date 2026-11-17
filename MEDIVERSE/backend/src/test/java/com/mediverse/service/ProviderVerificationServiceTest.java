package com.mediverse.service;

import com.mediverse.dto.PendingProviderResponseDto;
import com.mediverse.dto.ProviderVerificationRequestDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.LaboratoryProfile;
import com.mediverse.entity.MedicalOrgProfile;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.DoctorProfileRepository;
import com.mediverse.repository.LaboratoryProfileRepository;
import com.mediverse.repository.MedicalOrgProfileRepository;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class ProviderVerificationServiceTest {

    @Autowired
    private ProviderVerificationService providerVerificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private MedicalOrgProfileRepository medicalOrgProfileRepository;

    @Autowired
    private LaboratoryProfileRepository laboratoryProfileRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext context;

    private User adminUser;
    private Role doctorRole;
    private Role medicalRole;
    private Role labRole;
    private Role patientRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        doctorRole = roleRepository.findByRoleName("DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("DOCTOR", "Doctor role")));
        medicalRole = roleRepository.findByRoleName("MEDICAL")
                .orElseGet(() -> roleRepository.save(new Role("MEDICAL", "Medical org role")));
        labRole = roleRepository.findByRoleName("LAB")
                .orElseGet(() -> roleRepository.save(new Role("LAB", "Laboratory role")));
        patientRole = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));
        adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Admin role")));

        adminUser = new User();
        adminUser.setEmail("admin_" + UUID.randomUUID() + "@mediverse.com");
        adminUser.setPasswordHash(passwordEncoder.encode("AdminPass@123"));
        adminUser.setFullName("System Admin");
        adminUser.setRole(adminRole);
        adminUser.setAccountStatus(AccountStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);
    }

    private User createUser(String prefix, Role role, AccountStatus accountStatus) {
        User user = new User();
        user.setEmail(prefix + "_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash(passwordEncoder.encode("Password@123"));
        user.setFullName("Test " + prefix);
        user.setPhoneNumber("987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7));
        user.setRole(role);
        user.setAccountStatus(accountStatus);
        return userRepository.save(user);
    }

    private DoctorProfile createDoctor(User user, VerificationStatus status) {
        DoctorProfile doc = new DoctorProfile();
        doc.setUser(user);
        doc.setLicenseNumber("DOC-LIC-" + UUID.randomUUID().toString().substring(0, 8));
        doc.setSpecialization("Cardiology");
        doc.setHospitalAffiliation("City Hospital");
        doc.setVerificationStatus(status);
        return doctorProfileRepository.save(doc);
    }

    private MedicalOrgProfile createMedicalOrg(User user, VerificationStatus status) {
        MedicalOrgProfile med = new MedicalOrgProfile();
        med.setUser(user);
        med.setOrganizationName("General Medical Center");
        med.setRegistrationNumber("MED-REG-" + UUID.randomUUID().toString().substring(0, 8));
        med.setFacilityType("HOSPITAL");
        med.setVerificationStatus(status);
        return medicalOrgProfileRepository.save(med);
    }

    private LaboratoryProfile createLab(User user, VerificationStatus status) {
        LaboratoryProfile lab = new LaboratoryProfile();
        lab.setUser(user);
        lab.setLabName("Precision Pathology Lab");
        lab.setAccreditationNumber("LAB-ACC-" + UUID.randomUUID().toString().substring(0, 8));
        lab.setVerificationStatus(status);
        return laboratoryProfileRepository.save(lab);
    }

    @Test
    @DisplayName("1. Pending doctors are returned")
    void test1_PendingDoctorsAreReturned() {
        User docUser = createUser("doctor", doctorRole, AccountStatus.PENDING);
        DoctorProfile doc = createDoctor(docUser, VerificationStatus.PENDING);

        List<PendingProviderResponseDto> pending = providerVerificationService.getPendingProviders();
        assertFalse(pending.isEmpty());

        boolean found = pending.stream().anyMatch(dto ->
                docUser.getUserId().equals(dto.getUserId())
                        && "DOCTOR".equals(dto.getRole())
                        && doc.getLicenseNumber().equals(dto.getLicenseNumber()));
        assertTrue(found, "Pending doctor must be included in results");
    }

    @Test
    @DisplayName("2. Pending medical organizations are returned")
    void test2_PendingMedicalOrgsAreReturned() {
        User medUser = createUser("medical", medicalRole, AccountStatus.PENDING);
        MedicalOrgProfile med = createMedicalOrg(medUser, VerificationStatus.PENDING);

        List<PendingProviderResponseDto> pending = providerVerificationService.getPendingProviders();
        assertFalse(pending.isEmpty());

        boolean found = pending.stream().anyMatch(dto ->
                medUser.getUserId().equals(dto.getUserId())
                        && "MEDICAL".equals(dto.getRole())
                        && med.getOrganizationName().equals(dto.getOrganizationName()));
        assertTrue(found, "Pending medical organization must be included in results");
    }

    @Test
    @DisplayName("3. Pending laboratories are returned")
    void test3_PendingLaboratoriesAreReturned() {
        User labUser = createUser("lab", labRole, AccountStatus.PENDING);
        LaboratoryProfile lab = createLab(labUser, VerificationStatus.PENDING);

        List<PendingProviderResponseDto> pending = providerVerificationService.getPendingProviders();
        assertFalse(pending.isEmpty());

        boolean found = pending.stream().anyMatch(dto ->
                labUser.getUserId().equals(dto.getUserId())
                        && "LAB".equals(dto.getRole())
                        && lab.getAccreditationNumber().equals(dto.getAccreditationNumber()));
        assertTrue(found, "Pending laboratory must be included in results");
    }

    @Test
    @DisplayName("4. All provider types appear in one unified result")
    void test4_AllProviderTypesAppearInOneUnifiedResult() {
        User docUser = createUser("doc_u", doctorRole, AccountStatus.PENDING);
        createDoctor(docUser, VerificationStatus.PENDING);

        User medUser = createUser("med_u", medicalRole, AccountStatus.PENDING);
        createMedicalOrg(medUser, VerificationStatus.PENDING);

        User labUser = createUser("lab_u", labRole, AccountStatus.PENDING);
        createLab(labUser, VerificationStatus.PENDING);

        List<PendingProviderResponseDto> pending = providerVerificationService.getPendingProviders();

        assertTrue(pending.stream().anyMatch(p -> docUser.getUserId().equals(p.getUserId())));
        assertTrue(pending.stream().anyMatch(p -> medUser.getUserId().equals(p.getUserId())));
        assertTrue(pending.stream().anyMatch(p -> labUser.getUserId().equals(p.getUserId())));
    }

    @Test
    @DisplayName("5. Approved doctor account and profile become ACTIVE")
    void test5_ApprovedDoctorAccountAndProfileBecomeActive() {
        User docUser = createUser("doc_approve", doctorRole, AccountStatus.PENDING);
        DoctorProfile doc = createDoctor(docUser, VerificationStatus.PENDING);

        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("ACTIVE");
        PendingProviderResponseDto response = providerVerificationService.verifyProvider(
                docUser.getUserId(), request, adminUser.getUserId());

        assertNotNull(response);
        assertEquals("ACTIVE", response.getAccountStatus());
        assertEquals("ACTIVE", response.getVerificationStatus());

        User reloadedUser = userRepository.findById(docUser.getUserId()).orElseThrow();
        assertEquals(AccountStatus.ACTIVE, reloadedUser.getAccountStatus());

        DoctorProfile reloadedProfile = doctorProfileRepository.findByUser_UserId(docUser.getUserId()).orElseThrow();
        assertEquals(VerificationStatus.ACTIVE, reloadedProfile.getVerificationStatus());
    }

    @Test
    @DisplayName("6. Approved medical organization becomes ACTIVE")
    void test6_ApprovedMedicalOrgBecomesActive() {
        User medUser = createUser("med_approve", medicalRole, AccountStatus.PENDING);
        MedicalOrgProfile med = createMedicalOrg(medUser, VerificationStatus.PENDING);

        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("ACTIVE");
        PendingProviderResponseDto response = providerVerificationService.verifyProvider(
                medUser.getUserId(), request, adminUser.getUserId());

        assertNotNull(response);
        assertEquals("ACTIVE", response.getAccountStatus());
        assertEquals("ACTIVE", response.getVerificationStatus());

        User reloadedUser = userRepository.findById(medUser.getUserId()).orElseThrow();
        assertEquals(AccountStatus.ACTIVE, reloadedUser.getAccountStatus());

        MedicalOrgProfile reloadedProfile = medicalOrgProfileRepository.findByUser_UserId(medUser.getUserId()).orElseThrow();
        assertEquals(VerificationStatus.ACTIVE, reloadedProfile.getVerificationStatus());
    }

    @Test
    @DisplayName("7. Approved laboratory becomes ACTIVE")
    void test7_ApprovedLaboratoryBecomesActive() {
        User labUser = createUser("lab_approve", labRole, AccountStatus.PENDING);
        LaboratoryProfile lab = createLab(labUser, VerificationStatus.PENDING);

        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("ACTIVE");
        PendingProviderResponseDto response = providerVerificationService.verifyProvider(
                labUser.getUserId(), request, adminUser.getUserId());

        assertNotNull(response);
        assertEquals("ACTIVE", response.getAccountStatus());
        assertEquals("ACTIVE", response.getVerificationStatus());

        User reloadedUser = userRepository.findById(labUser.getUserId()).orElseThrow();
        assertEquals(AccountStatus.ACTIVE, reloadedUser.getAccountStatus());

        LaboratoryProfile reloadedProfile = laboratoryProfileRepository.findByUser_UserId(labUser.getUserId()).orElseThrow();
        assertEquals(VerificationStatus.ACTIVE, reloadedProfile.getVerificationStatus());
    }

    @Test
    @DisplayName("8. Rejected provider account and profile become REJECTED")
    void test8_RejectedProviderAccountAndProfileBecomeRejected() {
        User docUser = createUser("doc_reject", doctorRole, AccountStatus.PENDING);
        DoctorProfile doc = createDoctor(docUser, VerificationStatus.PENDING);

        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto(
                "REJECTED", "License credentials could not be verified with state registry.");
        PendingProviderResponseDto response = providerVerificationService.verifyProvider(
                docUser.getUserId(), request, adminUser.getUserId());

        assertNotNull(response);
        assertEquals("REJECTED", response.getAccountStatus());
        assertEquals("REJECTED", response.getVerificationStatus());

        User reloadedUser = userRepository.findById(docUser.getUserId()).orElseThrow();
        assertEquals(AccountStatus.REJECTED, reloadedUser.getAccountStatus());

        DoctorProfile reloadedProfile = doctorProfileRepository.findByUser_UserId(docUser.getUserId()).orElseThrow();
        assertEquals(VerificationStatus.REJECTED, reloadedProfile.getVerificationStatus());
    }

    @Test
    @DisplayName("9. Rejection reason is recorded in the audit log")
    void test9_RejectionReasonIsRecordedInAuditLog() {
        User docUser = createUser("doc_reason", doctorRole, AccountStatus.PENDING);
        createDoctor(docUser, VerificationStatus.PENDING);

        String rejectionReason = "Forged medical diploma detected";
        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("REJECTED", rejectionReason);

        providerVerificationService.verifyProvider(docUser.getUserId(), request, adminUser.getUserId());

        List<AuditLog> logs = auditLogRepository.findAll();
        boolean foundLogWithReason = logs.stream().anyMatch(log ->
                "PROVIDER_VERIFY_REJECT".equals(log.getAction())
                        && log.getResourceAccessed().contains(rejectionReason));

        assertTrue(foundLogWithReason, "AuditLog must contain the rejection reason in resourceAccessed");
    }

    @Test
    @DisplayName("10. Audit entry contains the correct admin actor and provider target")
    void test10_AuditEntryContainsCorrectAdminActorAndProviderTarget() {
        User labUser = createUser("lab_audit", labRole, AccountStatus.PENDING);
        createLab(labUser, VerificationStatus.PENDING);

        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("ACTIVE");
        providerVerificationService.verifyProvider(labUser.getUserId(), request, adminUser.getUserId());

        List<AuditLog> logs = auditLogRepository.findAll();
        boolean foundMatchingAudit = logs.stream().anyMatch(log ->
                adminUser.getUserId().equals(log.getActorUserId())
                        && labUser.getUserId().equals(log.getTargetPatientId())
                        && "PROVIDER_VERIFY_APPROVE".equals(log.getAction()));

        assertTrue(foundMatchingAudit, "AuditLog must record admin as actor and provider user ID as target");
    }

    @Test
    @DisplayName("11. Non-provider roles are rejected")
    void test11_NonProviderRolesAreRejected() {
        User patientUser = createUser("patient", patientRole, AccountStatus.PENDING);
        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("ACTIVE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                providerVerificationService.verifyProvider(patientUser.getUserId(), request, adminUser.getUserId()));

        assertTrue(ex.getMessage().contains("not a healthcare provider"));
    }

    @Test
    @DisplayName("12. Missing provider account is handled correctly")
    void test12_MissingProviderAccountIsHandledCorrectly() {
        String nonExistentUserId = UUID.randomUUID().toString();
        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("ACTIVE");

        assertThrows(ResourceNotFoundException.class, () ->
                providerVerificationService.verifyProvider(nonExistentUserId, request, adminUser.getUserId()));
    }

    @Test
    @DisplayName("13. Missing provider profile is handled correctly")
    void test13_MissingProviderProfileIsHandledCorrectly() {
        // User has DOCTOR role but no DoctorProfile record exists
        User orphanedDoctor = createUser("orphan_doc", doctorRole, AccountStatus.PENDING);
        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("ACTIVE");

        assertThrows(ResourceNotFoundException.class, () ->
                providerVerificationService.verifyProvider(orphanedDoctor.getUserId(), request, adminUser.getUserId()));
    }

    @Test
    @DisplayName("14. Already-approved or rejected accounts cannot be processed again")
    void test14_AlreadyApprovedOrRejectedAccountsCannotBeProcessedAgain() {
        User activeDocUser = createUser("active_doc", doctorRole, AccountStatus.ACTIVE);
        createDoctor(activeDocUser, VerificationStatus.ACTIVE);

        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("ACTIVE");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                providerVerificationService.verifyProvider(activeDocUser.getUserId(), request, adminUser.getUserId()));

        assertTrue(ex.getMessage().contains("Cannot verify account that is not pending"));
    }

    @Test
    @DisplayName("15. Invalid decisions are rejected")
    void test15_InvalidDecisionsAreRejected() {
        User docUser = createUser("doc_invalid", doctorRole, AccountStatus.PENDING);
        createDoctor(docUser, VerificationStatus.PENDING);

        // Blank decision
        assertThrows(IllegalArgumentException.class, () ->
                providerVerificationService.verifyProvider(
                        docUser.getUserId(), new ProviderVerificationRequestDto("   "), adminUser.getUserId()));

        // Unsupported decision
        assertThrows(IllegalArgumentException.class, () ->
                providerVerificationService.verifyProvider(
                        docUser.getUserId(), new ProviderVerificationRequestDto("SUSPENDED"), adminUser.getUserId()));

        // REJECTED without reason
        assertThrows(IllegalArgumentException.class, () ->
                providerVerificationService.verifyProvider(
                        docUser.getUserId(), new ProviderVerificationRequestDto("REJECTED", null), adminUser.getUserId()));
    }

    @Test
    @DisplayName("16. Audit failure rolls back provider status changes")
    void test16_AuditFailureRollsBackProviderStatusChanges() {
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        DoctorProfileRepository mockDocRepo = Mockito.mock(DoctorProfileRepository.class);
        MedicalOrgProfileRepository mockMedRepo = Mockito.mock(MedicalOrgProfileRepository.class);
        LaboratoryProfileRepository mockLabRepo = Mockito.mock(LaboratoryProfileRepository.class);
        AuditLogRepository mockAuditRepo = Mockito.mock(AuditLogRepository.class);

        ProviderVerificationService service = new ProviderVerificationService(
                mockUserRepo, mockDocRepo, mockMedRepo, mockLabRepo, mockAuditRepo
        );

        User docUser = new User();
        docUser.setUserId("doc-123");
        docUser.setRole(doctorRole);
        docUser.setAccountStatus(AccountStatus.PENDING);

        DoctorProfile docProfile = new DoctorProfile();
        docProfile.setUser(docUser);
        docProfile.setVerificationStatus(VerificationStatus.PENDING);

        Mockito.when(mockUserRepo.findById("doc-123")).thenReturn(java.util.Optional.of(docUser));
        Mockito.when(mockDocRepo.findByUser_UserId("doc-123")).thenReturn(java.util.Optional.of(docProfile));
        Mockito.when(mockDocRepo.save(any(DoctorProfile.class))).thenReturn(docProfile);
        Mockito.doThrow(new RuntimeException("Simulated audit disk failure"))
                .when(mockAuditRepo).save(any(AuditLog.class));

        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("ACTIVE");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.verifyProvider("doc-123", request, adminUser.getUserId())
        );

        assertEquals("Simulated audit disk failure", ex.getMessage());

        // Verify @Transactional annotation is present on the method to ensure Spring transactional rollback
        try {
            var method = ProviderVerificationService.class.getMethod(
                    "verifyProvider", String.class, ProviderVerificationRequestDto.class, String.class);
            assertTrue(method.isAnnotationPresent(Transactional.class),
                    "verifyProvider must be annotated with @Transactional to guarantee rollback on audit failure");
        } catch (NoSuchMethodException e) {
            fail("verifyProvider method not found");
        }
    }

    @Test
    @DisplayName("17. Provider Health ID is not generated")
    void test17_ProviderHealthIdIsNotGenerated() {
        User docUser = createUser("doc_health_id", doctorRole, AccountStatus.PENDING);
        createDoctor(docUser, VerificationStatus.PENDING);

        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("ACTIVE");
        PendingProviderResponseDto response = providerVerificationService.verifyProvider(
                docUser.getUserId(), request, adminUser.getUserId());

        // Providers must never have a Health ID
        User reloadedUser = userRepository.findById(docUser.getUserId()).orElseThrow();
        assertNull(reloadedUser.getHealthIdRegistry(), "Providers must not have HealthIdRegistry attached");
    }

    @Test
    @DisplayName("18. User identity and registration details remain unchanged")
    void test18_UserIdentityAndRegistrationDetailsRemainUnchanged() {
        User docUser = createUser("doc_identity", doctorRole, AccountStatus.PENDING);
        DoctorProfile doc = createDoctor(docUser, VerificationStatus.PENDING);

        String originalEmail = docUser.getEmail();
        String originalFullName = docUser.getFullName();
        String originalPhone = docUser.getPhoneNumber();
        String originalPasswordHash = docUser.getPasswordHash();
        String originalLicense = doc.getLicenseNumber();
        String originalSpecialization = doc.getSpecialization();
        String originalHospital = doc.getHospitalAffiliation();

        ProviderVerificationRequestDto request = new ProviderVerificationRequestDto("ACTIVE");
        providerVerificationService.verifyProvider(docUser.getUserId(), request, adminUser.getUserId());

        User reloadedUser = userRepository.findById(docUser.getUserId()).orElseThrow();
        assertEquals(originalEmail, reloadedUser.getEmail());
        assertEquals(originalFullName, reloadedUser.getFullName());
        assertEquals(originalPhone, reloadedUser.getPhoneNumber());
        assertEquals(originalPasswordHash, reloadedUser.getPasswordHash());
        assertEquals("DOCTOR", reloadedUser.getRole().getRoleName());

        DoctorProfile reloadedProfile = doctorProfileRepository.findByUser_UserId(docUser.getUserId()).orElseThrow();
        assertEquals(originalLicense, reloadedProfile.getLicenseNumber());
        assertEquals(originalSpecialization, reloadedProfile.getSpecialization());
        assertEquals(originalHospital, reloadedProfile.getHospitalAffiliation());
    }
}
