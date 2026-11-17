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
import com.mediverse.entity.ConsentStatus;
import com.mediverse.entity.DataCategory;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.EmergencyProfile;
import com.mediverse.entity.EncounterType;
import com.mediverse.entity.LabOrder;
import com.mediverse.entity.LaboratoryProfile;
import com.mediverse.entity.MedicalRecord;
import com.mediverse.entity.MedicalReport;
import com.mediverse.entity.OrderStatus;
import com.mediverse.entity.PatientConsent;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.Prescription;
import com.mediverse.entity.PrescriptionItem;
import com.mediverse.entity.ReportType;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class ClinicalRepositoryTest {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PrescriptionItemRepository prescriptionItemRepository;

    @Autowired
    private LabOrderRepository labOrderRepository;

    @Autowired
    private MedicalReportRepository medicalReportRepository;

    @Autowired
    private PatientConsentRepository patientConsentRepository;

    @Autowired
    private EmergencyProfileRepository emergencyProfileRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private LaboratoryProfileRepository laboratoryProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User patient1User;
    private PatientProfile patient1Profile;

    private User patient2User;
    private PatientProfile patient2Profile;

    private User doctorUser;
    private DoctorProfile doctorProfile;

    private User labUser;
    private LaboratoryProfile labProfile;

    @BeforeEach
    void setUp() {
        Role patientRole = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));
        Role doctorRole = roleRepository.findByRoleName("DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("DOCTOR", "Doctor role")));
        Role labRole = roleRepository.findByRoleName("LAB")
                .orElseGet(() -> roleRepository.save(new Role("LAB", "Laboratory role")));

        // Patient 1
        patient1User = createTestUser("clin_p1", patientRole);
        patient1Profile = createTestPatient(patient1User);

        // Patient 2
        patient2User = createTestUser("clin_p2", patientRole);
        patient2Profile = createTestPatient(patient2User);

        // Doctor
        doctorUser = createTestUser("clin_doc", doctorRole);
        doctorProfile = createTestDoctor(doctorUser);

        // Lab
        labUser = createTestUser("clin_lab", labRole);
        labProfile = createTestLab(labUser);
    }

    private User createTestUser(String prefix, Role role) {
        User user = new User();
        user.setEmail(prefix + "_" + UUID.randomUUID() + "@mediverse.com");
        user.setPasswordHash("hashed_secret_pw");
        user.setFullName("User " + prefix);
        user.setPhoneNumber("987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7));
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(user);
    }

    private PatientProfile createTestPatient(User user) {
        PatientProfile profile = new PatientProfile();
        profile.setUser(user);
        profile.setHealthId("MV-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        profile.setDateOfBirth(LocalDate.of(1995, 5, 15));
        profile.setBloodGroup("O+");
        profile.setEmergencyContactPhone("9876543210");
        return patientProfileRepository.save(profile);
    }

    private DoctorProfile createTestDoctor(User user) {
        DoctorProfile doc = new DoctorProfile();
        doc.setUser(user);
        doc.setLicenseNumber("DOC-LIC-" + UUID.randomUUID().toString().substring(0, 8));
        doc.setSpecialization("Internal Medicine");
        doc.setHospitalAffiliation("Metropolitan Hospital");
        doc.setVerificationStatus(VerificationStatus.ACTIVE);
        return doctorProfileRepository.save(doc);
    }

    private LaboratoryProfile createTestLab(User user) {
        LaboratoryProfile lab = new LaboratoryProfile();
        lab.setUser(user);
        lab.setLabName("Diagnostic Pathology Labs");
        lab.setAccreditationNumber("ACC-LAB-" + UUID.randomUUID().toString().substring(0, 8));
        lab.setVerificationStatus(VerificationStatus.ACTIVE);
        return laboratoryProfileRepository.save(lab);
    }

    // ============================================================
    // 1. MEDICAL RECORD REPOSITORY TESTS
    // ============================================================

    @Test
    @DisplayName("MedicalRecordRepository: Scoped queries return only owner's records")
    void testMedicalRecordRepository_PatientIsolation() {
        MedicalRecord rec1 = new MedicalRecord();
        rec1.setPatient(patient1Profile);
        rec1.setDoctor(doctorProfile);
        rec1.setEncounterType(EncounterType.CONSULTATION);
        rec1.setChiefComplaint("Persistent fever");
        rec1.setDiagnosisNotes("Acute viral infection");
        rec1.setEncounterDate(LocalDateTime.now().minusDays(2));
        medicalRecordRepository.save(rec1);

        MedicalRecord rec2 = new MedicalRecord();
        rec2.setPatient(patient2Profile);
        rec2.setDoctor(doctorProfile);
        rec2.setEncounterType(EncounterType.CONSULTATION);
        rec2.setChiefComplaint("Sprained ankle");
        rec2.setDiagnosisNotes("Ligament stretch");
        rec2.setEncounterDate(LocalDateTime.now().minusDays(1));
        medicalRecordRepository.save(rec2);

        // Patient 1 sees only rec1
        List<MedicalRecord> p1Records = medicalRecordRepository.findAllByPatient_PatientIdOrderByEncounterDateDesc(patient1Profile.getPatientId());
        assertEquals(1, p1Records.size());
        assertEquals(rec1.getRecordId(), p1Records.get(0).getRecordId());

        // Cross-patient lookup returns empty
        Optional<MedicalRecord> crossCheck = medicalRecordRepository.findByRecordIdAndPatient_PatientId(rec1.getRecordId(), patient2Profile.getPatientId());
        assertTrue(crossCheck.isEmpty());

        // Doctor sees both authored records
        List<MedicalRecord> docRecords = medicalRecordRepository.findAllByDoctor_DoctorIdOrderByEncounterDateDesc(doctorProfile.getDoctorId());
        assertEquals(2, docRecords.size());
    }

    // ============================================================
    // 2. PRESCRIPTION & ITEM REPOSITORY TESTS
    // ============================================================

    @Test
    @DisplayName("PrescriptionRepository: Cascade saves items and enforces patient scoping")
    void testPrescriptionRepository_PatientScopingAndItemsCascade() {
        Prescription rx = new Prescription();
        rx.setPatient(patient1Profile);
        rx.setDoctor(doctorProfile);
        rx.setPrescriptionDate(LocalDateTime.now());
        rx.setGeneralInstructions("Take medications after food");

        PrescriptionItem item1 = new PrescriptionItem();
        item1.setPrescription(rx);
        item1.setMedicineName("Amoxicillin");
        item1.setDosage("500mg");
        item1.setFrequency("TDS");
        item1.setDurationDays(5);
        item1.setInstructions("With full glass of water");
        rx.getPrescriptionItems().add(item1);

        Prescription savedRx = prescriptionRepository.save(rx);
        assertNotNull(savedRx.getPrescriptionId());

        // Verify patient isolation
        List<Prescription> p1RxList = prescriptionRepository.findAllByPatient_PatientIdOrderByPrescriptionDateDesc(patient1Profile.getPatientId());
        assertEquals(1, p1RxList.size());
        assertEquals(1, p1RxList.get(0).getPrescriptionItems().size());

        List<Prescription> p2RxList = prescriptionRepository.findAllByPatient_PatientIdOrderByPrescriptionDateDesc(patient2Profile.getPatientId());
        assertTrue(p2RxList.isEmpty());

        // Verify PrescriptionItemRepository query
        List<PrescriptionItem> items = prescriptionItemRepository.findAllByPrescription_PrescriptionId(savedRx.getPrescriptionId());
        assertEquals(1, items.size());
        assertEquals("Amoxicillin", items.get(0).getMedicineName());
    }

    // ============================================================
    // 3. PATIENT CONSENT REPOSITORY TESTS
    // ============================================================

    @Test
    @DisplayName("PatientConsentRepository: Active unexpired consent is verified; expired/revoked are excluded")
    void testPatientConsentRepository_ConsentLifecycleAndExclusion() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Active unexpired consent for Patient 1 -> Doctor
        PatientConsent activeConsent = new PatientConsent();
        activeConsent.setPatient(patient1Profile);
        activeConsent.setGrantedToUser(doctorUser);
        activeConsent.setDataCategory(DataCategory.FULL_TIMELINE);
        activeConsent.setStatus(ConsentStatus.ACTIVE);
        activeConsent.setGrantedAt(now.minusHours(1));
        activeConsent.setExpiresAt(now.plusDays(7));
        patientConsentRepository.save(activeConsent);

        // 2. Expired consent for Patient 1 -> Lab
        PatientConsent expiredConsent = new PatientConsent();
        expiredConsent.setPatient(patient1Profile);
        expiredConsent.setGrantedToUser(labUser);
        expiredConsent.setDataCategory(DataCategory.LAB_ONLY);
        expiredConsent.setStatus(ConsentStatus.EXPIRED);
        expiredConsent.setGrantedAt(now.minusDays(10));
        expiredConsent.setExpiresAt(now.minusDays(1)); // in the past
        patientConsentRepository.save(expiredConsent);

        // 3. Revoked consent for Patient 2 -> Doctor
        PatientConsent revokedConsent = new PatientConsent();
        revokedConsent.setPatient(patient2Profile);
        revokedConsent.setGrantedToUser(doctorUser);
        revokedConsent.setDataCategory(DataCategory.CONSULT_ONLY);
        revokedConsent.setStatus(ConsentStatus.REVOKED);
        revokedConsent.setGrantedAt(now.minusDays(2));
        revokedConsent.setExpiresAt(now.plusDays(5));
        revokedConsent.setRevokedAt(now.minusHours(2));
        patientConsentRepository.save(revokedConsent);

        // Verify Active Doctor Consent exists for Patient 1
        boolean docHasConsentP1 = patientConsentRepository
                .existsByPatient_PatientIdAndGrantedToUser_UserIdAndStatusAndExpiresAtAfterAndRevokedAtIsNull(
                        patient1Profile.getPatientId(), doctorUser.getUserId(), ConsentStatus.ACTIVE, now);
        assertTrue(docHasConsentP1);

        Optional<PatientConsent> p1DocOpt = patientConsentRepository
                .findFirstByPatient_PatientIdAndGrantedToUser_UserIdAndStatusAndExpiresAtAfterAndRevokedAtIsNullOrderByGrantedAtDesc(
                        patient1Profile.getPatientId(), doctorUser.getUserId(), ConsentStatus.ACTIVE, now);
        assertTrue(p1DocOpt.isPresent());
        assertEquals(DataCategory.FULL_TIMELINE, p1DocOpt.get().getDataCategory());

        // Verify Expired Consent for Lab is excluded
        boolean labHasConsentP1 = patientConsentRepository
                .existsByPatient_PatientIdAndGrantedToUser_UserIdAndStatusAndExpiresAtAfterAndRevokedAtIsNull(
                        patient1Profile.getPatientId(), labUser.getUserId(), ConsentStatus.ACTIVE, now);
        assertFalse(labHasConsentP1);

        // Verify Revoked Consent for Patient 2 is excluded
        boolean docHasConsentP2 = patientConsentRepository
                .existsByPatient_PatientIdAndGrantedToUser_UserIdAndStatusAndExpiresAtAfterAndRevokedAtIsNull(
                        patient2Profile.getPatientId(), doctorUser.getUserId(), ConsentStatus.ACTIVE, now);
        assertFalse(docHasConsentP2);

        // Verify Category-Scoped Query
        boolean hasConsultConsent = patientConsentRepository
                .existsByPatient_PatientIdAndGrantedToUser_UserIdAndDataCategoryAndStatusAndExpiresAtAfterAndRevokedAtIsNull(
                        patient1Profile.getPatientId(), doctorUser.getUserId(), DataCategory.FULL_TIMELINE, ConsentStatus.ACTIVE, now);
        assertTrue(hasConsultConsent);

        boolean hasLabOnlyConsent = patientConsentRepository
                .existsByPatient_PatientIdAndGrantedToUser_UserIdAndDataCategoryAndStatusAndExpiresAtAfterAndRevokedAtIsNull(
                        patient1Profile.getPatientId(), doctorUser.getUserId(), DataCategory.LAB_ONLY, ConsentStatus.ACTIVE, now);
        assertFalse(hasLabOnlyConsent);
    }

    // ============================================================
    // 4. EMERGENCY PROFILE REPOSITORY TESTS
    // ============================================================

    @Test
    @DisplayName("EmergencyProfileRepository: Successfully retrieves emergency info by patient ID, user ID, and Health ID")
    void testEmergencyProfileRepository_Lookups() {
        EmergencyProfile ep = new EmergencyProfile();
        ep.setPatientProfile(patient1Profile);
        ep.setSevereAllergies("Penicillin, Peanuts");
        ep.setActiveCriticalMedications("Insulin Glargine");
        ep.setChronicConditions("Type 1 Diabetes");
        ep.setEmergencyNotes("Carries insulin pen in backpack");
        emergencyProfileRepository.save(ep);

        // Lookup by patientId
        Optional<EmergencyProfile> byPid = emergencyProfileRepository.findByPatientProfile_PatientId(patient1Profile.getPatientId());
        assertTrue(byPid.isPresent());
        assertEquals("Penicillin, Peanuts", byPid.get().getSevereAllergies());

        // Lookup by userId
        Optional<EmergencyProfile> byUid = emergencyProfileRepository.findByPatientProfile_User_UserId(patient1User.getUserId());
        assertTrue(byUid.isPresent());
        assertEquals("Type 1 Diabetes", byUid.get().getChronicConditions());

        // Lookup by HealthId
        Optional<EmergencyProfile> byHealthId = emergencyProfileRepository.findByPatientProfile_HealthId(patient1Profile.getHealthId());
        assertTrue(byHealthId.isPresent());
        assertEquals("Insulin Glargine", byHealthId.get().getActiveCriticalMedications());

        // Patient 2 has no emergency profile
        assertFalse(emergencyProfileRepository.existsByPatientProfile_PatientId(patient2Profile.getPatientId()));
    }

    // ============================================================
    // 5. LAB ORDER & MEDICAL REPORT REPOSITORY TESTS
    // ============================================================

    @Test
    @DisplayName("LabOrder & MedicalReport Repositories: Enforce patient scoping and status filtering")
    void testLabOrderAndMedicalReportRepository_Scoping() {
        // Lab Order
        LabOrder order = new LabOrder();
        order.setPatient(patient1Profile);
        order.setLab(labProfile);
        order.setRequestingDoctor(doctorProfile);
        order.setTestName("Lipid Panel");
        order.setOrderStatus(OrderStatus.ORDERED);
        labOrderRepository.save(order);

        List<LabOrder> p1Orders = labOrderRepository.findAllByPatient_PatientIdOrderByOrderedAtDesc(patient1Profile.getPatientId());
        assertEquals(1, p1Orders.size());
        assertEquals("Lipid Panel", p1Orders.get(0).getTestName());

        List<LabOrder> labOrders = labOrderRepository.findAllByLab_LabIdAndOrderStatusOrderByOrderedAtDesc(labProfile.getLabId(), OrderStatus.ORDERED);
        assertEquals(1, labOrders.size());

        // Medical Report
        MedicalReport report = new MedicalReport();
        report.setPatient(patient1Profile);
        report.setReportTitle("Lipid Profile Results");
        report.setReportType(ReportType.LAB_REPORT);
        report.setStructuredMetricsJson("{\"cholesterol_total\": 195, \"hdl\": 55, \"ldl\": 110}");
        medicalReportRepository.save(report);

        List<MedicalReport> reports = medicalReportRepository.findAllByPatient_PatientIdOrderByCreatedAtDesc(patient1Profile.getPatientId());
        assertEquals(1, reports.size());
        assertEquals(ReportType.LAB_REPORT, reports.get(0).getReportType());

        // Patient 2 has zero reports
        assertTrue(medicalReportRepository.findAllByPatient_PatientIdOrderByCreatedAtDesc(patient2Profile.getPatientId()).isEmpty());
    }
}
