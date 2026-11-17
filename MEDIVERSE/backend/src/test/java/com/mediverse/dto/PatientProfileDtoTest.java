package com.mediverse.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PatientProfileDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidUpdateRequestDto_PassesValidation() {
        PatientProfileUpdateRequestDto dto = new PatientProfileUpdateRequestDto(
                "Jane Doe",
                "+1234567890",
                LocalDate.of(1990, 5, 20),
                "FEMALE",
                "O+",
                "+1987654321",
                "123 Health Ave, Bangalore"
        );

        Set<ConstraintViolation<PatientProfileUpdateRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid DTO should have no constraint violations");
    }

    @Test
    void testBlankFullName_FailsValidation() {
        PatientProfileUpdateRequestDto dto = new PatientProfileUpdateRequestDto();
        dto.setFullName("   ");

        Set<ConstraintViolation<PatientProfileUpdateRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Blank full name should violate validation constraints");

        boolean hasFullNameViolation = violations.stream()
                .anyMatch(v -> "fullName".equals(v.getPropertyPath().toString())
                        && v.getMessage().contains("Full name is required"));
        assertTrue(hasFullNameViolation, "Should report required validation error for full name");
    }

    @Test
    void testFutureDateOfBirth_FailsValidation() {
        PatientProfileUpdateRequestDto dto = new PatientProfileUpdateRequestDto();
        dto.setFullName("Valid Name");
        dto.setDateOfBirth(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<PatientProfileUpdateRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Future date of birth should violate validation constraints");

        boolean hasDobViolation = violations.stream()
                .anyMatch(v -> "dateOfBirth".equals(v.getPropertyPath().toString())
                        && v.getMessage().contains("Date of birth must be in the past"));
        assertTrue(hasDobViolation, "Should report past date constraint error for date of birth");
    }

    @Test
    void testOversizedFields_FailValidation() {
        String long101 = "A".repeat(101);
        String long21 = "1".repeat(21);
        String long11 = "G".repeat(11);
        String long6 = "B".repeat(6);
        String long1001 = "X".repeat(1001);

        PatientProfileUpdateRequestDto dto = new PatientProfileUpdateRequestDto(
                long101,
                long21,
                LocalDate.of(1995, 1, 1),
                long11,
                long6,
                long21,
                long1001
        );

        Set<ConstraintViolation<PatientProfileUpdateRequestDto>> violations = validator.validate(dto);
        assertEquals(6, violations.size(), "All oversized fields should fail validation");

        Set<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(violatedProperties.contains("fullName"));
        assertTrue(violatedProperties.contains("phoneNumber"));
        assertTrue(violatedProperties.contains("gender"));
        assertTrue(violatedProperties.contains("bloodGroup"));
        assertTrue(violatedProperties.contains("emergencyContactPhone"));
        assertTrue(violatedProperties.contains("address"));
    }

    @Test
    void testImmutableSecurityFieldsAbsentFromUpdateRequestDto() {
        List<String> forbiddenFields = List.of(
                "userId",
                "patientId",
                "healthId",
                "email",
                "role",
                "accountStatus",
                "password",
                "passwordHash",
                "createdAt",
                "updatedAt"
        );

        Set<String> declaredFields = Arrays.stream(PatientProfileUpdateRequestDto.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        for (String forbidden : forbiddenFields) {
            assertFalse(declaredFields.contains(forbidden),
                    "Security violation: PatientProfileUpdateRequestDto must NOT contain field: " + forbidden);
        }
    }

    @Test
    void testResponseDto_GettersAndSettersAndSecurityHygiene() {
        LocalDate dob = LocalDate.of(1988, 12, 10);
        PatientProfileResponseDto dto = new PatientProfileResponseDto(
                "Alice Smith",
                "alice@mediverse.com",
                "+1234567890",
                "ACTIVE",
                "MV-2026-12345678",
                dob,
                "FEMALE",
                "B+",
                "+1987654321",
                "789 Doctor Ln"
        );

        assertEquals("Alice Smith", dto.getFullName());
        assertEquals("alice@mediverse.com", dto.getEmail());
        assertEquals("+1234567890", dto.getPhoneNumber());
        assertEquals("ACTIVE", dto.getAccountStatus());
        assertEquals("MV-2026-12345678", dto.getHealthId());
        assertEquals(dob, dto.getDateOfBirth());
        assertEquals("FEMALE", dto.getGender());
        assertEquals("B+", dto.getBloodGroup());
        assertEquals("+1987654321", dto.getEmergencyContactPhone());
        assertEquals("789 Doctor Ln", dto.getAddress());

        // Verify no-args constructor and setters
        PatientProfileResponseDto emptyDto = new PatientProfileResponseDto();
        emptyDto.setFullName("Bob");
        emptyDto.setEmail("bob@mediverse.com");
        emptyDto.setPhoneNumber("+999");
        emptyDto.setAccountStatus("PENDING");
        emptyDto.setHealthId("MV-2026-87654321");
        emptyDto.setDateOfBirth(dob);
        emptyDto.setGender("MALE");
        emptyDto.setBloodGroup("O-");
        emptyDto.setEmergencyContactPhone("+888");
        emptyDto.setAddress("456 Clinic Blvd");

        assertEquals("Bob", emptyDto.getFullName());
        assertEquals("bob@mediverse.com", emptyDto.getEmail());
        assertEquals("+999", emptyDto.getPhoneNumber());
        assertEquals("PENDING", emptyDto.getAccountStatus());
        assertEquals("MV-2026-87654321", emptyDto.getHealthId());
        assertEquals(dob, emptyDto.getDateOfBirth());
        assertEquals("MALE", emptyDto.getGender());
        assertEquals("O-", emptyDto.getBloodGroup());
        assertEquals("+888", emptyDto.getEmergencyContactPhone());
        assertEquals("456 Clinic Blvd", emptyDto.getAddress());

        // Verify security hygiene: response DTO must never declare password or internal security fields
        List<String> forbiddenInResponse = List.of("password", "passwordHash", "roleId", "permissions");
        Set<String> responseFields = Arrays.stream(PatientProfileResponseDto.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        for (String forbidden : forbiddenInResponse) {
            assertFalse(responseFields.contains(forbidden),
                    "Security violation: PatientProfileResponseDto must NOT expose field: " + forbidden);
        }
    }
}
