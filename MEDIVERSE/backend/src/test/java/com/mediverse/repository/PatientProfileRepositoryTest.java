package com.mediverse.repository;

import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.Gender;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class PatientProfileRepositoryTest {

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private PatientProfile testProfile;
    private String uniqueHealthId;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.findByRoleName("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("PATIENT", "Patient role")));

        testUser = new User();
        testUser.setEmail("patient_repo_test_" + UUID.randomUUID() + "@mediverse.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setFullName("Jane Patient");
        testUser.setPhoneNumber("987" + UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 7));
        testUser.setRole(role);
        testUser.setAccountStatus(AccountStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        uniqueHealthId = "MV-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        testProfile = new PatientProfile();
        testProfile.setUser(testUser);
        testProfile.setHealthId(uniqueHealthId);
        testProfile.setDateOfBirth(LocalDate.of(1992, 4, 15));
        testProfile.setGender(Gender.FEMALE);
        testProfile.setBloodGroup("A+");
        testProfile.setEmergencyContactPhone("9123456780");
        testProfile.setAddress("456 Clinic Street, Bangalore");

        testProfile = patientProfileRepository.save(testProfile);
    }

    @Test
    void testFindByUser_UserId_Success() {
        Optional<PatientProfile> found = patientProfileRepository.findByUser_UserId(testUser.getUserId());

        assertTrue(found.isPresent());
        assertEquals(testProfile.getPatientId(), found.get().getPatientId());
        assertEquals(testUser.getUserId(), found.get().getUser().getUserId());
        assertEquals(uniqueHealthId, found.get().getHealthId());
        assertEquals("A+", found.get().getBloodGroup());
        assertEquals(Gender.FEMALE, found.get().getGender());
    }

    @Test
    void testFindByUser_UserId_NotFound() {
        String nonExistentUserId = UUID.randomUUID().toString();
        Optional<PatientProfile> found = patientProfileRepository.findByUser_UserId(nonExistentUserId);

        assertTrue(found.isEmpty());
    }

    @Test
    void testFindByHealthId_Success() {
        Optional<PatientProfile> found = patientProfileRepository.findByHealthId(uniqueHealthId);

        assertTrue(found.isPresent());
        assertEquals(testProfile.getPatientId(), found.get().getPatientId());
        assertEquals(testUser.getUserId(), found.get().getUser().getUserId());
        assertEquals(uniqueHealthId, found.get().getHealthId());
        assertEquals("456 Clinic Street, Bangalore", found.get().getAddress());
    }

    @Test
    void testFindByHealthId_NotFound() {
        String nonExistentHealthId = "MV-9999-NOTFOUND";
        Optional<PatientProfile> found = patientProfileRepository.findByHealthId(nonExistentHealthId);

        assertTrue(found.isEmpty());
    }
}
