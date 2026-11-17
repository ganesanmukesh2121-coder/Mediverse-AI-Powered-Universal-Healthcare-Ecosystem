package com.mediverse.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Import(RbacPreAuthorizeSecurityTest.TestRbacEndpointsConfiguration.class)
class RbacPreAuthorizeSecurityTest {

    @TestConfiguration
    static class TestRbacEndpointsConfiguration {

        @RestController
        @RequestMapping("/api/v1/test-rbac")
        static class TestRbacController {

            @GetMapping("/patient-profile")
            @PreAuthorize("hasAuthority('PATIENT_VIEW_OWN_PROFILE')")
            public ResponseEntity<String> getPatientProfile() {
                return ResponseEntity.ok("patient-profile-data");
            }

            @GetMapping("/clinical-record")
            @PreAuthorize("hasAuthority('DOCTOR_VIEW_AUTHORIZED_RECORD')")
            public ResponseEntity<String> getClinicalRecord() {
                return ResponseEntity.ok("clinical-record-data");
            }

            @GetMapping("/emergency-vitals")
            @PreAuthorize("hasAuthority('EMERGENCY_VIEW_EMERGENCY_INFO')")
            public ResponseEntity<String> getEmergencyVitals() {
                return ResponseEntity.ok("emergency-vitals-data");
            }

            @PostMapping("/admin-verify")
            @PreAuthorize("hasAuthority('ADMIN_VERIFY_PROVIDER')")
            public ResponseEntity<String> verifyProvider() {
                return ResponseEntity.ok("provider-verified");
            }

            @PostMapping("/medical-admission")
            @PreAuthorize("hasAuthority('MEDICAL_CREATE_ADMISSION')")
            public ResponseEntity<String> createAdmission() {
                return ResponseEntity.ok("admission-created");
            }

            @PostMapping("/lab-order")
            @PreAuthorize("hasAuthority('LAB_CREATE_TEST_ORDER')")
            public ResponseEntity<String> createLabOrder() {
                return ResponseEntity.ok("lab-order-created");
            }
        }
    }

    @Autowired
    private WebApplicationContext context;

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

    private String createToken(String role, List<String> permissions) {
        String userId = UUID.randomUUID().toString();
        String healthId = "PATIENT".equalsIgnoreCase(role) ? "MV-2026-TEST1234" : null;
        return jwtService.generateAccessToken(
                userId,
                role.toLowerCase() + "@mediverse.com",
                role,
                "ACTIVE",
                healthId,
                permissions
        );
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401 Unauthorized")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/test-rbac/clinical-record"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authorized patient with PATIENT_VIEW_OWN_PROFILE succeeds with 200 OK")
    void authorizedPatientAccessSucceeds() throws Exception {
        String token = createToken("PATIENT", List.of("PATIENT_VIEW_OWN_PROFILE", "PATIENT_UPDATE_OWN_PROFILE"));

        mockMvc.perform(get("/api/v1/test-rbac/patient-profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("patient-profile-data"));
    }

    @Test
    @DisplayName("Patient cannot access doctor clinical endpoints (403 Forbidden)")
    void patientAccessToClinicalRecordDenied() throws Exception {
        String token = createToken("PATIENT", List.of("PATIENT_VIEW_OWN_PROFILE"));

        mockMvc.perform(get("/api/v1/test-rbac/clinical-record")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Authorized doctor with DOCTOR_VIEW_AUTHORIZED_RECORD succeeds with 200 OK")
    void authorizedDoctorAccessToClinicalRecordSucceeds() throws Exception {
        String token = createToken("DOCTOR", List.of("DOCTOR_VIEW_AUTHORIZED_RECORD", "DOCTOR_CREATE_CONSULTATION"));

        mockMvc.perform(get("/api/v1/test-rbac/clinical-record")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("clinical-record-data"));
    }

    @Test
    @DisplayName("Doctor cannot access admin verify endpoint (403 Forbidden)")
    void doctorAccessToAdminOperationDenied() throws Exception {
        String token = createToken("DOCTOR", List.of("DOCTOR_VIEW_AUTHORIZED_RECORD"));

        mockMvc.perform(post("/api/v1/test-rbac/admin-verify")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Authorized emergency responder with EMERGENCY_VIEW_EMERGENCY_INFO succeeds with 200 OK")
    void authorizedEmergencyAccessToVitalsSucceeds() throws Exception {
        String token = createToken("EMERGENCY", List.of("EMERGENCY_VIEW_EMERGENCY_INFO"));

        mockMvc.perform(get("/api/v1/test-rbac/emergency-vitals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("emergency-vitals-data"));
    }

    @Test
    @DisplayName("Emergency responder cannot access historical clinical records (403 Forbidden)")
    void emergencyAccessToClinicalRecordDenied() throws Exception {
        String token = createToken("EMERGENCY", List.of("EMERGENCY_VIEW_EMERGENCY_INFO"));

        mockMvc.perform(get("/api/v1/test-rbac/clinical-record")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Authorized admin with ADMIN_VERIFY_PROVIDER succeeds with 200 OK")
    void authorizedAdminAccessSucceeds() throws Exception {
        String token = createToken("ADMIN", List.of("ADMIN_VERIFY_PROVIDER", "ADMIN_MANAGE_ACCOUNT"));

        mockMvc.perform(post("/api/v1/test-rbac/admin-verify")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("provider-verified"));
    }

    @Test
    @DisplayName("Admin cannot access clinical records (Security Requirement 1: Zero Clinical Access)")
    void adminAccessToClinicalRecordDenied() throws Exception {
        String token = createToken("ADMIN", List.of("ADMIN_VERIFY_PROVIDER", "ADMIN_MANAGE_ACCOUNT", "ADMIN_VIEW_AUDIT"));

        mockMvc.perform(get("/api/v1/test-rbac/clinical-record")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Authorized medical organization with MEDICAL_CREATE_ADMISSION succeeds with 200 OK")
    void authorizedMedicalAccessSucceeds() throws Exception {
        String token = createToken("MEDICAL", List.of("MEDICAL_CREATE_ADMISSION", "MEDICAL_CREATE_TREATMENT"));

        mockMvc.perform(post("/api/v1/test-rbac/medical-admission")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("admission-created"));
    }

    @Test
    @DisplayName("Authorized laboratory with LAB_CREATE_TEST_ORDER succeeds with 200 OK")
    void authorizedLabAccessSucceeds() throws Exception {
        String token = createToken("LAB", List.of("LAB_CREATE_TEST_ORDER", "LAB_ENTER_RESULTS"));

        mockMvc.perform(post("/api/v1/test-rbac/lab-order")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("lab-order-created"));
    }

    @Test
    @DisplayName("Token with missing permissions is denied (403 Forbidden)")
    void tokenWithoutPermissionsDenied() throws Exception {
        // Patient role without any permissions in JWT
        String token = createToken("PATIENT", List.of());

        mockMvc.perform(get("/api/v1/test-rbac/patient-profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
