package com.mediverse.controller;

import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.Gender;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.repository.PatientProfileRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class PatientControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    private User patientUser;
    private PatientProfile patientProfile;
    private String patientHealthId;
    private String patientAccessToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        Role patientRole = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));

        patientUser = new User();
        patientUser.setEmail("patient_api_" + UUID.randomUUID() + "@mediverse.com");
        patientUser.setPasswordHash(passwordEncoder.encode("Password@123"));
        patientUser.setFullName("John Patient");
        patientUser.setPhoneNumber("987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7));
        patientUser.setRole(patientRole);
        patientUser.setAccountStatus(AccountStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);

        patientHealthId = "MV-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        patientProfile = new PatientProfile();
        patientProfile.setUser(patientUser);
        patientProfile.setHealthId(patientHealthId);
        patientProfile.setDateOfBirth(LocalDate.of(1990, 6, 15));
        patientProfile.setGender(Gender.MALE);
        patientProfile.setBloodGroup("O+");
        patientProfile.setEmergencyContactPhone("9123456780");
        patientProfile.setAddress("123 Mediverse St, Bangalore");
        patientProfile = patientProfileRepository.save(patientProfile);

        patientAccessToken = jwtService.generateAccessToken(
                patientUser.getUserId(),
                patientUser.getEmail(),
                "PATIENT",
                "ACTIVE",
                patientHealthId,
                List.of("PATIENT_VIEW_OWN_PROFILE", "PATIENT_UPDATE_OWN_PROFILE")
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

    private String toJson(String fullName, String phoneNumber, String dateOfBirth, String gender, String bloodGroup, String emergencyContactPhone, String address) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        if (fullName != null) {
            sb.append("\"fullName\":\"").append(fullName).append("\"");
            first = false;
        }
        if (phoneNumber != null) {
            if (!first) sb.append(",");
            sb.append("\"phoneNumber\":\"").append(phoneNumber).append("\"");
            first = false;
        }
        if (dateOfBirth != null) {
            if (!first) sb.append(",");
            sb.append("\"dateOfBirth\":\"").append(dateOfBirth).append("\"");
            first = false;
        }
        if (gender != null) {
            if (!first) sb.append(",");
            sb.append("\"gender\":\"").append(gender).append("\"");
            first = false;
        }
        if (bloodGroup != null) {
            if (!first) sb.append(",");
            sb.append("\"bloodGroup\":\"").append(bloodGroup).append("\"");
            first = false;
        }
        if (emergencyContactPhone != null) {
            if (!first) sb.append(",");
            sb.append("\"emergencyContactPhone\":\"").append(emergencyContactPhone).append("\"");
            first = false;
        }
        if (address != null) {
            if (!first) sb.append(",");
            sb.append("\"address\":\"").append(address).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @Test
    @DisplayName("1. Authenticated patient retrieves own profile — 200")
    void test1_AuthenticatedPatientRetrievesOwnProfile_200() throws Exception {
        mockMvc.perform(get("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + patientAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Patient"))
                .andExpect(jsonPath("$.email").value(patientUser.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(patientUser.getPhoneNumber()))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.healthId").value(patientHealthId))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.bloodGroup").value("O+"))
                .andExpect(jsonPath("$.emergencyContactPhone").value("9123456780"))
                .andExpect(jsonPath("$.address").value("123 Mediverse St, Bangalore"));
    }

    @Test
    @DisplayName("2. Authenticated patient updates own profile — 200")
    void test2_AuthenticatedPatientUpdatesOwnProfile_200() throws Exception {
        String updatePayload = toJson(
                "John Updated",
                "9876540000",
                "1991-07-20",
                "FEMALE",
                "AB+",
                "9123450000",
                "456 New Clinic Road, Mumbai"
        );

        mockMvc.perform(put("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + patientAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Updated"))
                .andExpect(jsonPath("$.phoneNumber").value("9876540000"))
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.bloodGroup").value("AB+"))
                .andExpect(jsonPath("$.emergencyContactPhone").value("9123450000"))
                .andExpect(jsonPath("$.address").value("456 New Clinic Road, Mumbai"));
    }

    @Test
    @DisplayName("3. Unauthenticated GET — 401")
    void test3_UnauthenticatedGet_401() throws Exception {
        mockMvc.perform(get("/api/v1/patients/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4. Unauthenticated PUT — 401")
    void test4_UnauthenticatedPut_401() throws Exception {
        String updatePayload = toJson(
                "Anonymous Name",
                "9876543210",
                "1990-01-01",
                "MALE",
                "O+",
                "9123456780",
                "Some Address"
        );

        mockMvc.perform(put("/api/v1/patients/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5. Doctor GET — 403")
    void test5_DoctorGet_403() throws Exception {
        String doctorToken = createToken("DOCTOR", List.of("DOCTOR_VIEW_AUTHORIZED_RECORD", "DOCTOR_ATTACH_DOCUMENT"));

        mockMvc.perform(get("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. Admin GET — 403")
    void test6_AdminGet_403() throws Exception {
        String adminToken = createToken("ADMIN", List.of("ADMIN_MANAGE_USERS", "ADMIN_VIEW_SYSTEM_METRICS", "ADMIN_VERIFY_PROVIDER"));

        mockMvc.perform(get("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7. Medical GET — 403")
    void test7_MedicalGet_403() throws Exception {
        String medicalToken = createToken("MEDICAL", List.of("MEDICAL_CREATE_ADMISSION", "MEDICAL_CREATE_DISCHARGE"));

        mockMvc.perform(get("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + medicalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("8. Lab GET — 403")
    void test8_LabGet_403() throws Exception {
        String labToken = createToken("LAB", List.of("LAB_CREATE_TEST_ORDER", "LAB_UPLOAD_RESULT"));

        mockMvc.perform(get("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + labToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("9. Emergency GET — 403")
    void test9_EmergencyGet_403() throws Exception {
        String emergencyToken = createToken("EMERGENCY", List.of("EMERGENCY_VIEW_EMERGENCY_INFO", "EMERGENCY_LOG_ACCESS"));

        mockMvc.perform(get("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + emergencyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("10. Patient without required permission — 403")
    void test10_PatientWithoutRequiredPermission_403() throws Exception {
        String patientWithoutPermToken = jwtService.generateAccessToken(
                patientUser.getUserId(),
                patientUser.getEmail(),
                "PATIENT",
                "ACTIVE",
                patientHealthId,
                List.of("PATIENT_UPLOAD_DOCUMENT") // missing PATIENT_VIEW_OWN_PROFILE & PATIENT_UPDATE_OWN_PROFILE
        );

        mockMvc.perform(get("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + patientWithoutPermToken))
                .andExpect(status().isForbidden());

        String updatePayload = toJson(
                "Name Without Perm",
                "9876543210",
                "1990-01-01",
                "MALE",
                "O+",
                "9123456780",
                "Some Address"
        );

        mockMvc.perform(put("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + patientWithoutPermToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("11. Invalid update payload — 400")
    void test11_InvalidUpdatePayload_400() throws Exception {
        // Blank full name and future date of birth
        String invalidPayload = """
                {
                    "fullName": "   ",
                    "phoneNumber": "9876543210",
                    "dateOfBirth": "2099-01-01",
                    "gender": "FEMALE",
                    "bloodGroup": "A+",
                    "emergencyContactPhone": "9123456780",
                    "address": "Address"
                }
                """;

        mockMvc.perform(put("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + patientAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("12. Patient cannot modify another patient's profile")
    void test12_PatientCannotModifyAnotherPatientsProfile() throws Exception {
        // Create second patient
        Role patientRole = roleRepository.findByRoleName("PATIENT").orElseThrow();

        User victimUser = new User();
        victimUser.setEmail("victim_" + UUID.randomUUID() + "@mediverse.com");
        victimUser.setPasswordHash(passwordEncoder.encode("Password@123"));
        victimUser.setFullName("Victim Patient");
        victimUser.setPhoneNumber("1234567890");
        victimUser.setRole(patientRole);
        victimUser.setAccountStatus(AccountStatus.ACTIVE);
        victimUser = userRepository.save(victimUser);

        String victimHealthId = "MV-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PatientProfile victimProfile = new PatientProfile();
        victimProfile.setUser(victimUser);
        victimProfile.setHealthId(victimHealthId);
        victimProfile.setDateOfBirth(LocalDate.of(1988, 3, 20));
        victimProfile.setGender(Gender.FEMALE);
        victimProfile.setBloodGroup("B+");
        victimProfile.setEmergencyContactPhone("1234567899");
        victimProfile.setAddress("456 Victim Lane");
        victimProfile = patientProfileRepository.save(victimProfile);

        // Patient 1 calls PUT /api/v1/patients/me
        String updatePayload = toJson(
                "Patient 1 Updated",
                "9876549999",
                "1990-06-15",
                "MALE",
                "O+",
                "9123459999",
                "Patient 1 Address"
        );

        mockMvc.perform(put("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + patientAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk());

        // Verify Victim Patient (Patient 2) is completely untouched
        PatientProfile reloadedVictim = patientProfileRepository.findByUser_UserId(victimUser.getUserId()).orElseThrow();
        assertEquals("Victim Patient", reloadedVictim.getUser().getFullName());
        assertEquals("1234567890", reloadedVictim.getUser().getPhoneNumber());
        assertEquals("456 Victim Lane", reloadedVictim.getAddress());
        assertEquals(victimHealthId, reloadedVictim.getHealthId());
    }

    @Test
    @DisplayName("13. Health ID remains unchanged after API update")
    void test13_HealthIdRemainsUnchangedAfterApiUpdate() throws Exception {
        String updatePayload = toJson(
                "Name After HealthId Check",
                "9876541111",
                "1990-06-15",
                "MALE",
                "O+",
                "9123451111",
                "Address After Update"
        );

        mockMvc.perform(put("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + patientAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthId").value(patientHealthId));

        PatientProfile persistedProfile = patientProfileRepository.findByUser_UserId(patientUser.getUserId()).orElseThrow();
        assertEquals(patientHealthId, persistedProfile.getHealthId());
    }

    @Test
    @DisplayName("14. Doctor PUT to patient profile endpoint returns 403 Forbidden")
    void test14_DoctorPut_403() throws Exception {
        String doctorToken = createToken("DOCTOR", List.of("DOCTOR_VIEW_AUTHORIZED_RECORD"));

        String updatePayload = toJson(
                "Doctor Tamper Attempt",
                "9876540000",
                "1990-01-01",
                "MALE",
                "O+",
                "9123450000",
                "Tamper Address"
        );

        mockMvc.perform(put("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isForbidden());
    }
}
