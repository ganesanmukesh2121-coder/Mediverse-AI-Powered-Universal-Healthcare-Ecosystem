package com.mediverse.service;

import com.mediverse.dto.PatientProfileResponseDto;
import com.mediverse.dto.PatientProfileUpdateRequestDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.Gender;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.PatientProfileRepository;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class PatientProfileServiceTest {

    @Autowired
    private PatientProfileService patientProfileService;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private PatientProfile testProfile;
    private String healthId;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));

        testUser = new User();
        testUser.setEmail("patient_service_" + UUID.randomUUID() + "@mediverse.com");
        testUser.setPasswordHash(passwordEncoder.encode("Password@123"));
        testUser.setFullName("Original Patient Name");
        testUser.setPhoneNumber("987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7));
        testUser.setRole(role);
        testUser.setAccountStatus(AccountStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        healthId = "MV-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        testProfile = new PatientProfile();
        testProfile.setUser(testUser);
        testProfile.setHealthId(healthId);
        testProfile.setDateOfBirth(LocalDate.of(1990, 5, 15));
        testProfile.setGender(Gender.MALE);
        testProfile.setBloodGroup("O+");
        testProfile.setEmergencyContactPhone("9123456780");
        testProfile.setAddress("123 Original Street, Bangalore");
        testProfile = patientProfileRepository.save(testProfile);
    }

    @Test
    void test1_RetrieveExistingPatientProfile() {
        PatientProfileResponseDto response = patientProfileService.getPatientProfile(testUser.getUserId());

        assertNotNull(response);
        assertEquals(testUser.getFullName(), response.getFullName());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getPhoneNumber(), response.getPhoneNumber());
        assertEquals("ACTIVE", response.getAccountStatus());
        assertEquals(healthId, response.getHealthId());
        assertEquals(LocalDate.of(1990, 5, 15), response.getDateOfBirth());
        assertEquals("MALE", response.getGender());
        assertEquals("O+", response.getBloodGroup());
        assertEquals("9123456780", response.getEmergencyContactPhone());
        assertEquals("123 Original Street, Bangalore", response.getAddress());
    }

    @Test
    void test2_ReturnNotFoundForUnknownUser() {
        String nonExistentUserId = UUID.randomUUID().toString();

        assertThrows(ResourceNotFoundException.class, () ->
                patientProfileService.getPatientProfile(nonExistentUserId));

        PatientProfileUpdateRequestDto updateDto = new PatientProfileUpdateRequestDto(
                "Jane Doe",
                "9876543210",
                LocalDate.of(1992, 1, 1),
                "FEMALE",
                "A+",
                "9123456789",
                "456 New Road"
        );

        assertThrows(ResourceNotFoundException.class, () ->
                patientProfileService.updatePatientProfile(nonExistentUserId, updateDto));
    }

    @Test
    void test3_UpdatePermittedDemographicFields() {
        PatientProfileUpdateRequestDto updateDto = new PatientProfileUpdateRequestDto(
                "Updated Patient Name",
                "9998887776",
                LocalDate.of(1995, 8, 20),
                "FEMALE",
                "B+",
                "8887776665",
                "789 Health Boulevard, Chennai"
        );

        PatientProfileResponseDto response = patientProfileService.updatePatientProfile(testUser.getUserId(), updateDto);

        assertNotNull(response);
        assertEquals("Updated Patient Name", response.getFullName());
        assertEquals("9998887776", response.getPhoneNumber());
        assertEquals(LocalDate.of(1995, 8, 20), response.getDateOfBirth());
        assertEquals("FEMALE", response.getGender());
        assertEquals("B+", response.getBloodGroup());
        assertEquals("8887776665", response.getEmergencyContactPhone());
        assertEquals("789 Health Boulevard, Chennai", response.getAddress());

        // Verify database persistence
        User persistedUser = userRepository.findById(testUser.getUserId()).orElseThrow();
        assertEquals("Updated Patient Name", persistedUser.getFullName());
        assertEquals("9998887776", persistedUser.getPhoneNumber());

        PatientProfile persistedProfile = patientProfileRepository.findByUser_UserId(testUser.getUserId()).orElseThrow();
        assertEquals(LocalDate.of(1995, 8, 20), persistedProfile.getDateOfBirth());
        assertEquals(Gender.FEMALE, persistedProfile.getGender());
        assertEquals("B+", persistedProfile.getBloodGroup());
        assertEquals("8887776665", persistedProfile.getEmergencyContactPhone());
        assertEquals("789 Health Boulevard, Chennai", persistedProfile.getAddress());
    }

    @Test
    void test4_PreserveHealthIdAfterUpdate() {
        PatientProfileUpdateRequestDto updateDto = new PatientProfileUpdateRequestDto(
                "New Name",
                "9999999999",
                LocalDate.of(1992, 3, 10),
                "OTHER",
                "AB+",
                "8888888888",
                "Some Address"
        );

        PatientProfileResponseDto response = patientProfileService.updatePatientProfile(testUser.getUserId(), updateDto);

        assertEquals(healthId, response.getHealthId(), "Health ID in response must match original Health ID");

        PatientProfile persistedProfile = patientProfileRepository.findByUser_UserId(testUser.getUserId()).orElseThrow();
        assertEquals(healthId, persistedProfile.getHealthId(), "Health ID in DB must not be modified by update");
    }

    @Test
    void test5_PreserveEmailAfterUpdate() {
        String originalEmail = testUser.getEmail();

        PatientProfileUpdateRequestDto updateDto = new PatientProfileUpdateRequestDto(
                "Another Name",
                "9876543211",
                LocalDate.of(1991, 1, 1),
                "MALE",
                "O-",
                "9876543212",
                "Another Address"
        );

        PatientProfileResponseDto response = patientProfileService.updatePatientProfile(testUser.getUserId(), updateDto);

        assertEquals(originalEmail, response.getEmail(), "Email in response must match original email");

        User persistedUser = userRepository.findById(testUser.getUserId()).orElseThrow();
        assertEquals(originalEmail, persistedUser.getEmail(), "Email in DB must not be modified by update");
    }

    @Test
    void test6_PreserveRoleAndAccountStatus() {
        LocalDateTime originalUserCreatedAt = testUser.getCreatedAt();
        String originalPasswordHash = testUser.getPasswordHash();

        PatientProfileUpdateRequestDto updateDto = new PatientProfileUpdateRequestDto(
                "Role Check Name",
                "9876500000",
                LocalDate.of(1994, 6, 12),
                "FEMALE",
                "A-",
                "9876500001",
                "Role Check Address"
        );

        PatientProfileResponseDto response = patientProfileService.updatePatientProfile(testUser.getUserId(), updateDto);

        assertEquals("ACTIVE", response.getAccountStatus());

        User persistedUser = userRepository.findById(testUser.getUserId()).orElseThrow();
        assertEquals("PATIENT", persistedUser.getRole().getRoleName());
        assertEquals(AccountStatus.ACTIVE, persistedUser.getAccountStatus());
        assertEquals(testUser.getUserId(), persistedUser.getUserId());
        assertEquals(originalPasswordHash, persistedUser.getPasswordHash());

        PatientProfile persistedProfile = patientProfileRepository.findByUser_UserId(testUser.getUserId()).orElseThrow();
        assertEquals(testProfile.getPatientId(), persistedProfile.getPatientId());
    }

    @Test
    void test7_RejectInvalidGender() {
        PatientProfileUpdateRequestDto updateDto = new PatientProfileUpdateRequestDto(
                "Test Name",
                "987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7),
                LocalDate.of(1990, 1, 1),
                "UNKNOWN_GENDER",
                "O+",
                "9123456780",
                "Address"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                patientProfileService.updatePatientProfile(testUser.getUserId(), updateDto));

        assertTrue(ex.getMessage().contains("Invalid gender"));
    }

    @Test
    void test8_RejectFutureDateOfBirth() {
        PatientProfileUpdateRequestDto updateDto = new PatientProfileUpdateRequestDto(
                "Test Name",
                "987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7),
                LocalDate.now().plusDays(2),
                "MALE",
                "O+",
                "9123456780",
                "Address"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                patientProfileService.updatePatientProfile(testUser.getUserId(), updateDto));

        assertTrue(ex.getMessage().contains("future"));
    }

    @Test
    void test9_EnsureAnotherUsersProfileCannotBeSelectedUsingClientSuppliedIdentifier() {
        // Create second distinct user and profile
        Role role = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));

        User user2 = new User();
        user2.setEmail("victim_patient_" + UUID.randomUUID() + "@mediverse.com");
        user2.setPasswordHash(passwordEncoder.encode("Password@123"));
        user2.setFullName("Victim Patient");
        String victimPhone = "887" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7);
        user2.setPhoneNumber(victimPhone);
        user2.setRole(role);
        user2.setAccountStatus(AccountStatus.ACTIVE);
        user2 = userRepository.save(user2);

        String healthId2 = "MV-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PatientProfile profile2 = new PatientProfile();
        profile2.setUser(user2);
        profile2.setHealthId(healthId2);
        profile2.setDateOfBirth(LocalDate.of(1985, 3, 25));
        profile2.setGender(Gender.MALE);
        profile2.setBloodGroup("O-");
        profile2.setEmergencyContactPhone("1112223335");
        profile2.setAddress("456 Victim Lane");
        profile2 = patientProfileRepository.save(profile2);

        // Update user 1's profile
        PatientProfileUpdateRequestDto updateDto = new PatientProfileUpdateRequestDto(
                "Attacker Modified Name",
                "787" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7),
                LocalDate.of(1996, 11, 11),
                "FEMALE",
                "AB-",
                "9990001113",
                "Attacker Street"
        );

        patientProfileService.updatePatientProfile(testUser.getUserId(), updateDto);

        // Verify user 2's profile was untouched
        PatientProfile reloadedProfile2 = patientProfileRepository.findByUser_UserId(user2.getUserId()).orElseThrow();
        assertEquals("Victim Patient", reloadedProfile2.getUser().getFullName());
        assertEquals(victimPhone, reloadedProfile2.getUser().getPhoneNumber());
        assertEquals("456 Victim Lane", reloadedProfile2.getAddress());
        assertEquals(healthId2, reloadedProfile2.getHealthId());

        // Verify that passing a patientId instead of userId throws ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () ->
                patientProfileService.getPatientProfile(testProfile.getPatientId()));
        assertThrows(ResourceNotFoundException.class, () ->
                patientProfileService.updatePatientProfile(testProfile.getPatientId(), updateDto));

        // Reflection check: verify update DTO exposes no client-supplied identifier fields
        List<String> prohibitedIdentifierFields = List.of("userId", "patientId", "healthId");
        Set<String> declaredFields = Arrays.stream(PatientProfileUpdateRequestDto.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        for (String fieldName : prohibitedIdentifierFields) {
            assertFalse(declaredFields.contains(fieldName),
                    "Security breach: PatientProfileUpdateRequestDto must not declare identifier field: " + fieldName);
        }
    }
}
