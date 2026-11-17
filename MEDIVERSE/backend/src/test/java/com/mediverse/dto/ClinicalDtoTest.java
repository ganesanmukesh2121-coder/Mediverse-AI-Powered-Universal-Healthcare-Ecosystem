package com.mediverse.dto;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mediverse.entity.DataCategory;
import com.mediverse.entity.EncounterType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ClinicalDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ============================================================
    // 1. DTO VALIDATION TESTS
    // ============================================================

    @Test
    @DisplayName("CreateConsultationRequestDto: Valid payload passes, blank fields fail")
    void testCreateConsultationRequestDto_Validation() {
        CreateConsultationRequestDto valid = new CreateConsultationRequestDto(
                "patient-uuid-1", EncounterType.CONSULTATION, "Severe headache", "Migraine with aura"
        );
        Set<ConstraintViolation<CreateConsultationRequestDto>> violations = validator.validate(valid);
        assertTrue(violations.isEmpty());

        // Blank patientId, chief complaint, diagnosis notes
        CreateConsultationRequestDto invalid = new CreateConsultationRequestDto(
                "", null, "", ""
        );
        Set<ConstraintViolation<CreateConsultationRequestDto>> invalidViolations = validator.validate(invalid);
        assertFalse(invalidViolations.isEmpty());
        assertTrue(invalidViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("patientId")));
        assertTrue(invalidViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("encounterType")));
        assertTrue(invalidViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("chiefComplaint")));
        assertTrue(invalidViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("diagnosisNotes")));
    }

    @Test
    @DisplayName("CreatePrescriptionRequestDto: Validates item presence and nested item fields")
    void testCreatePrescriptionRequestDto_Validation() {
        // Empty items list fails
        CreatePrescriptionRequestDto noItems = new CreatePrescriptionRequestDto(
                "patient-uuid-1", null, "Instructions", new ArrayList<>()
        );
        Set<ConstraintViolation<CreatePrescriptionRequestDto>> noItemViolations = validator.validate(noItems);
        assertTrue(noItemViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("items")));

        // Invalid prescription item: duration = 0, blank medicine name
        PrescriptionItemRequestDto invalidItem = new PrescriptionItemRequestDto(
                "", "", "BID", 0, "Take with food"
        );
        CreatePrescriptionRequestDto invalidRx = new CreatePrescriptionRequestDto(
                "patient-uuid-1", null, "Instructions", List.of(invalidItem)
        );
        Set<ConstraintViolation<CreatePrescriptionRequestDto>> itemViolations = validator.validate(invalidRx);
        assertFalse(itemViolations.isEmpty());
        assertTrue(itemViolations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("medicineName")));
        assertTrue(itemViolations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("dosage")));
        assertTrue(itemViolations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("durationDays")));
    }

    @Test
    @DisplayName("PatientConsentRequestDto: Requires grantee, category, and future expiry")
    void testPatientConsentRequestDto_Validation() {
        // Valid
        PatientConsentRequestDto valid = new PatientConsentRequestDto(
                "doctor-user-uuid", DataCategory.FULL_TIMELINE, LocalDateTime.now().plusDays(30)
        );
        assertTrue(validator.validate(valid).isEmpty());

        // Past expiry date fails
        PatientConsentRequestDto pastExpiry = new PatientConsentRequestDto(
                "doctor-user-uuid", DataCategory.FULL_TIMELINE, LocalDateTime.now().minusDays(1)
        );
        Set<ConstraintViolation<PatientConsentRequestDto>> pastViolations = validator.validate(pastExpiry);
        assertTrue(pastViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("expiresAt")));

        // Blank grantee ID fails
        PatientConsentRequestDto blankGrantee = new PatientConsentRequestDto(
                "   ", DataCategory.FULL_TIMELINE, LocalDateTime.now().plusDays(1)
        );
        Set<ConstraintViolation<PatientConsentRequestDto>> blankViolations = validator.validate(blankGrantee);
        assertTrue(blankViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("grantedToUserId")));
    }

    // ============================================================
    // 2. EMERGENCY DTO BOUNDARY TESTS
    // ============================================================

    @Test
    @DisplayName("EmergencyProfileResponseDto: Strictly excludes historical clinical records and sensitive files")
    void testEmergencyProfileResponseDto_BoundaryIntegrity() {
        EmergencyProfileResponseDto dto = new EmergencyProfileResponseDto(
                1L, "p-id-1", "MV-2026-TEST", "John Doe", "O+", "9876543210",
                "Penicillin", "Metformin 500mg", "Type 2 Diabetes", "Emergency note", LocalDateTime.now()
        );

        assertNotNull(dto.getSevereAllergies());
        assertNotNull(dto.getActiveCriticalMedications());
        assertNotNull(dto.getChronicConditions());
        assertNotNull(dto.getEmergencyContactPhone());
        assertNotNull(dto.getBloodGroup());

        // Reflection check: verify DTO contains NO clinical history fields
        List<String> fieldNames = Arrays.stream(EmergencyProfileResponseDto.class.getDeclaredFields())
                .map(Field::getName)
                .toList();

        assertFalse(fieldNames.contains("medicalRecords"));
        assertFalse(fieldNames.contains("consultations"));
        assertFalse(fieldNames.contains("prescriptions"));
        assertFalse(fieldNames.contains("labOrders"));
        assertFalse(fieldNames.contains("medicalReports"));
        assertFalse(fieldNames.contains("radiologyScans"));
        assertFalse(fieldNames.contains("uploadedFiles"));
        assertFalse(fieldNames.contains("timeline"));
        assertFalse(fieldNames.contains("diagnosisNotes"));
    }

    // ============================================================
    // 3. SENSITIVE CREDENTIAL & PERSISTENCE EXCLUSION TESTS
    // ============================================================

    @Test
    @DisplayName("MedicalRecord & Prescription DTOs: Structural field inspection verifies absence of passwords, tokens, or credentials")
    void testClinicalDtos_DoNotExposeInternalCredentials() {
        List<String> recordFields = Arrays.stream(MedicalRecordResponseDto.class.getDeclaredFields())
                .map(Field::getName)
                .toList();

        assertFalse(recordFields.contains("password"));
        assertFalse(recordFields.contains("passwordHash"));
        assertFalse(recordFields.contains("token"));
        assertFalse(recordFields.contains("refreshToken"));
        assertFalse(recordFields.contains("secret"));
        assertFalse(recordFields.contains("supabase"));
        assertFalse(recordFields.contains("storagePath"));

        List<String> rxFields = Arrays.stream(PrescriptionResponseDto.class.getDeclaredFields())
                .map(Field::getName)
                .toList();

        assertFalse(rxFields.contains("password"));
        assertFalse(rxFields.contains("passwordHash"));
        assertFalse(rxFields.contains("token"));
        assertFalse(rxFields.contains("secret"));

        List<String> consentFields = Arrays.stream(PatientConsentResponseDto.class.getDeclaredFields())
                .map(Field::getName)
                .toList();

        assertFalse(consentFields.contains("password"));
        assertFalse(consentFields.contains("secret"));
    }

    // ============================================================
    // 4. TIMELINE EVENT CATEGORY MAPPING TESTS
    // ============================================================

    @Test
    @DisplayName("PatientTimelineResponseDto: Correctly maps events and authorized categories")
    void testTimelineDto_CategoryMapping() {
        PatientTimelineEventDto consultEvent = new PatientTimelineEventDto(
                "rec-101", "CONSULTATION", LocalDateTime.now().minusDays(5),
                "General Consultation", "Fever and cough", "Dr. Alice", "CONSULT_ONLY"
        );

        PatientTimelineEventDto rxEvent = new PatientTimelineEventDto(
                "rx-201", "PRESCRIPTION", LocalDateTime.now().minusDays(5),
                "Prescription Issued", "Paracetamol 650mg", "Dr. Alice", "CONSULT_ONLY"
        );

        PatientTimelineEventDto labEvent = new PatientTimelineEventDto(
                "lab-301", "LAB_ORDER", LocalDateTime.now().minusDays(2),
                "Complete Blood Count", "Status: COMPLETED", "Diagnostic Labs", "LAB_ONLY"
        );

        PatientTimelineEventDto reportEvent = new PatientTimelineEventDto(
                "rep-401", "MEDICAL_REPORT", LocalDateTime.now().minusDays(1),
                "CBC Lab Report", "Hemoglobin normal", "Diagnostic Labs", "LAB_ONLY"
        );

        // 1. FULL_TIMELINE includes all categories
        PatientTimelineResponseDto fullTimeline = new PatientTimelineResponseDto(
                "pat-1", "MV-2026-1234", "FULL_TIMELINE",
                List.of(consultEvent, rxEvent, labEvent, reportEvent)
        );
        assertEquals(4, fullTimeline.getEvents().size());
        assertEquals("FULL_TIMELINE", fullTimeline.getAuthorizedCategory());

        // 2. LAB_ONLY filters to lab order and report only
        List<PatientTimelineEventDto> labOnlyEvents = fullTimeline.getEvents().stream()
                .filter(e -> "LAB_ONLY".equals(e.getCategory()))
                .toList();
        assertEquals(2, labOnlyEvents.size());
        assertTrue(labOnlyEvents.stream().allMatch(e -> e.getCategory().equals("LAB_ONLY")));

        // 3. CONSULT_ONLY filters to consultation and prescription only
        List<PatientTimelineEventDto> consultOnlyEvents = fullTimeline.getEvents().stream()
                .filter(e -> "CONSULT_ONLY".equals(e.getCategory()))
                .toList();
        assertEquals(2, consultOnlyEvents.size());
        assertTrue(consultOnlyEvents.stream().allMatch(e -> e.getCategory().equals("CONSULT_ONLY")));
    }
}
