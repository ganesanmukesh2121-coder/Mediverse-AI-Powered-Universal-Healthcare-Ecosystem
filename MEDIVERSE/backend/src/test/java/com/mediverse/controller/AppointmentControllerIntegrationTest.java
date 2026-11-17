package com.mediverse.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediverse.dto.CreateAppointmentRequestDto;
import com.mediverse.dto.UpdateAppointmentStatusRequestDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.Appointment;
import com.mediverse.entity.AppointmentStatus;
import com.mediverse.entity.AppointmentType;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.DoctorProfileRepository;
import com.mediverse.repository.PatientProfileRepository;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;
import com.mediverse.security.JwtService;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class AppointmentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private Role patientRole;
    private Role doctorRole;
    private Role adminRole;
    private Role emergencyRole;
    private Role medicalRole;
    private Role labRole;

    private User patientUser;
    private PatientProfile patientProfile;
    private String patientToken;

    private User doctorUser;
    private DoctorProfile doctorProfile;
    private String doctorToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        patientRole = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));
        doctorRole = roleRepository.findByRoleName("DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("DOCTOR", "Doctor role")));
        adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Admin role")));
        emergencyRole = roleRepository.findByRoleName("EMERGENCY")
                .orElseGet(() -> roleRepository.save(new Role("EMERGENCY", "Emergency role")));
        medicalRole = roleRepository.findByRoleName("MEDICAL")
                .orElseGet(() -> roleRepository.save(new Role("MEDICAL", "Medical role")));
        labRole = roleRepository.findByRoleName("LAB")
                .orElseGet(() -> roleRepository.save(new Role("LAB", "Lab role")));

        // Create default test patient
        patientUser = createUser("ctrl_patient", patientRole, AccountStatus.ACTIVE);
        patientProfile = createPatient(patientUser);
        patientToken = jwtService.generateAccessToken(
                patientUser.getUserId(),
                patientUser.getEmail(),
                "PATIENT",
                "ACTIVE",
                patientProfile.getHealthId(),
                List.of("PATIENT_VIEW_OWN_PROFILE")
        );

        // Create default test doctor
        doctorUser = createUser("ctrl_doctor", doctorRole, AccountStatus.ACTIVE);
        doctorProfile = createDoctor(doctorUser, VerificationStatus.ACTIVE);
        doctorToken = jwtService.generateAccessToken(
                doctorUser.getUserId(),
                doctorUser.getEmail(),
                "DOCTOR",
                "ACTIVE",
                null,
                List.of("DOCTOR_VIEW_AUTHORIZED_RECORD")
        );
    }

    private User createUser(String prefix, Role role, AccountStatus status) {
        User user = new User();
        user.setEmail(prefix + "_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash(passwordEncoder.encode("TestPass@123"));
        user.setFullName("User " + prefix);
        user.setPhoneNumber("987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7));
        user.setRole(role);
        user.setAccountStatus(status);
        return userRepository.save(user);
    }

    private PatientProfile createPatient(User user) {
        PatientProfile profile = new PatientProfile();
        profile.setUser(user);
        profile.setHealthId("MV-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        profile.setDateOfBirth(LocalDate.of(1991, 7, 20));
        profile.setAddress("Bangalore");
        return patientProfileRepository.save(profile);
    }

    private DoctorProfile createDoctor(User user, VerificationStatus status) {
        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setLicenseNumber("LIC-" + UUID.randomUUID().toString().substring(0, 8));
        profile.setSpecialization("Cardiology");
        profile.setHospitalAffiliation("Apollo Hospital");
        profile.setVerificationStatus(status);
        return doctorProfileRepository.save(profile);
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

    private Appointment createDbAppointment(PatientProfile patient, DoctorProfile doctor, LocalDateTime date,
                                            AppointmentType type, AppointmentStatus status, String reason) {
        Appointment apt = new Appointment();
        apt.setPatient(patient);
        apt.setDoctor(doctor);
        apt.setAppointmentDate(date);
        apt.setAppointmentType(type);
        apt.setStatus(status);
        apt.setReason(reason);
        return appointmentRepository.save(apt);
    }

    // ==========================================
    // 20 REQUIRED INTEGRATION TEST SCENARIOS
    // ==========================================

    @Test
    @DisplayName("1. Unauthenticated POST returns 401")
    void test01_UnauthenticatedPost_Returns401() throws Exception {
        String payload = String.format("""
                {
                    "doctorId": "%s",
                    "appointmentDate": "%s",
                    "appointmentType": "IN_PERSON",
                    "reason": "Routine checkup"
                }
                """, doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(2).withNano(0));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. PATIENT can schedule an appointment")
    void test02_PatientCanScheduleAppointment_201() throws Exception {
        LocalDateTime date = LocalDateTime.now().plusDays(2).withNano(0);
        String payload = String.format("""
                {
                    "doctorId": "%s",
                    "appointmentDate": "%s",
                    "appointmentType": "IN_PERSON",
                    "reason": "Cardiology checkup"
                }
                """, doctorProfile.getDoctorId(), date);

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentId").exists())
                .andExpect(jsonPath("$.patientId").value(patientProfile.getPatientId()))
                .andExpect(jsonPath("$.patientName").value(patientUser.getFullName()))
                .andExpect(jsonPath("$.doctorId").value(doctorProfile.getDoctorId()))
                .andExpect(jsonPath("$.doctorName").value(doctorUser.getFullName()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.appointmentType").value("IN_PERSON"))
                .andExpect(jsonPath("$.reason").value("Cardiology checkup"));
    }

    @Test
    @DisplayName("3. Invalid booking payload returns 400")
    void test03_InvalidBookingPayload_Returns400() throws Exception {
        // Missing doctorId, past date, null appointmentType
        String invalidPayload = """
                {
                    "doctorId": "",
                    "appointmentDate": "2020-01-01T10:00:00",
                    "appointmentType": null,
                    "reason": "Invalid"
                }
                """;

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("4. DOCTOR cannot schedule an appointment")
    void test04_DoctorCannotScheduleAppointment_403() throws Exception {
        String payload = String.format("""
                {
                    "doctorId": "%s",
                    "appointmentDate": "%s",
                    "appointmentType": "IN_PERSON",
                    "reason": "Doctor self-scheduling"
                }
                """, doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1).withNano(0));

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. PATIENT can list their own appointments")
    void test05_PatientCanListTheirOwnAppointments_200() throws Exception {
        createDbAppointment(patientProfile, doctorProfile, LocalDateTime.now().plusDays(1).withNano(0),
                AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Patient Apt 1");

        mockMvc.perform(get("/api/v1/appointments/me")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(patientProfile.getPatientId()))
                .andExpect(jsonPath("$[0].reason").value("Patient Apt 1"));
    }

    @Test
    @DisplayName("6. DOCTOR can list assigned appointments")
    void test06_DoctorCanListAssignedAppointments_200() throws Exception {
        createDbAppointment(patientProfile, doctorProfile, LocalDateTime.now().plusDays(1).withNano(0),
                AppointmentType.TELECONSULT, AppointmentStatus.SCHEDULED, "Doctor Apt 1");

        mockMvc.perform(get("/api/v1/appointments/me")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].doctorId").value(doctorProfile.getDoctorId()))
                .andExpect(jsonPath("$[0].reason").value("Doctor Apt 1"));
    }

    @Test
    @DisplayName("7. PATIENT cannot see another patient's appointments")
    void test07_PatientCannotSeeAnotherPatientsAppointments() throws Exception {
        User patient2User = createUser("other_patient", patientRole, AccountStatus.ACTIVE);
        PatientProfile patient2Profile = createPatient(patient2User);

        createDbAppointment(patientProfile, doctorProfile, LocalDateTime.now().plusDays(1).withNano(0),
                AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Patient 1 Appointment");
        createDbAppointment(patient2Profile, doctorProfile, LocalDateTime.now().plusDays(2).withNano(0),
                AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Patient 2 Appointment");

        mockMvc.perform(get("/api/v1/appointments/me")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reason").value("Patient 1 Appointment"));
    }

    @Test
    @DisplayName("8. DOCTOR cannot see another doctor's appointments")
    void test08_DoctorCannotSeeAnotherDoctorsAppointments() throws Exception {
        User doc2User = createUser("other_doc", doctorRole, AccountStatus.ACTIVE);
        DoctorProfile doc2Profile = createDoctor(doc2User, VerificationStatus.ACTIVE);

        createDbAppointment(patientProfile, doctorProfile, LocalDateTime.now().plusDays(1).withNano(0),
                AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Doc 1 Appointment");
        createDbAppointment(patientProfile, doc2Profile, LocalDateTime.now().plusDays(2).withNano(0),
                AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Doc 2 Appointment");

        mockMvc.perform(get("/api/v1/appointments/me")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reason").value("Doc 1 Appointment"));
    }

    @Test
    @DisplayName("9. ADMIN receives 403 for appointment listing")
    void test09_AdminReceives403ForAppointmentListing() throws Exception {
        String adminToken = createToken("ADMIN", List.of("ADMIN_VERIFY_PROVIDER", "ADMIN_MANAGE_ACCOUNT"));

        mockMvc.perform(get("/api/v1/appointments/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("10. EMERGENCY receives 403")
    void test10_EmergencyReceives403() throws Exception {
        String emergencyToken = createToken("EMERGENCY", List.of("EMERGENCY_VIEW_EMERGENCY_INFO"));

        mockMvc.perform(get("/api/v1/appointments/me")
                        .header("Authorization", "Bearer " + emergencyToken))
                .andExpect(status().isForbidden());

        String validPayload = String.format("""
                {
                    "doctorId": "%s",
                    "appointmentDate": "%s",
                    "appointmentType": "IN_PERSON",
                    "reason": "Emergency role test"
                }
                """, doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(2).withNano(0));

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + emergencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("11. MEDICAL receives 403")
    void test11_MedicalReceives403() throws Exception {
        String medicalToken = createToken("MEDICAL", List.of("MEDICAL_CREATE_ADMISSION"));

        mockMvc.perform(get("/api/v1/appointments/me")
                        .header("Authorization", "Bearer " + medicalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("12. LAB receives 403")
    void test12_LabReceives403() throws Exception {
        String labToken = createToken("LAB", List.of("LAB_CREATE_TEST_ORDER"));

        mockMvc.perform(get("/api/v1/appointments/me")
                        .header("Authorization", "Bearer " + labToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("13. PATIENT can cancel their own scheduled appointment")
    void test13_PatientCanCancelTheirOwnScheduledAppointment() throws Exception {
        Appointment apt = createDbAppointment(patientProfile, doctorProfile, LocalDateTime.now().plusDays(1).withNano(0),
                AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "To Cancel");

        String payload = """
                {
                    "status": "CANCELLED",
                    "cancellationReason": "Patient cancelled"
                }
                """;

        mockMvc.perform(put("/api/v1/appointments/" + apt.getAppointmentId() + "/status")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("14. PATIENT cannot mark appointment COMPLETED")
    void test14_PatientCannotMarkAppointmentCompleted_403() throws Exception {
        Appointment apt = createDbAppointment(patientProfile, doctorProfile, LocalDateTime.now().plusDays(1).withNano(0),
                AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Visit");

        String payload = """
                {
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(put("/api/v1/appointments/" + apt.getAppointmentId() + "/status")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("15. Assigned DOCTOR can mark appointment COMPLETED")
    void test15_AssignedDoctorCanMarkAppointmentCompleted_200() throws Exception {
        Appointment apt = createDbAppointment(patientProfile, doctorProfile, LocalDateTime.now().plusDays(1).withNano(0),
                AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Doctor Completes");

        String payload = """
                {
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(put("/api/v1/appointments/" + apt.getAppointmentId() + "/status")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("16. Unrelated DOCTOR cannot update another doctor's appointment")
    void test16_UnrelatedDoctorCannotUpdateAnotherDoctorsAppointment_403() throws Exception {
        Appointment apt = createDbAppointment(patientProfile, doctorProfile, LocalDateTime.now().plusDays(1).withNano(0),
                AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Doc 1 Appointment");

        User doc2User = createUser("doc2_intruder", doctorRole, AccountStatus.ACTIVE);
        createDoctor(doc2User, VerificationStatus.ACTIVE);
        String doc2Token = jwtService.generateAccessToken(
                doc2User.getUserId(), doc2User.getEmail(), "DOCTOR", "ACTIVE", null, List.of("DOCTOR_VIEW_AUTHORIZED_RECORD"));

        String payload = """
                {
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(put("/api/v1/appointments/" + apt.getAppointmentId() + "/status")
                        .header("Authorization", "Bearer " + doc2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("17. Unrelated PATIENT cannot update another patient's appointment")
    void test17_UnrelatedPatientCannotUpdateAnotherPatientsAppointment_403() throws Exception {
        Appointment apt = createDbAppointment(patientProfile, doctorProfile, LocalDateTime.now().plusDays(1).withNano(0),
                AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Patient 1 Appointment");

        User patient2User = createUser("pat2_intruder", patientRole, AccountStatus.ACTIVE);
        PatientProfile p2Profile = createPatient(patient2User);
        String patient2Token = jwtService.generateAccessToken(
                patient2User.getUserId(), patient2User.getEmail(), "PATIENT", "ACTIVE", p2Profile.getHealthId(), List.of("PATIENT_VIEW_OWN_PROFILE"));

        String payload = """
                {
                    "status": "CANCELLED",
                    "cancellationReason": "Intruder cancels"
                }
                """;

        mockMvc.perform(put("/api/v1/appointments/" + apt.getAppointmentId() + "/status")
                        .header("Authorization", "Bearer " + patient2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("18. Invalid status transition returns expected client error")
    void test18_InvalidStatusTransition_Returns400() throws Exception {
        Appointment apt = createDbAppointment(patientProfile, doctorProfile, LocalDateTime.now().plusDays(1).withNano(0),
                AppointmentType.IN_PERSON, AppointmentStatus.COMPLETED, "Already Completed");

        String payload = """
                {
                    "status": "CANCELLED"
                }
                """;

        mockMvc.perform(put("/api/v1/appointments/" + apt.getAppointmentId() + "/status")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("19. Audit records are persisted for successful booking and status changes")
    void test19_AuditRecordsPersistedForBookingAndStatusChange() throws Exception {
        long initialAuditCount = auditLogRepository.count();

        // 1. Schedule via API
        LocalDateTime date = LocalDateTime.now().plusDays(3).withNano(0);
        String bookPayload = String.format("""
                {
                    "doctorId": "%s",
                    "appointmentDate": "%s",
                    "appointmentType": "TELECONSULT",
                    "reason": "Audit test booking"
                }
                """, doctorProfile.getDoctorId(), date);

        String responseStr = mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(responseStr).get("appointmentId").asText();

        // 2. Status update via API
        String statusPayload = """
                {
                    "status": "CANCELLED",
                    "cancellationReason": "Patient conflict reason"
                }
                """;

        mockMvc.perform(put("/api/v1/appointments/" + appointmentId + "/status")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusPayload))
                .andExpect(status().isOk());

        // Verify audit log increment
        assertEquals(initialAuditCount + 2, auditLogRepository.count());
        List<AuditLog> logs = auditLogRepository.findAll();
        AuditLog scheduleLog = logs.get(logs.size() - 2);
        AuditLog updateLog = logs.get(logs.size() - 1);

        assertEquals(patientUser.getUserId(), scheduleLog.getActorUserId());
        assertEquals("APPOINTMENT_SCHEDULE", scheduleLog.getAction());

        assertEquals(patientUser.getUserId(), updateLog.getActorUserId());
        assertEquals("APPOINTMENT_STATUS_UPDATE", updateLog.getAction());
        assertTrue(updateLog.getResourceAccessed().contains("CANCELLED"));
        assertTrue(updateLog.getResourceAccessed().contains("Patient conflict reason"));
    }

    @Test
    @DisplayName("20. Request bodies cannot spoof patient or actor identity")
    void test20_RequestBodiesCannotSpoofPatientOrActorIdentity() throws Exception {
        // Create an innocent victim patient
        User victimUser = createUser("victim_pat", patientRole, AccountStatus.ACTIVE);
        PatientProfile victimProfile = createPatient(victimUser);

        // Attacker payload attempting to inject victim's patientId / patientUserId
        String maliciousPayload = String.format("""
                {
                    "doctorId": "%s",
                    "appointmentDate": "%s",
                    "appointmentType": "IN_PERSON",
                    "reason": "Spoof attempt",
                    "patientId": "%s",
                    "patientUserId": "%s"
                }
                """, doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(4).withNano(0),
                victimProfile.getPatientId(), victimUser.getUserId());

        String responseStr = mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maliciousPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // The appointment MUST belong to authenticated patient, not victim
        String bookedPatientId = objectMapper.readTree(responseStr).get("patientId").asText();
        String bookedPatientName = objectMapper.readTree(responseStr).get("patientName").asText();

        assertEquals(patientProfile.getPatientId(), bookedPatientId);
        assertEquals(patientUser.getFullName(), bookedPatientName);
        assertTrue(!victimProfile.getPatientId().equals(bookedPatientId));
    }
}
