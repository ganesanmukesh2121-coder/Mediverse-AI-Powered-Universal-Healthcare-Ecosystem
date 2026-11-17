package com.mediverse.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ProviderVerificationDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid approval request passes validation")
    void testValidApprovalRequest_PassesValidation() {
        ProviderVerificationRequestDto dto = new ProviderVerificationRequestDto("ACTIVE");
        Set<ConstraintViolation<ProviderVerificationRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid approval decision should have 0 violations");

        ProviderVerificationRequestDto dtoWithLower = new ProviderVerificationRequestDto("active");
        assertTrue(validator.validate(dtoWithLower).isEmpty(), "Case-insensitive ACTIVE should pass");
    }

    @Test
    @DisplayName("Valid rejection request with reason passes validation")
    void testValidRejectionRequestWithReason_PassesValidation() {
        ProviderVerificationRequestDto dto = new ProviderVerificationRequestDto("REJECTED", "License is expired");
        Set<ConstraintViolation<ProviderVerificationRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid rejection with reason should have 0 violations");
    }

    @Test
    @DisplayName("Reject blank decision")
    void testBlankDecision_FailsValidation() {
        ProviderVerificationRequestDto dto = new ProviderVerificationRequestDto("   ");
        Set<ConstraintViolation<ProviderVerificationRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Blank decision must fail validation");

        boolean hasBlankViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Decision is required"));
        assertTrue(hasBlankViolation, "Must report 'Decision is required'");
    }

    @Test
    @DisplayName("Reject unsupported decision")
    void testUnsupportedDecision_FailsValidation() {
        ProviderVerificationRequestDto dto = new ProviderVerificationRequestDto("SUSPENDED");
        Set<ConstraintViolation<ProviderVerificationRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Unsupported decision value must fail validation");

        boolean hasDecisionViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Decision must be either ACTIVE or REJECTED"));
        assertTrue(hasDecisionViolation, "Must report 'Decision must be either ACTIVE or REJECTED'");
    }

    @Test
    @DisplayName("Reject rejection without reason")
    void testRejectionWithoutReason_FailsValidation() {
        ProviderVerificationRequestDto dtoNullReason = new ProviderVerificationRequestDto("REJECTED", null);
        Set<ConstraintViolation<ProviderVerificationRequestDto>> violationsNull = validator.validate(dtoNullReason);
        assertFalse(violationsNull.isEmpty(), "Rejection without reason must fail validation");

        boolean hasReasonViolation = violationsNull.stream()
                .anyMatch(v -> v.getMessage().contains("Rejection reason is required when decision is REJECTED"));
        assertTrue(hasReasonViolation);

        ProviderVerificationRequestDto dtoBlankReason = new ProviderVerificationRequestDto("REJECTED", "   ");
        Set<ConstraintViolation<ProviderVerificationRequestDto>> violationsBlank = validator.validate(dtoBlankReason);
        assertFalse(violationsBlank.isEmpty(), "Rejection with blank reason must fail validation");
    }

    @Test
    @DisplayName("Reject oversized rejection reason")
    void testOversizedRejectionReason_FailsValidation() {
        String long1001 = "A".repeat(1001);
        ProviderVerificationRequestDto dto = new ProviderVerificationRequestDto("REJECTED", long1001);
        Set<ConstraintViolation<ProviderVerificationRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Rejection reason > 1000 characters must fail validation");

        boolean hasSizeViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Rejection reason cannot exceed 1000 characters"));
        assertTrue(hasSizeViolation);
    }

    @Test
    @DisplayName("Ensure PendingProviderResponseDto getters, setters and absence of security fields")
    void testPendingProviderResponseDto_StructureAndSecurity() {
        LocalDateTime now = LocalDateTime.now();
        PendingProviderResponseDto dto = new PendingProviderResponseDto(
                "user-123",
                "Dr. Marcus Welby",
                "marcus@hospital.com",
                "9876543210",
                "DOCTOR",
                "PENDING",
                "PENDING",
                "Dr. Marcus Welby",
                "City General",
                "DOC-9988",
                null,
                "Neurology",
                "Metro Hospital",
                null,
                null,
                now
        );

        assertEquals("user-123", dto.getUserId());
        assertEquals("Dr. Marcus Welby", dto.getFullName());
        assertEquals("marcus@hospital.com", dto.getEmail());
        assertEquals("9876543210", dto.getPhoneNumber());
        assertEquals("DOCTOR", dto.getRole());
        assertEquals("PENDING", dto.getAccountStatus());
        assertEquals("PENDING", dto.getVerificationStatus());
        assertEquals("Dr. Marcus Welby", dto.getProviderName());
        assertEquals("City General", dto.getOrganizationName());
        assertEquals("DOC-9988", dto.getLicenseNumber());
        assertNull(dto.getRegistrationNumber());
        assertEquals("Neurology", dto.getSpecialization());
        assertEquals("Metro Hospital", dto.getHospitalAffiliation());
        assertNull(dto.getFacilityType());
        assertNull(dto.getAccreditationNumber());
        assertEquals(now, dto.getCreatedAt());

        // Verify absence of sensitive credentials or security claims
        List<String> forbiddenFields = List.of(
                "password",
                "passwordHash",
                "refreshToken",
                "refreshTokens",
                "permissions",
                "roleId",
                "healthId" // Health ID is for patients only
        );

        Set<String> declaredFields = Arrays.stream(PendingProviderResponseDto.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        for (String forbidden : forbiddenFields) {
            assertFalse(declaredFields.contains(forbidden),
                    "Security breach: PendingProviderResponseDto must NOT declare field: " + forbidden);
        }
    }
}
