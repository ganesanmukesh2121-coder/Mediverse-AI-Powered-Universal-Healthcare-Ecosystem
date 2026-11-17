package com.mediverse.service;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.CreateConsultationRequestDto;
import com.mediverse.dto.CreatePrescriptionRequestDto;
import com.mediverse.dto.EmergencyProfileResponseDto;
import com.mediverse.dto.MedicalRecordResponseDto;
import com.mediverse.dto.PatientConsentRequestDto;
import com.mediverse.dto.PatientConsentResponseDto;
import com.mediverse.dto.PatientTimelineResponseDto;
import com.mediverse.dto.PrescriptionItemRequestDto;
import com.mediverse.dto.PrescriptionResponseDto;
import com.mediverse.dto.UpdateEmergencyProfileRequestDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.Appointment;
import com.mediverse.entity.AppointmentStatus;
import com.mediverse.entity.AppointmentType;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.ConsentStatus;
import com.mediverse.entity.DataCategory;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.EmergencyProfile;
import com.mediverse.entity.EncounterType;
import com.mediverse.entity.LabOrder;
import com.mediverse.entity.LaboratoryProfile;
import com.mediverse.entity.MedicalReport;
import com.mediverse.entity.OrderStatus;
import com.mediverse.entity.PatientConsent;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.ReportType;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.DoctorProfileRepository;
import com.mediverse.repository.EmergencyProfileRepository;
import com.mediverse.repository.LabOrderRepository;
import com.mediverse.repository.LaboratoryProfileRepository;
import com.mediverse.repository.MedicalRecordRepository;
import com.mediverse.repository.MedicalReportRepository;
import com.mediverse.repository.PatientConsentRepository;
import com.mediverse.repository.PatientProfileRepository;
import com.mediverse.repository.PrescriptionRepository;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class ClinicalServiceIntegrationTest {

    @Autowired
    private PatientConsentService patientConsentService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private EmergencyAccessService emergencyAccessService;

    @Autowired
    private PatientTimelineService patientTimelineService;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private LaboratoryProfileRepository laboratoryProfileRepository;

    @Autowired
    private EmergencyProfileRepository emergencyProfileRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private LabOrderRepository labOrderRepository;

    @Autowired
    private MedicalReportRepository medicalReportRepository;

    @Autowired
    private PatientConsentRepository patientConsentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private User patient1User;
    private PatientProfile patient1Profile;

    private User patient2User;
    private PatientProfile patient2Profile;

    private User doctor1User;
    private DoctorProfile doctor1Profile;

    private User doctor2User;
    private DoctorProfile doctor2Profile;

    private User emergencyUser;
    private User labUser;
    private LaboratoryProfile labProfile;
    private User adminUser;

    @BeforeEach
    void setUp() {
        Role patientRole = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));
        Role doctorRole = roleRepository.findByRoleName("DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("DOCTOR", "Doctor role")));
        Role emergencyRole = roleRepository.findByRoleName("EMERGENCY")
                .orElseGet(() -> roleRepository.save(new Role("EMERGENCY", "Emergency role")));
        Role labRole = roleRepository.findByRoleName("LAB")
                .orElseGet(() -> roleRepository.save(new Role("LAB", "Lab role")));
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Admin role")));

        patient1User = createUser("pat1", patientRole, AccountStatus.ACTIVE);
        patient1Profile = createPatient(patient1User, "O+", "9876543210");

        patient2User = createUser("pat2", patientRole, AccountStatus.ACTIVE);
        patient2Profile = createPatient(patient2User, "A+", "9876543211");

        doctor1User = createUser("doc1", doctorRole, AccountStatus.ACTIVE);
        doctor1Profile = createDoctor(doctor1User, VerificationStatus.ACTIVE, "Cardiology");

        doctor2User = createUser("doc2", doctorRole, AccountStatus.ACTIVE);
        doctor2Profile = createDoctor(doctor2User, VerificationStatus.ACTIVE, "Neurology");

        emergencyUser = createUser("emg1", emergencyRole, AccountStatus.ACTIVE);

        labUser = createUser("lab1", labRole, AccountStatus.ACTIVE);
        labProfile = createLab(labUser, VerificationStatus.ACTIVE);

        adminUser = createUser("adm1", adminRole, AccountStatus.ACTIVE);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(User user, String... permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            String roleName = user.getRole().getRoleName();
            authorities.add(new SimpleGrantedAuthority(roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName));
        }
        for (String perm : permissions) {
            authorities.add(new SimpleGrantedAuthority(perm));
        }
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getUserId(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Appointment createAppointment(PatientProfile patient, DoctorProfile doctor, AppointmentStatus status) {
        Appointment apt = new Appointment();
        apt.setPatient(patient);
        apt.setDoctor(doctor);
        apt.setAppointmentDate(LocalDateTime.now().plusDays(1));
        apt.setAppointmentType(AppointmentType.IN_PERSON);
        apt.setStatus(status);
        apt.setReason("Clinical examination appointment");
        return appointmentRepository.save(apt);
    }

    private User createUser(String prefix, Role role, AccountStatus status) {
        User user = new User();
        user.setEmail(prefix + "_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash("hashed_secure_pass");
        user.setFullName("User " + prefix);
        user.setPhoneNumber("987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7));
        user.setRole(role);
        user.setAccountStatus(status);
        return userRepository.save(user);
    }

    private PatientProfile createPatient(User user, String bloodGroup, String contactPhone) {
        PatientProfile profile = new PatientProfile();
        profile.setUser(user);
        profile.setHealthId("MV-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        profile.setDateOfBirth(LocalDate.of(1992, 4, 10));
        profile.setBloodGroup(bloodGroup);
        profile.setEmergencyContactPhone(contactPhone);
        return patientProfileRepository.save(profile);
    }

    private DoctorProfile createDoctor(User user, VerificationStatus status, String spec) {
        DoctorProfile doc = new DoctorProfile();
        doc.setUser(user);
        doc.setLicenseNumber("DOC-LIC-" + UUID.randomUUID().toString().substring(0, 8));
        doc.setSpecialization(spec);
        doc.setHospitalAffiliation("Apex Healthcare");
        doc.setVerificationStatus(status);
        return doctorProfileRepository.save(doc);
    }

    private LaboratoryProfile createLab(User user, VerificationStatus status) {
        LaboratoryProfile lab = new LaboratoryProfile();
        lab.setUser(user);
        lab.setLabName("Apex Central Lab");
        lab.setAccreditationNumber("LAB-ACC-" + UUID.randomUUID().toString().substring(0, 8));
        lab.setVerificationStatus(status);
        return laboratoryProfileRepository.save(lab);
    }

    // ============================================================
    // 1. PATIENT CONSENT SERVICE TESTS
    // ============================================================

    @Test
    @DisplayName("Consent: Patient can grant provider-specific consent and audit record is persisted")
    void testConsent01_PatientGrantsConsentToDoctor_Success() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        long initAuditCount = auditLogRepository.count();

        PatientConsentRequestDto request = new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.FULL_TIMELINE, LocalDateTime.now().plusDays(14)
        );

        PatientConsentResponseDto response = patientConsentService.grantConsent(patient1User.getUserId(), request);

        assertNotNull(response.getConsentId());
        assertEquals(patient1Profile.getPatientId(), response.getPatientId());
        assertEquals(doctor1User.getUserId(), response.getGrantedToUserId());
        assertEquals("FULL_TIMELINE", response.getDataCategory());
        assertEquals("ACTIVE", response.getStatus());

        // Verify audit log
        assertEquals(initAuditCount + 1, auditLogRepository.count());
        List<AuditLog> logs = auditLogRepository.findAll();
        AuditLog lastLog = logs.get(logs.size() - 1);
        assertEquals(patient1User.getUserId(), lastLog.getActorUserId());
        assertEquals(patient1User.getUserId(), lastLog.getTargetPatientId());
        assertEquals("CONSENT_GRANT", lastLog.getAction());
        assertTrue(lastLog.getResourceAccessed().contains(response.getConsentId()));
    }

    @Test
    @DisplayName("Consent: Patient cannot grant consent to non-doctor or to themselves")
    void testConsent02_PatientCannotGrantConsentToNonDoctor_Rejected() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");

        // To another patient
        PatientConsentRequestDto toPatient = new PatientConsentRequestDto(
                patient2User.getUserId(), DataCategory.FULL_TIMELINE, LocalDateTime.now().plusDays(10)
        );
        assertThrows(IllegalArgumentException.class, () ->
                patientConsentService.grantConsent(patient1User.getUserId(), toPatient));

        // To self
        PatientConsentRequestDto toSelf = new PatientConsentRequestDto(
                patient1User.getUserId(), DataCategory.FULL_TIMELINE, LocalDateTime.now().plusDays(10)
        );
        assertThrows(IllegalArgumentException.class, () ->
                patientConsentService.grantConsent(patient1User.getUserId(), toSelf));
    }

    @Test
    @DisplayName("Consent: Past expiry date is rejected")
    void testConsent03_PatientCannotGrantConsentWithPastDate_Rejected() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");

        PatientConsentRequestDto past = new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.FULL_TIMELINE, LocalDateTime.now().minusMinutes(5)
        );
        assertThrows(IllegalArgumentException.class, () ->
                patientConsentService.grantConsent(patient1User.getUserId(), past));
    }

    @Test
    @DisplayName("Consent: Patient can list own consents and revoke own consent")
    void testConsent04_PatientCanListAndRevokeConsent() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");

        PatientConsentRequestDto request = new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        );
        PatientConsentResponseDto granted = patientConsentService.grantConsent(patient1User.getUserId(), request);

        List<PatientConsentResponseDto> myConsents = patientConsentService.getMyConsents(patient1User.getUserId());
        assertEquals(1, myConsents.size());
        assertEquals(granted.getConsentId(), myConsents.get(0).getConsentId());

        // Revoke
        PatientConsentResponseDto revoked = patientConsentService.revokeConsent(patient1User.getUserId(), granted.getConsentId());
        assertEquals("REVOKED", revoked.getStatus());
        assertNotNull(revoked.getRevokedAt());

        // Patient 2 attempting to revoke Patient 1's consent is forbidden
        setSecurityContext(patient2User, "PATIENT_MANAGE_CONSENT");
        assertThrows(AccessDeniedException.class, () ->
                patientConsentService.revokeConsent(patient2User.getUserId(), granted.getConsentId()));
    }

    // ============================================================
    // 2. MEDICAL RECORD SERVICE TESTS
    // ============================================================

    @Test
    @DisplayName("MedicalRecord: Doctor creates consultation under active consent with audit log")
    void testRecord01_DoctorCanCreateConsultation_WithActiveConsent_Success() {
        // 1. Grant consent
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        // 2. Create Appointment
        Appointment apt = createAppointment(patient1Profile, doctor1Profile, AppointmentStatus.SCHEDULED);

        long initAuditCount = auditLogRepository.count();

        // 3. Doctor creates consultation
        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION", "DOCTOR_VIEW_AUTHORIZED_RECORD");
        CreateConsultationRequestDto request = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Acute migraine headache", "Prescribed triptans and rest", apt.getAppointmentId()
        );

        MedicalRecordResponseDto record = medicalRecordService.createConsultation(doctor1User.getUserId(), request);

        assertNotNull(record.getRecordId());
        assertEquals(patient1Profile.getPatientId(), record.getPatientId());
        assertEquals(doctor1Profile.getDoctorId(), record.getDoctorId());
        assertEquals("CONSULTATION", record.getEncounterType());
        assertEquals("Acute migraine headache", record.getChiefComplaint());
        assertEquals("Prescribed triptans and rest", record.getDiagnosisNotes());

        // Verify audit log
        assertEquals(initAuditCount + 1, auditLogRepository.count());
        List<AuditLog> logs = auditLogRepository.findAll();
        AuditLog lastLog = logs.get(logs.size() - 1);
        assertEquals(doctor1User.getUserId(), lastLog.getActorUserId());
        assertEquals(patient1User.getUserId(), lastLog.getTargetPatientId());
        assertEquals("CONSULTATION_CREATE", lastLog.getAction());
        assertTrue(lastLog.getResourceAccessed().contains("CareContext: APPOINTMENT"));
    }

    @Test
    @DisplayName("MedicalRecord: Doctor cannot create consultation without active patient consent")
    void testRecord02_DoctorCannotCreateConsultation_WithoutActiveConsent_Forbidden() {
        setSecurityContext(doctor2User, "DOCTOR_CREATE_CONSULTATION");
        CreateConsultationRequestDto request = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Routine checkup", "Normal findings", true, "Documented walk-in"
        );

        // Doctor 2 has NO consent from Patient 1
        assertThrows(AccessDeniedException.class, () ->
                medicalRecordService.createConsultation(doctor2User.getUserId(), request));
    }

    @Test
    @DisplayName("MedicalRecord: Patient can view own records; Doctor can view records under active consent and appointment")
    void testRecord03_PatientSelfViewAndDoctorConsentView() {
        // Grant consent to Doctor 1
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT", "PATIENT_VIEW_OWN_PROFILE");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.FULL_TIMELINE, LocalDateTime.now().plusDays(5)
        ));

        // Create appointment and consultation
        Appointment apt = createAppointment(patient1Profile, doctor1Profile, AppointmentStatus.SCHEDULED);
        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION", "DOCTOR_VIEW_AUTHORIZED_RECORD");
        CreateConsultationRequestDto request = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Follow-up", "Stable condition", apt.getAppointmentId()
        );
        medicalRecordService.createConsultation(doctor1User.getUserId(), request);

        // 1. Patient 1 self-view
        setSecurityContext(patient1User, "PATIENT_VIEW_OWN_PROFILE");
        List<MedicalRecordResponseDto> p1Records = medicalRecordService.getPatientMedicalRecords(patient1User.getUserId());
        assertEquals(1, p1Records.size());

        // 2. Patient 2 sees zero records
        setSecurityContext(patient2User, "PATIENT_VIEW_OWN_PROFILE");
        List<MedicalRecordResponseDto> p2Records = medicalRecordService.getPatientMedicalRecords(patient2User.getUserId());
        assertTrue(p2Records.isEmpty());

        // 3. Doctor 1 view under consent and care context
        setSecurityContext(doctor1User, "DOCTOR_VIEW_AUTHORIZED_RECORD");
        long preReadAudit = auditLogRepository.count();
        List<MedicalRecordResponseDto> docView = medicalRecordService.getMedicalRecordsForDoctor(
                doctor1User.getUserId(), patient1Profile.getPatientId(), apt.getAppointmentId(), null);
        assertEquals(1, docView.size());
        assertEquals(preReadAudit + 1, auditLogRepository.count()); // Read audit logged

        // 4. Doctor 2 has NO consent -> Forbidden
        setSecurityContext(doctor2User, "DOCTOR_VIEW_AUTHORIZED_RECORD");
        assertThrows(AccessDeniedException.class, () ->
                medicalRecordService.getMedicalRecordsForDoctor(doctor2User.getUserId(), patient1Profile.getPatientId()));
    }

    @Test
    @DisplayName("MedicalRecord: Service enforces append-only pattern with zero update or delete methods")
    void testRecord04_ServiceHasNoUpdateOrDeleteMethods() {
        List<String> methodNames = Arrays.stream(MedicalRecordService.class.getDeclaredMethods())
                .map(Method::getName)
                .toList();

        assertFalse(methodNames.stream().anyMatch(m -> m.startsWith("update") || m.startsWith("delete") || m.startsWith("remove")));
    }

    // ============================================================
    // 3. PRESCRIPTION SERVICE TESTS
    // ============================================================

    @Test
    @DisplayName("Prescription: Doctor issues prescription with items under active consent and appointment")
    void testPrescription01_DoctorCanIssuePrescription_WithConsent_Success() {
        // Grant consent
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(10)
        ));

        // Create appointment
        Appointment apt = createAppointment(patient1Profile, doctor1Profile, AppointmentStatus.SCHEDULED);

        // Issue prescription
        setSecurityContext(doctor1User, "DOCTOR_CREATE_PRESCRIPTION");
        PrescriptionItemRequestDto item1 = new PrescriptionItemRequestDto("Atorvastatin", "20mg", "OD", 30, "At bedtime");
        PrescriptionItemRequestDto item2 = new PrescriptionItemRequestDto("Aspirin", "75mg", "OD", 30, "After breakfast");

        CreatePrescriptionRequestDto rxRequest = new CreatePrescriptionRequestDto(
                patient1Profile.getPatientId(), null, "Maintain low salt diet", List.of(item1, item2), apt.getAppointmentId()
        );

        long initAuditCount = auditLogRepository.count();
        PrescriptionResponseDto rxResponse = prescriptionService.createPrescription(doctor1User.getUserId(), rxRequest);

        assertNotNull(rxResponse.getPrescriptionId());
        assertEquals(patient1Profile.getPatientId(), rxResponse.getPatientId());
        assertEquals(doctor1Profile.getDoctorId(), rxResponse.getDoctorId());
        assertEquals(2, rxResponse.getItems().size());

        // Audit log
        assertEquals(initAuditCount + 1, auditLogRepository.count());
        List<AuditLog> logs = auditLogRepository.findAll();
        AuditLog lastLog = logs.get(logs.size() - 1);
        assertEquals(doctor1User.getUserId(), lastLog.getActorUserId());
        assertEquals("PRESCRIPTION_CREATE", lastLog.getAction());
        assertTrue(lastLog.getResourceAccessed().contains("CareContext: APPOINTMENT"));
    }

    @Test
    @DisplayName("Prescription: Linking consultation record from another patient is rejected")
    void testPrescription02_PrescriptionLinkedToDifferentPatientRecord_Rejected() {
        // Grant consent to Patient 1 and Patient 2
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.FULL_TIMELINE, LocalDateTime.now().plusDays(5)
        ));
        setSecurityContext(patient2User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient2User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.FULL_TIMELINE, LocalDateTime.now().plusDays(5)
        ));

        // Create consultation for Patient 2
        Appointment apt2 = createAppointment(patient2Profile, doctor1Profile, AppointmentStatus.SCHEDULED);
        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION", "DOCTOR_CREATE_PRESCRIPTION");
        MedicalRecordResponseDto p2Record = medicalRecordService.createConsultation(doctor1User.getUserId(), new CreateConsultationRequestDto(
                patient2Profile.getPatientId(), EncounterType.CONSULTATION, "Patient 2 issue", "Patient 2 notes", apt2.getAppointmentId()
        ));

        // Attempt to link Patient 2's record to Patient 1's prescription
        CreatePrescriptionRequestDto invalidRx = new CreatePrescriptionRequestDto(
                patient1Profile.getPatientId(), p2Record.getRecordId(), "Instructions",
                List.of(new PrescriptionItemRequestDto("Ibuprofen", "400mg", "TDS", 3, "Pain"))
        );

        assertThrows(IllegalArgumentException.class, () ->
                prescriptionService.createPrescription(doctor1User.getUserId(), invalidRx));
    }

    @Test
    @DisplayName("Prescription: Service enforces append-only pattern with zero update or delete methods")
    void testPrescription03_ServiceHasNoUpdateOrDeleteMethods() {
        List<String> methodNames = Arrays.stream(PrescriptionService.class.getDeclaredMethods())
                .map(Method::getName)
                .toList();

        assertFalse(methodNames.stream().anyMatch(m -> m.startsWith("update") || m.startsWith("delete") || m.startsWith("remove")));
    }

    // ============================================================
    // 4. EMERGENCY ACCESS SERVICE TESTS
    // ============================================================

    @Test
    @DisplayName("Emergency: Authenticated emergency role with justification receives minimal emergency DTO")
    void testEmergency01_EmergencyAccess_SuccessAndAuditLogged() {
        setSecurityContext(emergencyUser, "EMERGENCY_VIEW_EMERGENCY_INFO");

        // Set up emergency triage data
        EmergencyProfile ep = new EmergencyProfile();
        ep.setPatientProfile(patient1Profile);
        ep.setSevereAllergies("Severe anaphylaxis to Penicillin");
        ep.setActiveCriticalMedications("Epinephrine auto-injector");
        ep.setChronicConditions("Asthma");
        ep.setEmergencyNotes("Contact family immediately");
        emergencyProfileRepository.save(ep);

        long initAuditCount = auditLogRepository.count();

        // Break-glass emergency request
        EmergencyProfileResponseDto response = emergencyAccessService.getEmergencyProfileByHealthId(
                emergencyUser.getUserId(), "ROLE_EMERGENCY", patient1Profile.getHealthId(), "Ambulance trauma triage incident #911"
        );

        assertNotNull(response);
        assertEquals(patient1Profile.getPatientId(), response.getPatientId());
        assertEquals(patient1Profile.getHealthId(), response.getHealthId());
        assertEquals("O+", response.getBloodGroup());
        assertEquals("9876543210", response.getEmergencyContactPhone());
        assertEquals("Severe anaphylaxis to Penicillin", response.getSevereAllergies());
        assertEquals("Epinephrine auto-injector", response.getActiveCriticalMedications());

        // Verify audit log
        assertEquals(initAuditCount + 1, auditLogRepository.count());
        List<AuditLog> logs = auditLogRepository.findAll();
        AuditLog lastLog = logs.get(logs.size() - 1);
        assertEquals(emergencyUser.getUserId(), lastLog.getActorUserId());
        assertEquals(patient1User.getUserId(), lastLog.getTargetPatientId());
        assertEquals("EMERGENCY_PROFILE_VIEW", lastLog.getAction());
        assertTrue(lastLog.getResourceAccessed().contains("Ambulance trauma triage incident #911"));
    }

    @Test
    @DisplayName("Emergency: Non-emergency role or missing justification is rejected")
    void testEmergency02_NonEmergencyRoleOrMissingReason_Rejected() {
        // Doctor trying emergency override
        setSecurityContext(doctor1User, "DOCTOR_VIEW_AUTHORIZED_RECORD");
        assertThrows(AccessDeniedException.class, () ->
                emergencyAccessService.getEmergencyProfileByHealthId(
                        doctor1User.getUserId(), "ROLE_DOCTOR", patient1Profile.getHealthId(), "Doctor curious"
                ));

        // Missing justification
        setSecurityContext(emergencyUser, "EMERGENCY_VIEW_EMERGENCY_INFO");
        assertThrows(IllegalArgumentException.class, () ->
                emergencyAccessService.getEmergencyProfileByHealthId(
                        emergencyUser.getUserId(), "ROLE_EMERGENCY", patient1Profile.getHealthId(), "   "
                ));
    }

    @Test
    @DisplayName("Emergency: Patient can update their own emergency profile")
    void testEmergency03_PatientCanUpdateEmergencyProfile() {
        setSecurityContext(patient1User, "PATIENT_UPDATE_OWN_PROFILE");

        UpdateEmergencyProfileRequestDto update = new UpdateEmergencyProfileRequestDto(
                "Sulfa drugs", "Albuterol inhaler", "Mild asthma", "Emergency contact: Spouse"
        );

        EmergencyProfileResponseDto response = emergencyAccessService.updatePatientEmergencyProfile(patient1User.getUserId(), update);
        assertEquals("Sulfa drugs", response.getSevereAllergies());
        assertEquals("Albuterol inhaler", response.getActiveCriticalMedications());
        assertEquals("O+", response.getBloodGroup());
    }

    // ============================================================
    // 5. PATIENT TIMELINE SERVICE TESTS
    // ============================================================

    @Test
    @DisplayName("Timeline: Patient self-access aggregates full chronological history")
    void testTimeline01_PatientSelfAccess_FullChronologicalAggregation() {
        // 1. Consultation
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.FULL_TIMELINE, LocalDateTime.now().plusDays(10)
        ));

        Appointment apt = createAppointment(patient1Profile, doctor1Profile, AppointmentStatus.SCHEDULED);

        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION", "DOCTOR_CREATE_PRESCRIPTION");
        medicalRecordService.createConsultation(doctor1User.getUserId(), new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Fever", "Viral", LocalDateTime.now().minusDays(5), apt.getAppointmentId()
        ));

        // 2. Prescription
        prescriptionService.createPrescription(doctor1User.getUserId(), new CreatePrescriptionRequestDto(
                patient1Profile.getPatientId(), null, "Meds",
                List.of(new PrescriptionItemRequestDto("Paracetamol", "500mg", "TDS", 3, "After food")),
                apt.getAppointmentId()
        ));

        // 3. Lab Order
        LabOrder order = new LabOrder();
        order.setPatient(patient1Profile);
        order.setLab(labProfile);
        order.setTestName("Complete Blood Count");
        order.setOrderStatus(OrderStatus.ORDERED);
        order.setOrderedAt(LocalDateTime.now().minusDays(3));
        labOrderRepository.save(order);

        // 4. Medical Report
        MedicalReport report = new MedicalReport();
        report.setPatient(patient1Profile);
        report.setReportTitle("CBC Diagnostic Report");
        report.setReportType(ReportType.LAB_REPORT);
        report.setCreatedAt(LocalDateTime.now().minusDays(2));
        medicalReportRepository.save(report);

        // Retrieve Patient Timeline
        setSecurityContext(patient1User, "PATIENT_VIEW_OWN_PROFILE");
        PatientTimelineResponseDto timeline = patientTimelineService.getTimelineForPatient(patient1User.getUserId());

        assertEquals(patient1Profile.getPatientId(), timeline.getPatientId());
        assertEquals("FULL_TIMELINE", timeline.getAuthorizedCategory());
        assertEquals(4, timeline.getEvents().size());

        // Verify chronological order (descending)
        for (int i = 0; i < timeline.getEvents().size() - 1; i++) {
            LocalDateTime cur = timeline.getEvents().get(i).getEventDate();
            LocalDateTime next = timeline.getEvents().get(i + 1).getEventDate();
            assertTrue(cur.isAfter(next) || cur.isEqual(next));
        }
    }

    @Test
    @DisplayName("Timeline: Provider timeline strictly enforces CONSULT_ONLY and LAB_ONLY category scopes")
    void testTimeline02_ProviderTimeline_CategoryScopingEnforced() {
        // Seed clinical data for Patient 1
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.FULL_TIMELINE, LocalDateTime.now().plusDays(10)
        ));

        Appointment apt1 = createAppointment(patient1Profile, doctor1Profile, AppointmentStatus.SCHEDULED);
        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION");
        medicalRecordService.createConsultation(doctor1User.getUserId(), new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Cough", "Bronchitis", LocalDateTime.now().minusDays(4), apt1.getAppointmentId()
        ));

        LabOrder order = new LabOrder();
        order.setPatient(patient1Profile);
        order.setLab(labProfile);
        order.setTestName("Chest X-Ray");
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setOrderedAt(LocalDateTime.now().minusDays(2));
        labOrderRepository.save(order);

        // Scenario A: Doctor 2 is granted CONSULT_ONLY consent
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor2User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(5)
        ));

        Appointment apt2 = createAppointment(patient1Profile, doctor2Profile, AppointmentStatus.SCHEDULED);
        setSecurityContext(doctor2User, "DOCTOR_VIEW_AUTHORIZED_RECORD");

        PatientTimelineResponseDto consultOnlyTimeline = patientTimelineService.getTimelineForProvider(
                doctor2User.getUserId(), patient1Profile.getPatientId(), apt2.getAppointmentId(), null
        );

        assertEquals("CONSULT_ONLY", consultOnlyTimeline.getAuthorizedCategory());
        assertEquals(1, consultOnlyTimeline.getEvents().size());
        assertEquals("CONSULTATION", consultOnlyTimeline.getEvents().get(0).getEventType());
        assertFalse(consultOnlyTimeline.getEvents().stream().anyMatch(e -> "LAB_ORDER".equals(e.getEventType())));

        // Scenario B: Doctor 1 granted LAB_ONLY consent
        PatientConsent labOnlyConsent = new PatientConsent();
        labOnlyConsent.setPatient(patient1Profile);
        labOnlyConsent.setGrantedToUser(doctor1User);
        labOnlyConsent.setDataCategory(DataCategory.LAB_ONLY);
        labOnlyConsent.setStatus(ConsentStatus.ACTIVE);
        labOnlyConsent.setGrantedAt(LocalDateTime.now());
        labOnlyConsent.setExpiresAt(LocalDateTime.now().plusDays(5));
        patientConsentRepository.save(labOnlyConsent);

        setSecurityContext(doctor1User, "DOCTOR_VIEW_AUTHORIZED_RECORD");
        PatientTimelineResponseDto labOnlyTimeline = patientTimelineService.getTimelineForProvider(
                doctor1User.getUserId(), patient1Profile.getPatientId(), apt1.getAppointmentId(), null
        );

        assertEquals("LAB_ONLY", labOnlyTimeline.getAuthorizedCategory());
        assertEquals(1, labOnlyTimeline.getEvents().size());
        assertEquals("LAB_ORDER", labOnlyTimeline.getEvents().get(0).getEventType());
        assertFalse(labOnlyTimeline.getEvents().stream().anyMatch(e -> "CONSULTATION".equals(e.getEventType())));
    }

    @Test
    @DisplayName("Timeline: Provider cannot view timeline without active patient consent")
    void testTimeline03_ProviderTimeline_WithoutConsent_Forbidden() {
        Appointment apt2 = createAppointment(patient1Profile, doctor2Profile, AppointmentStatus.SCHEDULED);
        setSecurityContext(doctor2User, "DOCTOR_VIEW_AUTHORIZED_RECORD");

        // Doctor 2 has no consent for Patient 1
        assertThrows(AccessDeniedException.class, () ->
                patientTimelineService.getTimelineForProvider(doctor2User.getUserId(), patient1Profile.getPatientId(), apt2.getAppointmentId(), null));
    }

    // ============================================================
    // 6. CLINICAL CARE CONTEXT & PERMISSION REMEDIATION TESTS
    // ============================================================

    @Test
    @DisplayName("CareContext: Valid appointment-based consultation creation succeeds and logs appointment context")
    void testCareContext01_ValidAppointmentBasedConsultation_Success() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        Appointment apt = createAppointment(patient1Profile, doctor1Profile, AppointmentStatus.SCHEDULED);

        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION");
        CreateConsultationRequestDto request = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Hypertension check", "BP 125/80 mmHg", apt.getAppointmentId()
        );

        long initAuditCount = auditLogRepository.count();
        MedicalRecordResponseDto response = medicalRecordService.createConsultation(doctor1User.getUserId(), request);

        assertNotNull(response.getRecordId());
        assertEquals(initAuditCount + 1, auditLogRepository.count());
        List<AuditLog> logs = auditLogRepository.findAll();
        AuditLog lastLog = logs.get(logs.size() - 1);
        assertTrue(lastLog.getResourceAccessed().contains("CareContext: APPOINTMENT [ID: " + apt.getAppointmentId()));
    }

    @Test
    @DisplayName("CareContext: Appointment belonging to another doctor is rejected")
    void testCareContext02_AppointmentBelongingToAnotherDoctor_Rejected() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        // Appointment belongs to Doctor 2 and Patient 1
        Appointment aptDoctor2 = createAppointment(patient1Profile, doctor2Profile, AppointmentStatus.SCHEDULED);

        // Doctor 1 attempts to create consultation using Doctor 2's appointment
        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION");
        CreateConsultationRequestDto request = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Complaint", "Notes", aptDoctor2.getAppointmentId()
        );

        assertThrows(AccessDeniedException.class, () ->
                medicalRecordService.createConsultation(doctor1User.getUserId(), request));
    }

    @Test
    @DisplayName("CareContext: Appointment belonging to another patient is rejected")
    void testCareContext03_AppointmentBelongingToAnotherPatient_Rejected() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        // Appointment belongs to Doctor 1 and Patient 2
        Appointment aptPatient2 = createAppointment(patient2Profile, doctor1Profile, AppointmentStatus.SCHEDULED);

        // Doctor 1 attempts to create consultation for Patient 1 using Patient 2's appointment
        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION");
        CreateConsultationRequestDto request = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Complaint", "Notes", aptPatient2.getAppointmentId()
        );

        assertThrows(AccessDeniedException.class, () ->
                medicalRecordService.createConsultation(doctor1User.getUserId(), request));
    }

    @Test
    @DisplayName("CareContext: Cancelled appointment is rejected")
    void testCareContext04_CancelledAppointment_Rejected() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        // Cancelled appointment
        Appointment cancelledApt = createAppointment(patient1Profile, doctor1Profile, AppointmentStatus.CANCELLED);

        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION");
        CreateConsultationRequestDto request = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Complaint", "Notes", cancelledApt.getAppointmentId()
        );

        assertThrows(IllegalStateException.class, () ->
                medicalRecordService.createConsultation(doctor1User.getUserId(), request));
    }

    @Test
    @DisplayName("CareContext: Valid documented walk-in consultation succeeds and logs walk-in context")
    void testCareContext05_ValidDocumentedWalkInConsultation_Success() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION");
        CreateConsultationRequestDto request = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Sudden sharp abdominal pain",
                "Acute appendicitis suspect - urgent ultrasound advised",
                true, "Emergency acute walk-in without scheduled slot"
        );

        long initAuditCount = auditLogRepository.count();
        MedicalRecordResponseDto record = medicalRecordService.createConsultation(doctor1User.getUserId(), request);

        assertNotNull(record.getRecordId());
        assertEquals(initAuditCount + 1, auditLogRepository.count());
        List<AuditLog> logs = auditLogRepository.findAll();
        AuditLog lastLog = logs.get(logs.size() - 1);
        assertTrue(lastLog.getResourceAccessed().contains("CareContext: WALK_IN [Justification: Emergency acute walk-in"));
    }

    @Test
    @DisplayName("CareContext: Walk-in without meaningful justification is rejected")
    void testCareContext06_MissingWalkInJustification_Rejected() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION");
        CreateConsultationRequestDto request = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Pain", "Notes",
                true, "   " // blank justification
        );

        assertThrows(IllegalArgumentException.class, () ->
                medicalRecordService.createConsultation(doctor1User.getUserId(), request));
    }

    @Test
    @DisplayName("CareContext: Walk-in authorization does not become permanent patient access")
    void testCareContext07_WalkInAuthorization_NotBecomingPermanent() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        // 1. Doctor records walk-in consultation
        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION", "DOCTOR_VIEW_AUTHORIZED_RECORD");
        CreateConsultationRequestDto request = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Walk-in flu", "Rest and fluids",
                true, "Urgent seasonal walk-in consultation"
        );
        medicalRecordService.createConsultation(doctor1User.getUserId(), request);

        // 2. Doctor attempts subsequent read WITHOUT appointment or walk-in justification
        // Note: No appointment exists between doctor1 and patient1 in repository
        assertThrows(AccessDeniedException.class, () ->
                medicalRecordService.getMedicalRecordsForDoctor(doctor1User.getUserId(), patient1Profile.getPatientId(), null, null));
    }

    @Test
    @DisplayName("Permission: Doctor lacking DOCTOR_CREATE_CONSULTATION is rejected")
    void testPermission01_DoctorMissingCreateConsultationPermission_Rejected() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        Appointment apt = createAppointment(patient1Profile, doctor1Profile, AppointmentStatus.SCHEDULED);

        // Doctor has only read permission, lacks creation permission
        setSecurityContext(doctor1User, "DOCTOR_VIEW_AUTHORIZED_RECORD");
        CreateConsultationRequestDto request = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Pain", "Notes", apt.getAppointmentId()
        );

        assertThrows(AccessDeniedException.class, () ->
                medicalRecordService.createConsultation(doctor1User.getUserId(), request));
    }

    @Test
    @DisplayName("Permission: Doctor lacking DOCTOR_CREATE_PRESCRIPTION is rejected")
    void testPermission02_DoctorMissingCreatePrescriptionPermission_Rejected() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        Appointment apt = createAppointment(patient1Profile, doctor1Profile, AppointmentStatus.SCHEDULED);

        // Doctor has create consultation permission, but lacks prescription permission
        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION");
        CreatePrescriptionRequestDto rxRequest = new CreatePrescriptionRequestDto(
                patient1Profile.getPatientId(), null, "Instructions",
                List.of(new PrescriptionItemRequestDto("Amoxicillin", "500mg", "TDS", 5, "Oral")),
                apt.getAppointmentId()
        );

        assertThrows(AccessDeniedException.class, () ->
                prescriptionService.createPrescription(doctor1User.getUserId(), rxRequest));
    }

    @Test
    @DisplayName("Permission: Emergency caller lacking EMERGENCY_VIEW_EMERGENCY_INFO is rejected")
    void testPermission03_EmergencyMissingEmergencyInfoPermission_Rejected() {
        // Authenticated as EMERGENCY role but missing granular permission
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                emergencyUser.getUserId(), null, List.of(new SimpleGrantedAuthority("ROLE_EMERGENCY"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(AccessDeniedException.class, () ->
                emergencyAccessService.getEmergencyProfileByHealthId(
                        emergencyUser.getUserId(), "ROLE_EMERGENCY", patient1Profile.getHealthId(), "Ambulance triage"
                ));
    }

    @Test
    @DisplayName("Permission: Patient lacking PATIENT_MANAGE_CONSENT is rejected")
    void testPermission04_PatientMissingManageConsentPermission_Rejected() {
        // Authenticated with only profile view, missing consent management
        setSecurityContext(patient1User, "PATIENT_VIEW_OWN_PROFILE");

        PatientConsentRequestDto request = new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        );

        assertThrows(AccessDeniedException.class, () ->
                patientConsentService.grantConsent(patient1User.getUserId(), request));
    }

    @Test
    @DisplayName("Prescription: Prescription without any authorized care context is rejected")
    void testPrescription04_WithoutCareContext_Rejected() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        setSecurityContext(doctor1User, "DOCTOR_CREATE_PRESCRIPTION");
        // No recordId, no appointmentId, no walkInJustification
        CreatePrescriptionRequestDto noContextRx = new CreatePrescriptionRequestDto(
                patient1Profile.getPatientId(), null, "No care context",
                List.of(new PrescriptionItemRequestDto("Cetirizine", "10mg", "OD", 5, "Bedtime"))
        );

        assertThrows(IllegalArgumentException.class, () ->
                prescriptionService.createPrescription(doctor1User.getUserId(), noContextRx));
    }

    @Test
    @DisplayName("Audit: Distinguishes appointment-based and walk-in care contexts in audit log")
    void testAudit01_DistinguishesAppointmentAndWalkInCareContext() {
        setSecurityContext(patient1User, "PATIENT_MANAGE_CONSENT");
        patientConsentService.grantConsent(patient1User.getUserId(), new PatientConsentRequestDto(
                doctor1User.getUserId(), DataCategory.CONSULT_ONLY, LocalDateTime.now().plusDays(7)
        ));

        // 1. Appointment-based consultation
        Appointment apt = createAppointment(patient1Profile, doctor1Profile, AppointmentStatus.SCHEDULED);
        setSecurityContext(doctor1User, "DOCTOR_CREATE_CONSULTATION");
        CreateConsultationRequestDto aptRequest = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Followup", "Normal", apt.getAppointmentId()
        );
        medicalRecordService.createConsultation(doctor1User.getUserId(), aptRequest);

        List<AuditLog> logs = auditLogRepository.findAll();
        AuditLog aptLog = logs.get(logs.size() - 1);
        assertTrue(aptLog.getResourceAccessed().contains("CareContext: APPOINTMENT [ID: " + apt.getAppointmentId()));

        // 2. Walk-in consultation
        CreateConsultationRequestDto walkInRequest = new CreateConsultationRequestDto(
                patient1Profile.getPatientId(), EncounterType.CONSULTATION, "Sprain", "Ice compression",
                true, "Walk-in sports injury"
        );
        medicalRecordService.createConsultation(doctor1User.getUserId(), walkInRequest);

        logs = auditLogRepository.findAll();
        AuditLog walkInLog = logs.get(logs.size() - 1);
        assertTrue(walkInLog.getResourceAccessed().contains("CareContext: WALK_IN [Justification: Walk-in sports injury]"));
    }
}
