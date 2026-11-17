package com.mediverse.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.Appointment;
import com.mediverse.entity.AppointmentStatus;
import com.mediverse.entity.AppointmentType;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class AppointmentRepositoryTest {

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

    private User patientUser;
    private PatientProfile patientProfile;

    private User doctorUser;
    private DoctorProfile doctorProfile;

    @BeforeEach
    void setUp() {
        Role patientRole = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient user account")));
        Role doctorRole = roleRepository.findByRoleName("DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("DOCTOR", "Doctor user account")));

        // Create Patient
        patientUser = new User();
        patientUser.setEmail("apt_patient_" + UUID.randomUUID() + "@mediverse.com");
        patientUser.setPasswordHash("hashed_password");
        patientUser.setFullName("Test Patient");
        patientUser.setPhoneNumber("987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7));
        patientUser.setRole(patientRole);
        patientUser.setAccountStatus(AccountStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);

        patientProfile = new PatientProfile();
        patientProfile.setUser(patientUser);
        patientProfile.setHealthId("MV-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        patientProfile.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patientProfile = patientProfileRepository.save(patientProfile);

        // Create Doctor
        doctorUser = new User();
        doctorUser.setEmail("apt_doctor_" + UUID.randomUUID() + "@mediverse.com");
        doctorUser.setPasswordHash("hashed_password");
        doctorUser.setFullName("Dr. Test Doctor");
        doctorUser.setPhoneNumber("987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7));
        doctorUser.setRole(doctorRole);
        doctorUser.setAccountStatus(AccountStatus.ACTIVE);
        doctorUser = userRepository.save(doctorUser);

        doctorProfile = new DoctorProfile();
        doctorProfile.setUser(doctorUser);
        doctorProfile.setLicenseNumber("DOC-LIC-" + UUID.randomUUID().toString().substring(0, 8));
        doctorProfile.setSpecialization("Cardiology");
        doctorProfile.setVerificationStatus(VerificationStatus.ACTIVE);
        doctorProfile = doctorProfileRepository.save(doctorProfile);
    }

    private Appointment createAppointment(PatientProfile patient, DoctorProfile doctor, LocalDateTime date,
                                          AppointmentType type, AppointmentStatus status, String reason) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentType(type);
        appointment.setStatus(status);
        appointment.setReason(reason);
        return appointmentRepository.save(appointment);
    }

    @Test
    @DisplayName("1. Patient appointment query ordered by appointment date")
    void testPatientAppointmentQuery() {
        LocalDateTime date1 = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime date2 = LocalDateTime.now().plusDays(2).withNano(0);

        createAppointment(patientProfile, doctorProfile, date1, AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Visit 1");
        createAppointment(patientProfile, doctorProfile, date2, AppointmentType.TELECONSULT, AppointmentStatus.SCHEDULED, "Visit 2");

        List<Appointment> byPatientIdDesc = appointmentRepository.findAllByPatient_PatientIdOrderByAppointmentDateDesc(patientProfile.getPatientId());
        assertEquals(2, byPatientIdDesc.size());
        assertEquals(date2, byPatientIdDesc.get(0).getAppointmentDate());
        assertEquals(date1, byPatientIdDesc.get(1).getAppointmentDate());

        List<Appointment> byPatientIdAsc = appointmentRepository.findAllByPatient_PatientIdOrderByAppointmentDateAsc(patientProfile.getPatientId());
        assertEquals(2, byPatientIdAsc.size());
        assertEquals(date1, byPatientIdAsc.get(0).getAppointmentDate());
        assertEquals(date2, byPatientIdAsc.get(1).getAppointmentDate());

        List<Appointment> byUserIdDesc = appointmentRepository.findAllByPatient_User_UserIdOrderByAppointmentDateDesc(patientUser.getUserId());
        assertEquals(2, byUserIdDesc.size());
        assertEquals(date2, byUserIdDesc.get(0).getAppointmentDate());
    }

    @Test
    @DisplayName("2. Doctor appointment query ordered by appointment date")
    void testDoctorAppointmentQuery() {
        LocalDateTime date1 = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime date2 = LocalDateTime.now().plusDays(3).withNano(0);

        createAppointment(patientProfile, doctorProfile, date1, AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Doctor Visit 1");
        createAppointment(patientProfile, doctorProfile, date2, AppointmentType.TELECONSULT, AppointmentStatus.SCHEDULED, "Doctor Visit 2");

        List<Appointment> byDoctorIdDesc = appointmentRepository.findAllByDoctor_DoctorIdOrderByAppointmentDateDesc(doctorProfile.getDoctorId());
        assertEquals(2, byDoctorIdDesc.size());
        assertEquals(date2, byDoctorIdDesc.get(0).getAppointmentDate());
        assertEquals(date1, byDoctorIdDesc.get(1).getAppointmentDate());

        List<Appointment> byUserIdDesc = appointmentRepository.findAllByDoctor_User_UserIdOrderByAppointmentDateDesc(doctorUser.getUserId());
        assertEquals(2, byUserIdDesc.size());
        assertEquals(date2, byUserIdDesc.get(0).getAppointmentDate());
    }

    @Test
    @DisplayName("3. Patient and doctor status filtering")
    void testPatientAndDoctorStatusFiltering() {
        LocalDateTime date1 = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime date2 = LocalDateTime.now().plusDays(2).withNano(0);

        createAppointment(patientProfile, doctorProfile, date1, AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Scheduled Visit");
        createAppointment(patientProfile, doctorProfile, date2, AppointmentType.IN_PERSON, AppointmentStatus.COMPLETED, "Completed Visit");

        List<Appointment> scheduledPatient = appointmentRepository.findAllByPatient_PatientIdAndStatusOrderByAppointmentDateDesc(
                patientProfile.getPatientId(), AppointmentStatus.SCHEDULED);
        assertEquals(1, scheduledPatient.size());
        assertEquals(AppointmentStatus.SCHEDULED, scheduledPatient.get(0).getStatus());

        List<Appointment> completedDoctor = appointmentRepository.findAllByDoctor_DoctorIdAndStatusOrderByAppointmentDateDesc(
                doctorProfile.getDoctorId(), AppointmentStatus.COMPLETED);
        assertEquals(1, completedDoctor.size());
        assertEquals(AppointmentStatus.COMPLETED, completedDoctor.get(0).getStatus());

        List<Appointment> scheduledUserDoctor = appointmentRepository.findAllByDoctor_User_UserIdAndStatusOrderByAppointmentDateDesc(
                doctorUser.getUserId(), AppointmentStatus.SCHEDULED);
        assertEquals(1, scheduledUserDoctor.size());
        assertEquals(AppointmentStatus.SCHEDULED, scheduledUserDoctor.get(0).getStatus());
    }

    @Test
    @DisplayName("4. Scheduled doctor slot conflict detection")
    void testScheduledDoctorSlotConflictDetection() {
        LocalDateTime slot = LocalDateTime.now().plusDays(5).withNano(0);

        assertFalse(appointmentRepository.existsByDoctor_DoctorIdAndAppointmentDateAndStatus(
                doctorProfile.getDoctorId(), slot, AppointmentStatus.SCHEDULED));

        createAppointment(patientProfile, doctorProfile, slot, AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Booked Slot");

        assertTrue(appointmentRepository.existsByDoctor_DoctorIdAndAppointmentDateAndStatus(
                doctorProfile.getDoctorId(), slot, AppointmentStatus.SCHEDULED));
        assertTrue(appointmentRepository.existsByDoctor_User_UserIdAndAppointmentDateAndStatus(
                doctorUser.getUserId(), slot, AppointmentStatus.SCHEDULED));

        // Different time slot should not conflict
        assertFalse(appointmentRepository.existsByDoctor_DoctorIdAndAppointmentDateAndStatus(
                doctorProfile.getDoctorId(), slot.plusHours(1), AppointmentStatus.SCHEDULED));

        // Different doctor should not conflict
        assertFalse(appointmentRepository.existsByDoctor_DoctorIdAndAppointmentDateAndStatus(
                "non-existent-doctor-id", slot, AppointmentStatus.SCHEDULED));
    }

    @Test
    @DisplayName("5. Scheduled patient slot conflict detection")
    void testScheduledPatientSlotConflictDetection() {
        LocalDateTime slot = LocalDateTime.now().plusDays(6).withNano(0);

        assertFalse(appointmentRepository.existsByPatient_PatientIdAndAppointmentDateAndStatus(
                patientProfile.getPatientId(), slot, AppointmentStatus.SCHEDULED));

        createAppointment(patientProfile, doctorProfile, slot, AppointmentType.IN_PERSON, AppointmentStatus.SCHEDULED, "Patient Slot");

        assertTrue(appointmentRepository.existsByPatient_PatientIdAndAppointmentDateAndStatus(
                patientProfile.getPatientId(), slot, AppointmentStatus.SCHEDULED));
        assertTrue(appointmentRepository.existsByPatient_User_UserIdAndAppointmentDateAndStatus(
                patientUser.getUserId(), slot, AppointmentStatus.SCHEDULED));

        // Different time slot should not conflict
        assertFalse(appointmentRepository.existsByPatient_PatientIdAndAppointmentDateAndStatus(
                patientProfile.getPatientId(), slot.plusHours(2), AppointmentStatus.SCHEDULED));
    }

    @Test
    @DisplayName("6. Cancelled appointments do not count as conflicts")
    void testCancelledAppointmentsDoNotCountAsConflicts() {
        LocalDateTime slot = LocalDateTime.now().plusDays(7).withNano(0);

        createAppointment(patientProfile, doctorProfile, slot, AppointmentType.IN_PERSON, AppointmentStatus.CANCELLED, "Cancelled Booking");

        assertFalse(appointmentRepository.existsByDoctor_DoctorIdAndAppointmentDateAndStatus(
                doctorProfile.getDoctorId(), slot, AppointmentStatus.SCHEDULED));
        assertFalse(appointmentRepository.existsByPatient_PatientIdAndAppointmentDateAndStatus(
                patientProfile.getPatientId(), slot, AppointmentStatus.SCHEDULED));
    }

    @Test
    @DisplayName("7. Completed appointments do not count as conflicts")
    void testCompletedAppointmentsDoNotCountAsConflicts() {
        LocalDateTime slot = LocalDateTime.now().plusDays(8).withNano(0);

        createAppointment(patientProfile, doctorProfile, slot, AppointmentType.IN_PERSON, AppointmentStatus.COMPLETED, "Past Completed");

        assertFalse(appointmentRepository.existsByDoctor_DoctorIdAndAppointmentDateAndStatus(
                doctorProfile.getDoctorId(), slot, AppointmentStatus.SCHEDULED));
        assertFalse(appointmentRepository.existsByPatient_PatientIdAndAppointmentDateAndStatus(
                patientProfile.getPatientId(), slot, AppointmentStatus.SCHEDULED));
    }

    @Test
    @DisplayName("8. User-scoped appointment lookup")
    void testUserScopedAppointmentLookup() {
        LocalDateTime slot = LocalDateTime.now().plusDays(4).withNano(0);
        Appointment appointment = createAppointment(patientProfile, doctorProfile, slot,
                AppointmentType.TELECONSULT, AppointmentStatus.SCHEDULED, "Consultation Note");

        assertNotNull(appointment.getAppointmentId());

        // Patient user scoped lookup
        Optional<Appointment> patientLookup = appointmentRepository.findByAppointmentIdAndPatient_User_UserId(
                appointment.getAppointmentId(), patientUser.getUserId());
        assertTrue(patientLookup.isPresent());
        assertEquals(appointment.getAppointmentId(), patientLookup.get().getAppointmentId());

        Optional<Appointment> wrongPatientLookup = appointmentRepository.findByAppointmentIdAndPatient_User_UserId(
                appointment.getAppointmentId(), "wrong-patient-user-id");
        assertFalse(wrongPatientLookup.isPresent());

        // Doctor user scoped lookup
        Optional<Appointment> doctorLookup = appointmentRepository.findByAppointmentIdAndDoctor_User_UserId(
                appointment.getAppointmentId(), doctorUser.getUserId());
        assertTrue(doctorLookup.isPresent());
        assertEquals(appointment.getAppointmentId(), doctorLookup.get().getAppointmentId());

        Optional<Appointment> wrongDoctorLookup = appointmentRepository.findByAppointmentIdAndDoctor_User_UserId(
                appointment.getAppointmentId(), "wrong-doctor-user-id");
        assertFalse(wrongDoctorLookup.isPresent());

        // Entity ID lookups
        assertTrue(appointmentRepository.findByAppointmentIdAndPatient_PatientId(
                appointment.getAppointmentId(), patientProfile.getPatientId()).isPresent());
        assertTrue(appointmentRepository.findByAppointmentIdAndDoctor_DoctorId(
                appointment.getAppointmentId(), doctorProfile.getDoctorId()).isPresent());
    }
}
