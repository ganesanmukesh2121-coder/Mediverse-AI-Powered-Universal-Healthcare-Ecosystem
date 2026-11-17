package com.mediverse.dto;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mediverse.entity.AppointmentStatus;
import com.mediverse.entity.AppointmentType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class AppointmentDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("1. Valid appointment creation request passes validation")
    void testValidAppointmentCreationRequest() {
        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(2),
                AppointmentType.IN_PERSON,
                "Routine checkup"
        );

        Set<ConstraintViolation<CreateAppointmentRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid request must have no violations");
    }

    @Test
    @DisplayName("2. Missing or blank doctorId is rejected")
    void testMissingDoctorIdRejected() {
        CreateAppointmentRequestDto dtoNull = new CreateAppointmentRequestDto(
                null,
                LocalDateTime.now().plusDays(1),
                AppointmentType.TELECONSULT,
                "Reason"
        );
        Set<ConstraintViolation<CreateAppointmentRequestDto>> violationsNull = validator.validate(dtoNull);
        assertFalse(violationsNull.isEmpty());
        assertTrue(violationsNull.stream().anyMatch(v -> v.getPropertyPath().toString().equals("doctorId")));

        CreateAppointmentRequestDto dtoBlank = new CreateAppointmentRequestDto(
                "   ",
                LocalDateTime.now().plusDays(1),
                AppointmentType.TELECONSULT,
                "Reason"
        );
        Set<ConstraintViolation<CreateAppointmentRequestDto>> violationsBlank = validator.validate(dtoBlank);
        assertFalse(violationsBlank.isEmpty());
        assertTrue(violationsBlank.stream().anyMatch(v -> v.getPropertyPath().toString().equals("doctorId")));
    }

    @Test
    @DisplayName("3. Past appointment date is rejected")
    void testPastAppointmentDateRejected() {
        CreateAppointmentRequestDto dtoPast = new CreateAppointmentRequestDto(
                UUID.randomUUID().toString(),
                LocalDateTime.now().minusDays(1),
                AppointmentType.IN_PERSON,
                "Past visit"
        );

        Set<ConstraintViolation<CreateAppointmentRequestDto>> violations = validator.validate(dtoPast);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("appointmentDate")));

        // Null appointment date
        CreateAppointmentRequestDto dtoNullDate = new CreateAppointmentRequestDto(
                UUID.randomUUID().toString(),
                null,
                AppointmentType.IN_PERSON,
                "Null date visit"
        );
        Set<ConstraintViolation<CreateAppointmentRequestDto>> violationsNull = validator.validate(dtoNullDate);
        assertFalse(violationsNull.isEmpty());
        assertTrue(violationsNull.stream().anyMatch(v -> v.getPropertyPath().toString().equals("appointmentDate")));
    }

    @Test
    @DisplayName("4. Missing appointment type is rejected")
    void testMissingAppointmentTypeRejected() {
        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(1),
                null,
                "Reason"
        );

        Set<ConstraintViolation<CreateAppointmentRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("appointmentType")));
    }

    @Test
    @DisplayName("5. Valid status update request passes validation")
    void testValidStatusUpdateRequest() {
        UpdateAppointmentStatusRequestDto dto1 = new UpdateAppointmentStatusRequestDto(
                AppointmentStatus.CANCELLED,
                "Patient requested cancellation due to personal emergency"
        );
        Set<ConstraintViolation<UpdateAppointmentStatusRequestDto>> violations1 = validator.validate(dto1);
        assertTrue(violations1.isEmpty());

        UpdateAppointmentStatusRequestDto dto2 = new UpdateAppointmentStatusRequestDto(
                AppointmentStatus.COMPLETED
        );
        Set<ConstraintViolation<UpdateAppointmentStatusRequestDto>> violations2 = validator.validate(dto2);
        assertTrue(violations2.isEmpty());
    }

    @Test
    @DisplayName("6. Missing status is rejected")
    void testMissingStatusRejected() {
        UpdateAppointmentStatusRequestDto dto = new UpdateAppointmentStatusRequestDto(null);

        Set<ConstraintViolation<UpdateAppointmentStatusRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @Test
    @DisplayName("7. Oversized reason rejected")
    void testOversizedReasonRejected() {
        String longReason = "a".repeat(1001);
        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(1),
                AppointmentType.IN_PERSON,
                longReason
        );

        Set<ConstraintViolation<CreateAppointmentRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("reason")));
    }

    @Test
    @DisplayName("8. Oversized cancellation reason rejected")
    void testOversizedCancellationReasonRejected() {
        String longCancellation = "c".repeat(1001);
        UpdateAppointmentStatusRequestDto dto = new UpdateAppointmentStatusRequestDto(
                AppointmentStatus.CANCELLED,
                longCancellation
        );

        Set<ConstraintViolation<UpdateAppointmentStatusRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("cancellationReason")));
    }

    @Test
    @DisplayName("9. Response DTO contains expected fields and does not expose security data")
    void testResponseDtoContainsExpectedFieldsAndDoesNotExposeSecurityData() {
        String aptId = UUID.randomUUID().toString();
        String patId = UUID.randomUUID().toString();
        String docId = UUID.randomUUID().toString();
        LocalDateTime aptDate = LocalDateTime.now().plusDays(2);
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        AppointmentResponseDto dto = new AppointmentResponseDto(
                aptId,
                patId,
                "John Doe",
                docId,
                "Dr. Smith",
                aptDate,
                AppointmentType.TELECONSULT,
                AppointmentStatus.SCHEDULED,
                "Fever checkup",
                createdAt,
                updatedAt
        );

        assertEquals(aptId, dto.getAppointmentId());
        assertEquals(patId, dto.getPatientId());
        assertEquals("John Doe", dto.getPatientName());
        assertEquals(docId, dto.getDoctorId());
        assertEquals("Dr. Smith", dto.getDoctorName());
        assertEquals(aptDate, dto.getAppointmentDate());
        assertEquals("TELECONSULT", dto.getAppointmentType());
        assertEquals("SCHEDULED", dto.getStatus());
        assertEquals("Fever checkup", dto.getReason());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());

        // Security assertion: no sensitive fields
        List<String> prohibitedSecurityFields = List.of(
                "password", "passwordHash", "token", "accessToken", "refreshToken", "secret", "credentials"
        );

        Field[] fields = AppointmentResponseDto.class.getDeclaredFields();
        for (Field f : fields) {
            String nameLower = f.getName().toLowerCase();
            for (String prohibited : prohibitedSecurityFields) {
                assertFalse(nameLower.contains(prohibited.toLowerCase()),
                        "Security breach: AppointmentResponseDto must not declare sensitive field: " + f.getName());
            }
        }
    }

    @Test
    @DisplayName("10. Request DTOs do not declare prohibited fields")
    void testRequestDtosDoNotDeclareProhibitedFields() {
        // CreateAppointmentRequestDto must not declare patientId, patientUserId, appointmentId, status, createdAt, updatedAt
        List<String> prohibitedCreateFields = List.of(
                "patientId", "patientUserId", "appointmentId", "status", "createdAt", "updatedAt"
        );
        Set<String> createFieldNames = Arrays.stream(CreateAppointmentRequestDto.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        for (String prohibited : prohibitedCreateFields) {
            assertFalse(createFieldNames.contains(prohibited),
                    "Security breach: CreateAppointmentRequestDto must not contain: " + prohibited);
        }

        // UpdateAppointmentStatusRequestDto must not declare actor IDs, patient IDs, doctor IDs or appointment IDs
        List<String> prohibitedUpdateFields = List.of(
                "actorId", "actorUserId", "patientId", "patientUserId", "doctorId", "doctorUserId", "appointmentId"
        );
        Set<String> updateFieldNames = Arrays.stream(UpdateAppointmentStatusRequestDto.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        for (String prohibited : prohibitedUpdateFields) {
            assertFalse(updateFieldNames.contains(prohibited),
                    "Security breach: UpdateAppointmentStatusRequestDto must not contain: " + prohibited);
        }
    }
}
