package com.mediverse.service;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.AppointmentResponseDto;
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
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.DoctorProfileRepository;
import com.mediverse.repository.PatientProfileRepository;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private Role patientRole;
    private Role doctorRole;
    private Role adminRole;

    private User patientUser;
    private PatientProfile patientProfile;

    private User doctorUser;
    private DoctorProfile doctorProfile;

    @BeforeEach
    void setUp() {
        patientRole = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));
        doctorRole = roleRepository.findByRoleName("DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("DOCTOR", "Doctor role")));
        adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Admin role")));

        // Create standard test patient
        patientUser = createUser("svc_patient", patientRole, AccountStatus.ACTIVE);
        patientProfile = createPatient(patientUser);

        // Create standard test doctor
        doctorUser = createUser("svc_doctor", doctorRole, AccountStatus.ACTIVE);
        doctorProfile = createDoctor(doctorUser, VerificationStatus.ACTIVE);
    }

    private User createUser(String prefix, Role role, AccountStatus status) {
        User user = new User();
        user.setEmail(prefix + "_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash("hashed_password_123");
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
        profile.setDateOfBirth(LocalDate.of(1992, 5, 10));
        profile.setAddress("Bangalore, Karnataka");
        return patientProfileRepository.save(profile);
    }

    private DoctorProfile createDoctor(User user, VerificationStatus status) {
        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setLicenseNumber("DOC-LIC-" + UUID.randomUUID().toString().substring(0, 8));
        profile.setSpecialization("General Medicine");
        profile.setHospitalAffiliation("City Care Hospital");
        profile.setVerificationStatus(status);
        return doctorProfileRepository.save(profile);
    }

    // ==========================================
    // BOOKING TESTS
    // ==========================================

    @Test
    @DisplayName("1. Successful patient booking")
    void test01_SuccessfulPatientBooking() {
        LocalDateTime aptDate = LocalDateTime.now().plusDays(2).withNano(0);
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(),
                aptDate,
                AppointmentType.IN_PERSON,
                "General consultation"
        );

        AppointmentResponseDto response = appointmentService.createAppointment(patientUser.getUserId(), request);

        assertNotNull(response);
        assertNotNull(response.getAppointmentId());
        assertEquals(patientProfile.getPatientId(), response.getPatientId());
        assertEquals(patientUser.getFullName(), response.getPatientName());
        assertEquals(doctorProfile.getDoctorId(), response.getDoctorId());
        assertEquals(doctorUser.getFullName(), response.getDoctorName());
        assertEquals(aptDate, response.getAppointmentDate());
        assertEquals("IN_PERSON", response.getAppointmentType());
        assertEquals("SCHEDULED", response.getStatus());
        assertEquals("General consultation", response.getReason());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    @DisplayName("2. Patient profile not found throws ResourceNotFoundException")
    void test02_PatientProfileNotFound() {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(),
                LocalDateTime.now().plusDays(1),
                AppointmentType.TELECONSULT,
                "Checkup"
        );

        assertThrows(ResourceNotFoundException.class, () ->
                appointmentService.createAppointment("unknown-patient-user-id", request));
    }

    @Test
    @DisplayName("3. Inactive patient rejected")
    void test03_InactivePatientRejected() {
        User inactiveUser = createUser("inactive_patient", patientRole, AccountStatus.SUSPENDED);
        createPatient(inactiveUser);

        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(),
                LocalDateTime.now().plusDays(1),
                AppointmentType.IN_PERSON,
                "Checkup"
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointmentService.createAppointment(inactiveUser.getUserId(), request));
        assertTrue(ex.getMessage().contains("Patient account is not active"));
    }

    @Test
    @DisplayName("4. Doctor profile not found throws ResourceNotFoundException")
    void test04_DoctorProfileNotFound() {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                "unknown-doctor-id",
                LocalDateTime.now().plusDays(1),
                AppointmentType.IN_PERSON,
                "Checkup"
        );

        assertThrows(ResourceNotFoundException.class, () ->
                appointmentService.createAppointment(patientUser.getUserId(), request));
    }

    @Test
    @DisplayName("5. Inactive doctor rejected")
    void test05_InactiveDoctorRejected() {
        User inactiveDocUser = createUser("inactive_doc", doctorRole, AccountStatus.PENDING);
        DoctorProfile inactiveDoc = createDoctor(inactiveDocUser, VerificationStatus.ACTIVE);

        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                inactiveDoc.getDoctorId(),
                LocalDateTime.now().plusDays(1),
                AppointmentType.IN_PERSON,
                "Checkup"
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointmentService.createAppointment(patientUser.getUserId(), request));
        assertTrue(ex.getMessage().contains("Doctor account is not active"));
    }

    @Test
    @DisplayName("6. Unverified doctor rejected")
    void test06_UnverifiedDoctorRejected() {
        User docUser = createUser("unverified_doc", doctorRole, AccountStatus.ACTIVE);
        DoctorProfile unverifiedDoc = createDoctor(docUser, VerificationStatus.PENDING);

        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                unverifiedDoc.getDoctorId(),
                LocalDateTime.now().plusDays(1),
                AppointmentType.IN_PERSON,
                "Checkup"
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointmentService.createAppointment(patientUser.getUserId(), request));
        assertTrue(ex.getMessage().contains("Doctor profile is not verified"));
    }

    @Test
    @DisplayName("7. Doctor exact-time conflict rejected")
    void test07_DoctorExactTimeConflictRejected() {
        LocalDateTime date = LocalDateTime.now().plusDays(3).withNano(0);

        CreateAppointmentRequestDto request1 = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(),
                date,
                AppointmentType.IN_PERSON,
                "First booking"
        );
        appointmentService.createAppointment(patientUser.getUserId(), request1);

        // Second booking with different patient but same doctor and exact same time
        User patient2User = createUser("patient2", patientRole, AccountStatus.ACTIVE);
        createPatient(patient2User);

        CreateAppointmentRequestDto request2 = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(),
                date,
                AppointmentType.TELECONSULT,
                "Conflicting booking"
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointmentService.createAppointment(patient2User.getUserId(), request2));
        assertTrue(ex.getMessage().contains("Doctor already has a scheduled appointment"));
    }

    @Test
    @DisplayName("8. Patient exact-time conflict rejected")
    void test08_PatientExactTimeConflictRejected() {
        LocalDateTime date = LocalDateTime.now().plusDays(4).withNano(0);

        CreateAppointmentRequestDto request1 = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(),
                date,
                AppointmentType.IN_PERSON,
                "Booking with doc 1"
        );
        appointmentService.createAppointment(patientUser.getUserId(), request1);

        // Create second doctor
        User doc2User = createUser("doc2", doctorRole, AccountStatus.ACTIVE);
        DoctorProfile doc2Profile = createDoctor(doc2User, VerificationStatus.ACTIVE);

        // Same patient attempts booking with doc 2 at exact same time
        CreateAppointmentRequestDto request2 = new CreateAppointmentRequestDto(
                doc2Profile.getDoctorId(),
                date,
                AppointmentType.TELECONSULT,
                "Conflicting booking for patient"
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointmentService.createAppointment(patientUser.getUserId(), request2));
        assertTrue(ex.getMessage().contains("Patient already has a scheduled appointment"));
    }

    @Test
    @DisplayName("9. New appointment always starts as SCHEDULED")
    void test09_NewAppointmentAlwaysStartsAsScheduled() {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(),
                LocalDateTime.now().plusDays(5),
                AppointmentType.IN_PERSON,
                "Initial Status Check"
        );

        AppointmentResponseDto response = appointmentService.createAppointment(patientUser.getUserId(), request);
        assertEquals("SCHEDULED", response.getStatus());

        Appointment inDb = appointmentRepository.findById(response.getAppointmentId()).orElseThrow();
        assertEquals(AppointmentStatus.SCHEDULED, inDb.getStatus());
    }

    @Test
    @DisplayName("10. Booking audit record is created")
    void test10_BookingAuditRecordIsCreated() {
        long initialAuditCount = auditLogRepository.count();

        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(),
                LocalDateTime.now().plusDays(6),
                AppointmentType.IN_PERSON,
                "Audit Check"
        );

        AppointmentResponseDto response = appointmentService.createAppointment(patientUser.getUserId(), request);

        assertEquals(initialAuditCount + 1, auditLogRepository.count());
        List<AuditLog> logs = auditLogRepository.findAll();
        AuditLog latestLog = logs.get(logs.size() - 1);

        assertEquals(patientUser.getUserId(), latestLog.getActorUserId());
        assertEquals(patientUser.getUserId(), latestLog.getTargetPatientId());
        assertEquals("APPOINTMENT_SCHEDULE", latestLog.getAction());
        assertTrue(latestLog.getResourceAccessed().contains(response.getAppointmentId()));
    }

    @Test
    @DisplayName("11. Audit failure rolls back booking")
    void test11_AuditFailureRollsBackBooking() {
        AppointmentRepository mockAptRepo = Mockito.mock(AppointmentRepository.class);
        PatientProfileRepository mockPatRepo = Mockito.mock(PatientProfileRepository.class);
        DoctorProfileRepository mockDocRepo = Mockito.mock(DoctorProfileRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        AuditLogRepository mockAuditRepo = Mockito.mock(AuditLogRepository.class);

        AppointmentService mockService = new AppointmentService(
                mockAptRepo, mockPatRepo, mockDocRepo, mockUserRepo, mockAuditRepo
        );

        Mockito.when(mockPatRepo.findByUser_UserId("p1")).thenReturn(Optional.of(patientProfile));
        Mockito.when(mockDocRepo.findById("d1")).thenReturn(Optional.of(doctorProfile));
        Mockito.when(mockAptRepo.existsByDoctor_DoctorIdAndAppointmentDateAndStatus(any(), any(), any())).thenReturn(false);
        Mockito.when(mockAptRepo.existsByPatient_PatientIdAndAppointmentDateAndStatus(any(), any(), any())).thenReturn(false);

        Appointment mockSavedApt = new Appointment();
        mockSavedApt.setAppointmentId("apt-123");
        mockSavedApt.setPatient(patientProfile);
        mockSavedApt.setDoctor(doctorProfile);
        mockSavedApt.setStatus(AppointmentStatus.SCHEDULED);
        Mockito.when(mockAptRepo.save(any(Appointment.class))).thenReturn(mockSavedApt);

        Mockito.doThrow(new RuntimeException("Simulated audit disk failure"))
                .when(mockAuditRepo).save(any(AuditLog.class));

        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                "d1",
                LocalDateTime.now().plusDays(1),
                AppointmentType.IN_PERSON,
                "Rollback Test"
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                mockService.createAppointment("p1", request));
        assertEquals("Simulated audit disk failure", ex.getMessage());

        // Verify @Transactional is present on createAppointment
        try {
            Method method = AppointmentService.class.getMethod("createAppointment", String.class, CreateAppointmentRequestDto.class);
            assertTrue(method.isAnnotationPresent(Transactional.class),
                    "createAppointment must be annotated with @Transactional to ensure rollback on audit failure");
        } catch (NoSuchMethodException e) {
            fail("createAppointment method not found");
        }
    }

    // ==========================================
    // LISTING TESTS
    // ==========================================

    @Test
    @DisplayName("12. Patient sees only their own appointments")
    void test12_PatientSeesOnlyTheirOwnAppointments() {
        User patient2 = createUser("patient_list_2", patientRole, AccountStatus.ACTIVE);
        createPatient(patient2);

        CreateAppointmentRequestDto req1 = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "P1 Apt");
        CreateAppointmentRequestDto req2 = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(2), AppointmentType.IN_PERSON, "P2 Apt");

        appointmentService.createAppointment(patientUser.getUserId(), req1);
        appointmentService.createAppointment(patient2.getUserId(), req2);

        List<AppointmentResponseDto> p1List = appointmentService.getMyAppointments(patientUser.getUserId(), "PATIENT");
        assertEquals(1, p1List.size());
        assertEquals("P1 Apt", p1List.get(0).getReason());

        List<AppointmentResponseDto> p2List = appointmentService.getMyAppointments(patient2.getUserId(), "PATIENT");
        assertEquals(1, p2List.size());
        assertEquals("P2 Apt", p2List.get(0).getReason());
    }

    @Test
    @DisplayName("13. Doctor sees only their assigned appointments")
    void test13_DoctorSeesOnlyTheirAssignedAppointments() {
        User doc2User = createUser("doc_list_2", doctorRole, AccountStatus.ACTIVE);
        DoctorProfile doc2Profile = createDoctor(doc2User, VerificationStatus.ACTIVE);

        CreateAppointmentRequestDto req1 = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Doc 1 Apt");
        CreateAppointmentRequestDto req2 = new CreateAppointmentRequestDto(
                doc2Profile.getDoctorId(), LocalDateTime.now().plusDays(2), AppointmentType.IN_PERSON, "Doc 2 Apt");

        appointmentService.createAppointment(patientUser.getUserId(), req1);
        appointmentService.createAppointment(patientUser.getUserId(), req2);

        List<AppointmentResponseDto> d1List = appointmentService.getMyAppointments(doctorUser.getUserId(), "DOCTOR");
        assertEquals(1, d1List.size());
        assertEquals("Doc 1 Apt", d1List.get(0).getReason());

        List<AppointmentResponseDto> d2List = appointmentService.getMyAppointments(doc2User.getUserId(), "DOCTOR");
        assertEquals(1, d2List.size());
        assertEquals("Doc 2 Apt", d2List.get(0).getReason());
    }

    @Test
    @DisplayName("14. Unsupported roles are rejected")
    void test14_UnsupportedRolesAreRejected() {
        assertThrows(AccessDeniedException.class, () ->
                appointmentService.getMyAppointments(patientUser.getUserId(), "ADMIN"));
        assertThrows(AccessDeniedException.class, () ->
                appointmentService.getMyAppointments(patientUser.getUserId(), "EMERGENCY"));
        assertThrows(AccessDeniedException.class, () ->
                appointmentService.getMyAppointments(patientUser.getUserId(), "LAB"));
        assertThrows(AccessDeniedException.class, () ->
                appointmentService.getMyAppointments(patientUser.getUserId(), "MEDICAL"));
    }

    // ==========================================
    // STATUS UPDATE TESTS
    // ==========================================

    @Test
    @DisplayName("15. Assigned doctor can mark appointment COMPLETED")
    void test15_AssignedDoctorCanMarkAppointmentCompleted() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Check");
        AppointmentResponseDto booked = appointmentService.createAppointment(patientUser.getUserId(), req);

        UpdateAppointmentStatusRequestDto update = new UpdateAppointmentStatusRequestDto(AppointmentStatus.COMPLETED);
        AppointmentResponseDto updated = appointmentService.updateAppointmentStatus(
                booked.getAppointmentId(), doctorUser.getUserId(), update);

        assertEquals("COMPLETED", updated.getStatus());
        Appointment inDb = appointmentRepository.findById(booked.getAppointmentId()).orElseThrow();
        assertEquals(AppointmentStatus.COMPLETED, inDb.getStatus());
    }

    @Test
    @DisplayName("16. Assigned doctor can mark appointment NO_SHOW")
    void test16_AssignedDoctorCanMarkAppointmentNoShow() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Check");
        AppointmentResponseDto booked = appointmentService.createAppointment(patientUser.getUserId(), req);

        UpdateAppointmentStatusRequestDto update = new UpdateAppointmentStatusRequestDto(AppointmentStatus.NO_SHOW);
        AppointmentResponseDto updated = appointmentService.updateAppointmentStatus(
                booked.getAppointmentId(), doctorUser.getUserId(), update);

        assertEquals("NO_SHOW", updated.getStatus());
        Appointment inDb = appointmentRepository.findById(booked.getAppointmentId()).orElseThrow();
        assertEquals(AppointmentStatus.NO_SHOW, inDb.getStatus());
    }

    @Test
    @DisplayName("17. Owning patient can cancel")
    void test17_OwningPatientCanCancel() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Check");
        AppointmentResponseDto booked = appointmentService.createAppointment(patientUser.getUserId(), req);

        UpdateAppointmentStatusRequestDto update = new UpdateAppointmentStatusRequestDto(
                AppointmentStatus.CANCELLED, "Unable to attend due to work schedule");
        AppointmentResponseDto updated = appointmentService.updateAppointmentStatus(
                booked.getAppointmentId(), patientUser.getUserId(), update);

        assertEquals("CANCELLED", updated.getStatus());
        Appointment inDb = appointmentRepository.findById(booked.getAppointmentId()).orElseThrow();
        assertEquals(AppointmentStatus.CANCELLED, inDb.getStatus());
    }

    @Test
    @DisplayName("18. Assigned doctor can cancel")
    void test18_AssignedDoctorCanCancel() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Check");
        AppointmentResponseDto booked = appointmentService.createAppointment(patientUser.getUserId(), req);

        UpdateAppointmentStatusRequestDto update = new UpdateAppointmentStatusRequestDto(
                AppointmentStatus.CANCELLED, "Doctor on emergency leave");
        AppointmentResponseDto updated = appointmentService.updateAppointmentStatus(
                booked.getAppointmentId(), doctorUser.getUserId(), update);

        assertEquals("CANCELLED", updated.getStatus());
    }

    @Test
    @DisplayName("19. Patient cannot mark appointment COMPLETED")
    void test19_PatientCannotMarkAppointmentCompleted() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Check");
        AppointmentResponseDto booked = appointmentService.createAppointment(patientUser.getUserId(), req);

        UpdateAppointmentStatusRequestDto update = new UpdateAppointmentStatusRequestDto(AppointmentStatus.COMPLETED);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                appointmentService.updateAppointmentStatus(booked.getAppointmentId(), patientUser.getUserId(), update));
        assertTrue(ex.getMessage().contains("Only the assigned doctor"));
    }

    @Test
    @DisplayName("20. Patient cannot mark appointment NO_SHOW")
    void test20_PatientCannotMarkAppointmentNoShow() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Check");
        AppointmentResponseDto booked = appointmentService.createAppointment(patientUser.getUserId(), req);

        UpdateAppointmentStatusRequestDto update = new UpdateAppointmentStatusRequestDto(AppointmentStatus.NO_SHOW);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                appointmentService.updateAppointmentStatus(booked.getAppointmentId(), patientUser.getUserId(), update));
        assertTrue(ex.getMessage().contains("Only the assigned doctor"));
    }

    @Test
    @DisplayName("21. Unrelated patient cannot cancel")
    void test21_UnrelatedPatientCannotCancel() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Check");
        AppointmentResponseDto booked = appointmentService.createAppointment(patientUser.getUserId(), req);

        User patient2 = createUser("unrelated_pat", patientRole, AccountStatus.ACTIVE);
        createPatient(patient2);

        UpdateAppointmentStatusRequestDto update = new UpdateAppointmentStatusRequestDto(AppointmentStatus.CANCELLED);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                appointmentService.updateAppointmentStatus(booked.getAppointmentId(), patient2.getUserId(), update));
        assertTrue(ex.getMessage().contains("not authorized to modify this appointment"));
    }

    @Test
    @DisplayName("22. Unrelated doctor cannot update")
    void test22_UnrelatedDoctorCannotUpdate() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Check");
        AppointmentResponseDto booked = appointmentService.createAppointment(patientUser.getUserId(), req);

        User doctor2 = createUser("unrelated_doc", doctorRole, AccountStatus.ACTIVE);
        createDoctor(doctor2, VerificationStatus.ACTIVE);

        UpdateAppointmentStatusRequestDto update = new UpdateAppointmentStatusRequestDto(AppointmentStatus.COMPLETED);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                appointmentService.updateAppointmentStatus(booked.getAppointmentId(), doctor2.getUserId(), update));
        assertTrue(ex.getMessage().contains("not authorized to modify this appointment"));
    }

    @Test
    @DisplayName("23. Terminal appointment status cannot be changed")
    void test23_TerminalAppointmentStatusCannotBeChanged() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Check");
        AppointmentResponseDto booked = appointmentService.createAppointment(patientUser.getUserId(), req);

        // Transition to COMPLETED
        UpdateAppointmentStatusRequestDto completeReq = new UpdateAppointmentStatusRequestDto(AppointmentStatus.COMPLETED);
        appointmentService.updateAppointmentStatus(booked.getAppointmentId(), doctorUser.getUserId(), completeReq);

        // Attempt second transition on terminal appointment
        UpdateAppointmentStatusRequestDto cancelReq = new UpdateAppointmentStatusRequestDto(AppointmentStatus.CANCELLED);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointmentService.updateAppointmentStatus(booked.getAppointmentId(), doctorUser.getUserId(), cancelReq));
        assertTrue(ex.getMessage().contains("Cannot update an appointment in terminal status"));
    }

    @Test
    @DisplayName("24. Invalid status transition is rejected")
    void test24_InvalidStatusTransitionIsRejected() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Check");
        AppointmentResponseDto booked = appointmentService.createAppointment(patientUser.getUserId(), req);

        UpdateAppointmentStatusRequestDto invalidTransition = new UpdateAppointmentStatusRequestDto(AppointmentStatus.SCHEDULED);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                appointmentService.updateAppointmentStatus(booked.getAppointmentId(), doctorUser.getUserId(), invalidTransition));
        assertTrue(ex.getMessage().contains("Invalid status transition"));
    }

    @Test
    @DisplayName("25. Successful status update creates an audit record")
    void test25_SuccessfulStatusUpdateCreatesAuditRecord() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(1), AppointmentType.IN_PERSON, "Check");
        AppointmentResponseDto booked = appointmentService.createAppointment(patientUser.getUserId(), req);

        long auditCountBefore = auditLogRepository.count();

        UpdateAppointmentStatusRequestDto update = new UpdateAppointmentStatusRequestDto(
                AppointmentStatus.CANCELLED, "Patient conflict");
        appointmentService.updateAppointmentStatus(booked.getAppointmentId(), patientUser.getUserId(), update);

        assertEquals(auditCountBefore + 1, auditLogRepository.count());
        List<AuditLog> logs = auditLogRepository.findAll();
        AuditLog latestLog = logs.get(logs.size() - 1);

        assertEquals(patientUser.getUserId(), latestLog.getActorUserId());
        assertEquals(patientUser.getUserId(), latestLog.getTargetPatientId());
        assertEquals("APPOINTMENT_STATUS_UPDATE", latestLog.getAction());
        assertTrue(latestLog.getResourceAccessed().contains("CANCELLED"));
        assertTrue(latestLog.getResourceAccessed().contains("Patient conflict"));
    }

    @Test
    @DisplayName("26. Audit failure rolls back status update")
    void test26_AuditFailureRollsBackStatusUpdate() {
        AppointmentRepository mockAptRepo = Mockito.mock(AppointmentRepository.class);
        PatientProfileRepository mockPatRepo = Mockito.mock(PatientProfileRepository.class);
        DoctorProfileRepository mockDocRepo = Mockito.mock(DoctorProfileRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        AuditLogRepository mockAuditRepo = Mockito.mock(AuditLogRepository.class);

        AppointmentService mockService = new AppointmentService(
                mockAptRepo, mockPatRepo, mockDocRepo, mockUserRepo, mockAuditRepo
        );

        Appointment mockApt = new Appointment();
        mockApt.setAppointmentId("apt-999");
        mockApt.setPatient(patientProfile);
        mockApt.setDoctor(doctorProfile);
        mockApt.setStatus(AppointmentStatus.SCHEDULED);

        Mockito.when(mockAptRepo.findById("apt-999")).thenReturn(Optional.of(mockApt));
        Mockito.when(mockAptRepo.save(any(Appointment.class))).thenReturn(mockApt);
        Mockito.doThrow(new RuntimeException("Simulated audit disk failure"))
                .when(mockAuditRepo).save(any(AuditLog.class));

        UpdateAppointmentStatusRequestDto updateReq = new UpdateAppointmentStatusRequestDto(AppointmentStatus.COMPLETED);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                mockService.updateAppointmentStatus("apt-999", doctorUser.getUserId(), updateReq));
        assertEquals("Simulated audit disk failure", ex.getMessage());

        // Verify @Transactional is present on updateAppointmentStatus
        try {
            Method method = AppointmentService.class.getMethod(
                    "updateAppointmentStatus", String.class, String.class, UpdateAppointmentStatusRequestDto.class);
            assertTrue(method.isAnnotationPresent(Transactional.class),
                    "updateAppointmentStatus must be annotated with @Transactional to ensure rollback on audit failure");
        } catch (NoSuchMethodException e) {
            fail("updateAppointmentStatus method not found");
        }
    }

    @Test
    @DisplayName("27. Response mapping does not expose JPA entities directly")
    void test27_ResponseMappingDoesNotExposeJpaEntity() {
        CreateAppointmentRequestDto req = new CreateAppointmentRequestDto(
                doctorProfile.getDoctorId(), LocalDateTime.now().plusDays(2), AppointmentType.TELECONSULT, "Video consult");
        AppointmentResponseDto response = appointmentService.createAppointment(patientUser.getUserId(), req);

        assertTrue(response instanceof AppointmentResponseDto);
        assertEquals(doctorProfile.getDoctorId(), response.getDoctorId());
        assertEquals(doctorUser.getFullName(), response.getDoctorName());
        assertEquals(patientProfile.getPatientId(), response.getPatientId());
        assertEquals(patientUser.getFullName(), response.getPatientName());
        assertEquals("TELECONSULT", response.getAppointmentType());
        assertEquals("SCHEDULED", response.getStatus());
    }
}
