package com.mediverse.security;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import com.mediverse.dto.LoginRequestDto;
import com.mediverse.dto.LoginResponseDto;
import com.mediverse.dto.RefreshTokenRequestDto;
import com.mediverse.dto.RefreshTokenResponseDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.HealthIdRegistry;
import com.mediverse.entity.HealthIdStatus;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.repository.HealthIdRegistryRepository;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;
import com.mediverse.service.AuthenticationService;
import com.mediverse.service.RefreshTokenService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration test verifying the complete authorization pipeline:
 * Database Role-Permission Mappings
 * -> AuthenticationService Login
 * -> JWT Permission Claims Generation
 * -> JwtAuthenticationFilter Authority Mapping
 * -> SecurityContextHolder
 * -> @PreAuthorize Evaluation
 * -> HTTP 200 / 401 / 403 Decision.
 *
 * NOTE: The endpoints defined below are strictly test-only endpoints isolated in src/test/java
 * to verify @PreAuthorize execution prior to domain controller development in subsequent steps.
 */
@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
@Import(RbacEndToEndIntegrationTest.TestRbacEndpointsConfiguration.class)
class RbacEndToEndIntegrationTest {

    @TestConfiguration
    static class TestRbacEndpointsConfiguration {

        @RestController
        @RequestMapping("/api/v1/test-rbac-e2e")
        static class TestRbacEndpointsController {

            @GetMapping("/patient-profile")
            @PreAuthorize("hasAuthority('PATIENT_VIEW_OWN_PROFILE')")
            public ResponseEntity<String> getPatientProfile() {
                return ResponseEntity.ok("e2e-patient-profile-data");
            }

            @GetMapping("/clinical-encounter")
            @PreAuthorize("hasAuthority('DOCTOR_VIEW_AUTHORIZED_RECORD')")
            public ResponseEntity<String> getClinicalEncounter() {
                return ResponseEntity.ok("e2e-clinical-encounter-data");
            }

            @PostMapping("/clinical-consultation")
            @PreAuthorize("hasAuthority('DOCTOR_CREATE_CONSULTATION')")
            public ResponseEntity<String> createConsultation() {
                return ResponseEntity.ok("e2e-consultation-created");
            }

            @GetMapping("/emergency-triage")
            @PreAuthorize("hasAuthority('EMERGENCY_VIEW_EMERGENCY_INFO')")
            public ResponseEntity<String> getEmergencyTriage() {
                return ResponseEntity.ok("e2e-emergency-triage-data");
            }

            @PostMapping("/admin-manage-provider")
            @PreAuthorize("hasAuthority('ADMIN_VERIFY_PROVIDER')")
            public ResponseEntity<String> verifyProvider() {
                return ResponseEntity.ok("e2e-provider-verified");
            }

            @PostMapping("/medical-admission")
            @PreAuthorize("hasAuthority('MEDICAL_CREATE_ADMISSION')")
            public ResponseEntity<String> createAdmission() {
                return ResponseEntity.ok("e2e-admission-created");
            }

            @PostMapping("/lab-diagnostic-order")
            @PreAuthorize("hasAuthority('LAB_CREATE_TEST_ORDER')")
            public ResponseEntity<String> createLabOrder() {
                return ResponseEntity.ok("e2e-lab-order-created");
            }
        }
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private HealthIdRegistryRepository healthIdRegistryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private User createActiveUser(String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));

        User user = new User();
        user.setEmail(roleName.toLowerCase() + "_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash(passwordEncoder.encode("SecurePass123!"));
        user.setFullName("E2E Test " + roleName);
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user = userRepository.save(user);

        if ("PATIENT".equals(roleName)) {
            HealthIdRegistry hir = new HealthIdRegistry();
            hir.setHealthId("MV-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            hir.setPatientUser(user);
            hir.setStatus(HealthIdStatus.ACTIVE);
            healthIdRegistryRepository.save(hir);
            user.setHealthIdRegistry(hir);
        }
        return user;
    }

    private LoginResponseDto login(User user) {
        return authenticationService.login(new LoginRequestDto(user.getEmail(), "SecurePass123!"));
    }

    @Test
    @DisplayName("Role 1: PATIENT end-to-end pipeline verification")
    void testRole1_Patient_FullPipeline_Authorization() throws Exception {
        User patientUser = createActiveUser("PATIENT");
        LoginResponseDto loginResponse = login(patientUser);

        assertNotNull(loginResponse.getAccessToken());
        List<String> permissions = jwtService.extractPermissions(loginResponse.getAccessToken());
        assertEquals(6, permissions.size(), "PATIENT must have exactly 6 permissions");
        assertTrue(permissions.contains("PATIENT_VIEW_OWN_PROFILE"));
        assertTrue(permissions.contains("PATIENT_UPDATE_OWN_PROFILE"));
        assertTrue(permissions.contains("PATIENT_UPLOAD_DOCUMENT"));
        assertTrue(permissions.contains("PATIENT_MANAGE_CONSENT"));
        assertTrue(permissions.contains("PATIENT_VIEW_OWN_AUDIT"));
        assertTrue(permissions.contains("PATIENT_USE_AI_ASSISTANT"));

        // Authorized endpoint -> 200 OK
        mockMvc.perform(get("/api/v1/test-rbac-e2e/patient-profile")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().string("e2e-patient-profile-data"));

        // Security boundary: Patient cannot access doctor clinical endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/v1/test-rbac-e2e/clinical-encounter")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Role 2: DOCTOR end-to-end pipeline verification")
    void testRole2_Doctor_FullPipeline_Authorization() throws Exception {
        User doctorUser = createActiveUser("DOCTOR");
        LoginResponseDto loginResponse = login(doctorUser);

        assertNotNull(loginResponse.getAccessToken());
        List<String> permissions = jwtService.extractPermissions(loginResponse.getAccessToken());
        assertEquals(5, permissions.size(), "DOCTOR must have exactly 5 permissions");
        assertTrue(permissions.contains("DOCTOR_VIEW_AUTHORIZED_RECORD"));
        assertTrue(permissions.contains("DOCTOR_CREATE_CONSULTATION"));
        assertTrue(permissions.contains("DOCTOR_CREATE_PRESCRIPTION"));
        assertTrue(permissions.contains("DOCTOR_ATTACH_DOCUMENT"));
        assertTrue(permissions.contains("DOCTOR_USE_RAG_AGENT"));

        // Authorized endpoint -> 200 OK
        mockMvc.perform(get("/api/v1/test-rbac-e2e/clinical-encounter")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().string("e2e-clinical-encounter-data"));

        // Security boundary: Doctor cannot access admin operations -> 403 Forbidden
        mockMvc.perform(post("/api/v1/test-rbac-e2e/admin-manage-provider")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Role 3: EMERGENCY end-to-end pipeline verification")
    void testRole3_Emergency_FullPipeline_Authorization() throws Exception {
        User emergencyUser = createActiveUser("EMERGENCY");
        LoginResponseDto loginResponse = login(emergencyUser);

        assertNotNull(loginResponse.getAccessToken());
        List<String> permissions = jwtService.extractPermissions(loginResponse.getAccessToken());
        assertEquals(1, permissions.size(), "EMERGENCY must have exactly 1 permission");
        assertTrue(permissions.contains("EMERGENCY_VIEW_EMERGENCY_INFO"));

        // Authorized emergency triage -> 200 OK
        mockMvc.perform(get("/api/v1/test-rbac-e2e/emergency-triage")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().string("e2e-emergency-triage-data"));

        // Security boundary: Emergency cannot access historical clinical encounters -> 403 Forbidden
        mockMvc.perform(get("/api/v1/test-rbac-e2e/clinical-encounter")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Role 4: ADMIN end-to-end pipeline verification (Zero Clinical Access Boundary)")
    void testRole4_Admin_FullPipeline_Authorization() throws Exception {
        User adminUser = createActiveUser("ADMIN");
        LoginResponseDto loginResponse = login(adminUser);

        assertNotNull(loginResponse.getAccessToken());
        List<String> permissions = jwtService.extractPermissions(loginResponse.getAccessToken());
        assertEquals(3, permissions.size(), "ADMIN must have exactly 3 permissions");
        assertTrue(permissions.contains("ADMIN_VERIFY_PROVIDER"));
        assertTrue(permissions.contains("ADMIN_MANAGE_ACCOUNT"));
        assertTrue(permissions.contains("ADMIN_VIEW_AUDIT"));

        // Authorized admin management -> 200 OK
        mockMvc.perform(post("/api/v1/test-rbac-e2e/admin-manage-provider")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().string("e2e-provider-verified"));

        // Security boundary: Admin has ZERO clinical access -> 403 Forbidden
        mockMvc.perform(get("/api/v1/test-rbac-e2e/clinical-encounter")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(post("/api/v1/test-rbac-e2e/clinical-consultation")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Role 5: MEDICAL end-to-end pipeline verification")
    void testRole5_Medical_FullPipeline_Authorization() throws Exception {
        User medicalUser = createActiveUser("MEDICAL");
        LoginResponseDto loginResponse = login(medicalUser);

        assertNotNull(loginResponse.getAccessToken());
        List<String> permissions = jwtService.extractPermissions(loginResponse.getAccessToken());
        assertEquals(4, permissions.size(), "MEDICAL must have exactly 4 permissions");
        assertTrue(permissions.contains("MEDICAL_CREATE_ADMISSION"));
        assertTrue(permissions.contains("MEDICAL_CREATE_TREATMENT"));
        assertTrue(permissions.contains("MEDICAL_CREATE_DISCHARGE"));
        assertTrue(permissions.contains("MEDICAL_UPLOAD_DOCUMENT"));

        // Authorized medical admission -> 200 OK
        mockMvc.perform(post("/api/v1/test-rbac-e2e/medical-admission")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().string("e2e-admission-created"));

        // Security boundary: Medical organization cannot verify providers -> 403 Forbidden
        mockMvc.perform(post("/api/v1/test-rbac-e2e/admin-manage-provider")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Role 6: LAB end-to-end pipeline verification")
    void testRole6_Lab_FullPipeline_Authorization() throws Exception {
        User labUser = createActiveUser("LAB");
        LoginResponseDto loginResponse = login(labUser);

        assertNotNull(loginResponse.getAccessToken());
        List<String> permissions = jwtService.extractPermissions(loginResponse.getAccessToken());
        assertEquals(3, permissions.size(), "LAB must have exactly 3 permissions");
        assertTrue(permissions.contains("LAB_CREATE_TEST_ORDER"));
        assertTrue(permissions.contains("LAB_ENTER_RESULTS"));
        assertTrue(permissions.contains("LAB_UPLOAD_REPORT"));

        // Authorized lab diagnostic order -> 200 OK
        mockMvc.perform(post("/api/v1/test-rbac-e2e/lab-diagnostic-order")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().string("e2e-lab-order-created"));

        // Security boundary: Lab cannot access clinical records -> 403 Forbidden
        mockMvc.perform(get("/api/v1/test-rbac-e2e/clinical-encounter")
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Unauthenticated request returns HTTP 401 Unauthorized")
    void test7_UnauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/test-rbac-e2e/clinical-encounter"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh token rotation issues valid access token maintaining permissions")
    void test8_RefreshTokenRotationMaintainsPermissions() throws Exception {
        User doctorUser = createActiveUser("DOCTOR");
        LoginResponseDto loginResponse = login(doctorUser);

        RefreshTokenResponseDto refreshResponse = authenticationService.refreshToken(
                new RefreshTokenRequestDto(loginResponse.getRefreshToken())
        );

        assertNotNull(refreshResponse.getAccessToken());
        assertNotNull(refreshResponse.getRefreshToken());

        // Validate refreshed token on clinical encounter -> 200 OK
        mockMvc.perform(get("/api/v1/test-rbac-e2e/clinical-encounter")
                        .header("Authorization", "Bearer " + refreshResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().string("e2e-clinical-encounter-data"));

        // Validate boundary still enforced on refreshed token -> 403 Forbidden
        mockMvc.perform(post("/api/v1/test-rbac-e2e/admin-manage-provider")
                        .header("Authorization", "Bearer " + refreshResponse.getAccessToken()))
                .andExpect(status().isForbidden());
    }
}
