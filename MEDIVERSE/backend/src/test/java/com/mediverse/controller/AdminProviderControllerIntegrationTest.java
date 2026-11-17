package com.mediverse.controller;

import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.LaboratoryProfile;
import com.mediverse.entity.MedicalOrgProfile;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.DoctorProfileRepository;
import com.mediverse.repository.LaboratoryProfileRepository;
import com.mediverse.repository.MedicalOrgProfileRepository;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;
import com.mediverse.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class AdminProviderControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

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

    private MockMvc mockMvc;

    private User adminUser;
    private String adminToken;

    private Role adminRole;
    private Role doctorRole;
    private Role medicalRole;
    private Role labRole;
    private Role patientRole;
    private Role emergencyRole;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Admin role")));
        doctorRole = roleRepository.findByRoleName("DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("DOCTOR", "Doctor role")));
        medicalRole = roleRepository.findByRoleName("MEDICAL")
                .orElseGet(() -> roleRepository.save(new Role("MEDICAL", "Medical org role")));
        labRole = roleRepository.findByRoleName("LAB")
                .orElseGet(() -> roleRepository.save(new Role("LAB", "Lab role")));
        patientRole = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));
        emergencyRole = roleRepository.findByRoleName("EMERGENCY")
                .orElseGet(() -> roleRepository.save(new Role("EMERGENCY", "Emergency role")));

        adminUser = new User();
        adminUser.setEmail("admin_ctrl_" + UUID.randomUUID() + "@mediverse.com");
        adminUser.setPasswordHash(passwordEncoder.encode("AdminPass@123"));
        adminUser.setFullName("Super Admin");
        adminUser.setRole(adminRole);
        adminUser.setAccountStatus(AccountStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);

        adminToken = jwtService.generateAccessToken(
                adminUser.getUserId(),
                adminUser.getEmail(),
                "ADMIN",
                "ACTIVE",
                null,
                List.of("ADMIN_VERIFY_PROVIDER", "ADMIN_MANAGE_ACCOUNT", "ADMIN_VIEW_AUDIT")
        );
    }

    private String createToken(String role, List<String> permissions) {
        String userId = UUID.randomUUID().toString();
        return jwtService.generateAccessToken(
                userId,
                role.toLowerCase() + "_" + UUID.randomUUID() + "@mediverse.com",
                role,
                "ACTIVE",
                null,
                permissions
        );
    }

    private User createProviderUser(String prefix, Role role, AccountStatus accountStatus) {
        User user = new User();
        user.setEmail(prefix + "_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash(passwordEncoder.encode("Password@123"));
        user.setFullName("Provider " + prefix);
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
        doc.setHospitalAffiliation("City Care Hospital");
        doc.setVerificationStatus(status);
        return doctorProfileRepository.save(doc);
    }

    private MedicalOrgProfile createMedicalOrg(User user, VerificationStatus status) {
        MedicalOrgProfile med = new MedicalOrgProfile();
        med.setUser(user);
        med.setOrganizationName("Central City Hospital");
        med.setRegistrationNumber("MED-REG-" + UUID.randomUUID().toString().substring(0, 8));
        med.setFacilityType("HOSPITAL");
        med.setVerificationStatus(status);
        return medicalOrgProfileRepository.save(med);
    }

    @Test
    @DisplayName("1. ADMIN with permission can retrieve pending providers — 200")
    void test1_AdminCanRetrievePendingProviders_200() throws Exception {
        User docUser = createProviderUser("doc_p1", doctorRole, AccountStatus.PENDING);
        createDoctor(docUser, VerificationStatus.PENDING);

        mockMvc.perform(get("/api/v1/admin/providers/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.userId == '" + docUser.getUserId() + "')].role").value("DOCTOR"))
                .andExpect(jsonPath("$[?(@.userId == '" + docUser.getUserId() + "')].accountStatus").value("PENDING"));
    }

    @Test
    @DisplayName("2. ADMIN with permission can approve a pending provider — success")
    void test2_AdminCanApprovePendingProvider_Success() throws Exception {
        User docUser = createProviderUser("doc_app", doctorRole, AccountStatus.PENDING);
        createDoctor(docUser, VerificationStatus.PENDING);

        String payload = "{\"decision\": \"ACTIVE\"}";

        mockMvc.perform(put("/api/v1/admin/providers/" + docUser.getUserId() + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(docUser.getUserId()))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.verificationStatus").value("ACTIVE"));

        User reloaded = userRepository.findById(docUser.getUserId()).orElseThrow();
        assertEquals(AccountStatus.ACTIVE, reloaded.getAccountStatus());
    }

    @Test
    @DisplayName("3. ADMIN with permission can reject a pending provider — success")
    void test3_AdminCanRejectPendingProvider_Success() throws Exception {
        User medUser = createProviderUser("med_rej", medicalRole, AccountStatus.PENDING);
        createMedicalOrg(medUser, VerificationStatus.PENDING);

        String payload = """
                {
                    "decision": "REJECTED",
                    "rejectionReason": "Medical license registration number is invalid"
                }
                """;

        mockMvc.perform(put("/api/v1/admin/providers/" + medUser.getUserId() + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(medUser.getUserId()))
                .andExpect(jsonPath("$.accountStatus").value("REJECTED"))
                .andExpect(jsonPath("$.verificationStatus").value("REJECTED"));

        User reloaded = userRepository.findById(medUser.getUserId()).orElseThrow();
        assertEquals(AccountStatus.REJECTED, reloaded.getAccountStatus());
    }

    @Test
    @DisplayName("4. PATIENT receives 403")
    void test4_PatientReceives403() throws Exception {
        String patientToken = createToken("PATIENT", List.of("PATIENT_VIEW_OWN_PROFILE", "PATIENT_UPDATE_OWN_PROFILE"));

        mockMvc.perform(get("/api/v1/admin/providers/pending")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/admin/providers/" + UUID.randomUUID() + "/verify")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\": \"ACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. DOCTOR receives 403")
    void test5_DoctorReceives403() throws Exception {
        String doctorToken = createToken("DOCTOR", List.of("DOCTOR_VIEW_AUTHORIZED_RECORD"));

        mockMvc.perform(get("/api/v1/admin/providers/pending")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/admin/providers/" + UUID.randomUUID() + "/verify")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\": \"ACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. MEDICAL receives 403")
    void test6_MedicalReceives403() throws Exception {
        String medicalToken = createToken("MEDICAL", List.of("MEDICAL_CREATE_ADMISSION"));

        mockMvc.perform(get("/api/v1/admin/providers/pending")
                        .header("Authorization", "Bearer " + medicalToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/admin/providers/" + UUID.randomUUID() + "/verify")
                        .header("Authorization", "Bearer " + medicalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\": \"ACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7. LAB receives 403")
    void test7_LabReceives403() throws Exception {
        String labToken = createToken("LAB", List.of("LAB_CREATE_TEST_ORDER"));

        mockMvc.perform(get("/api/v1/admin/providers/pending")
                        .header("Authorization", "Bearer " + labToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/admin/providers/" + UUID.randomUUID() + "/verify")
                        .header("Authorization", "Bearer " + labToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\": \"ACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("8. EMERGENCY receives 403")
    void test8_EmergencyReceives403() throws Exception {
        String emergencyToken = createToken("EMERGENCY", List.of("EMERGENCY_VIEW_EMERGENCY_INFO"));

        mockMvc.perform(get("/api/v1/admin/providers/pending")
                        .header("Authorization", "Bearer " + emergencyToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/admin/providers/" + UUID.randomUUID() + "/verify")
                        .header("Authorization", "Bearer " + emergencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\": \"ACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("9. Unauthenticated pending-list request receives 401")
    void test9_UnauthenticatedPendingListRequest_401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/providers/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("10. Unauthenticated verification request receives 401")
    void test10_UnauthenticatedVerificationRequest_401() throws Exception {
        mockMvc.perform(put("/api/v1/admin/providers/" + UUID.randomUUID() + "/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\": \"ACTIVE\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("11. ADMIN without ADMIN_VERIFY_PROVIDER permission receives 403")
    void test11_AdminWithoutPermission_403() throws Exception {
        String adminWithoutPermToken = jwtService.generateAccessToken(
                adminUser.getUserId(),
                adminUser.getEmail(),
                "ADMIN",
                "ACTIVE",
                null,
                List.of("ADMIN_VIEW_AUDIT") // missing ADMIN_VERIFY_PROVIDER
        );

        mockMvc.perform(get("/api/v1/admin/providers/pending")
                        .header("Authorization", "Bearer " + adminWithoutPermToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/admin/providers/" + UUID.randomUUID() + "/verify")
                        .header("Authorization", "Bearer " + adminWithoutPermToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\": \"ACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("12. Invalid decision receives 400")
    void test12_InvalidDecision_400() throws Exception {
        User docUser = createProviderUser("doc_inv", doctorRole, AccountStatus.PENDING);
        createDoctor(docUser, VerificationStatus.PENDING);

        String invalidDecisionPayload = "{\"decision\": \"SUSPENDED\"}";

        mockMvc.perform(put("/api/v1/admin/providers/" + docUser.getUserId() + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDecisionPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("13. Rejection without reason receives 400")
    void test13_RejectionWithoutReason_400() throws Exception {
        User docUser = createProviderUser("doc_noreason", doctorRole, AccountStatus.PENDING);
        createDoctor(docUser, VerificationStatus.PENDING);

        String noReasonPayload = "{\"decision\": \"REJECTED\"}";

        mockMvc.perform(put("/api/v1/admin/providers/" + docUser.getUserId() + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noReasonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("14. Unknown provider user ID returns 404")
    void test14_UnknownProviderUserId_404() throws Exception {
        String nonExistentUserId = UUID.randomUUID().toString();
        String payload = "{\"decision\": \"ACTIVE\"}";

        mockMvc.perform(put("/api/v1/admin/providers/" + nonExistentUserId + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("15. Already-processed provider returns appropriate error response (400)")
    void test15_AlreadyProcessedProvider_400() throws Exception {
        User activeDocUser = createProviderUser("doc_active", doctorRole, AccountStatus.ACTIVE);
        createDoctor(activeDocUser, VerificationStatus.ACTIVE);

        String payload = "{\"decision\": \"ACTIVE\"}";

        mockMvc.perform(put("/api/v1/admin/providers/" + activeDocUser.getUserId() + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cannot verify account that is not pending")));
    }

    @Test
    @DisplayName("16. Admin actor ID is taken from SecurityContext, not client input")
    void test16_AdminActorIdTakenFromSecurityContext() throws Exception {
        User docUser = createProviderUser("doc_actor", doctorRole, AccountStatus.PENDING);
        createDoctor(docUser, VerificationStatus.PENDING);

        // Try passing a spoofed admin ID in payload
        String spoofPayload = """
                {
                    "decision": "ACTIVE",
                    "adminUserId": "spoofed-admin-id-12345"
                }
                """;

        mockMvc.perform(put("/api/v1/admin/providers/" + docUser.getUserId() + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(spoofPayload))
                .andExpect(status().isOk());

        // Verify audit log recorded actual authenticated admin's ID, not the spoofed string
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        boolean foundRealAdminAudit = auditLogs.stream().anyMatch(log ->
                adminUser.getUserId().equals(log.getActorUserId())
                        && docUser.getUserId().equals(log.getTargetPatientId())
                        && "PROVIDER_VERIFY_APPROVE".equals(log.getAction()));

        assertTrue(foundRealAdminAudit, "Audit log must contain real admin ID from SecurityContext");

        boolean spoofedAuditExists = auditLogs.stream().anyMatch(log ->
                "spoofed-admin-id-12345".equals(log.getActorUserId()));
        assertFalse(spoofedAuditExists, "Spoofed admin ID must NEVER be recorded in audit log");
    }

    @Test
    @DisplayName("17. Response does not expose sensitive security fields")
    void test17_ResponseDoesNotExposeSensitiveSecurityFields() throws Exception {
        User docUser = createProviderUser("doc_sec", doctorRole, AccountStatus.PENDING);
        createDoctor(docUser, VerificationStatus.PENDING);

        String payload = "{\"decision\": \"ACTIVE\"}";

        mockMvc.perform(put("/api/v1/admin/providers/" + docUser.getUserId() + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.permissions").doesNotExist());
    }

    @Test
    @DisplayName("18. Existing ADMIN clinical-data restrictions remain intact")
    void test18_AdminClinicalDataRestrictionsRemainIntact() throws Exception {
        // Admin attempting to access patient self-service clinical profile
        mockMvc.perform(get("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }
}
