package com.mediverse.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Import(MethodSecurityTest.TestServiceConfiguration.class)
class MethodSecurityTest {

    @TestConfiguration
    static class TestServiceConfiguration {
        @Bean
        public TestSecuredService testSecuredService() {
            return new TestSecuredService();
        }
    }

    static class TestSecuredService {
        // Patient Permissions
        @PreAuthorize("hasAuthority('PATIENT_VIEW_OWN_PROFILE')")
        public String patientProfile() {
            return "patient-profile";
        }

        // Doctor Permissions
        @PreAuthorize("hasAuthority('DOCTOR_VIEW_AUTHORIZED_RECORD')")
        public String doctorRecord() {
            return "doctor-record";
        }

        @PreAuthorize("hasAuthority('DOCTOR_CREATE_CONSULTATION')")
        public String doctorConsultation() {
            return "doctor-consultation";
        }

        // Emergency Permissions
        @PreAuthorize("hasAuthority('EMERGENCY_VIEW_EMERGENCY_INFO')")
        public String emergencyTriage() {
            return "emergency-triage";
        }

        // Admin Permissions
        @PreAuthorize("hasAuthority('ADMIN_VERIFY_PROVIDER')")
        public String adminVerify() {
            return "admin-verify";
        }

        // Medical Organization Permissions
        @PreAuthorize("hasAuthority('MEDICAL_CREATE_ADMISSION')")
        public String medicalAdmission() {
            return "medical-admission";
        }

        // Laboratory Permissions
        @PreAuthorize("hasAuthority('LAB_CREATE_TEST_ORDER')")
        public String labOrder() {
            return "lab-order";
        }
    }

    @Autowired
    private TestSecuredService testSecuredService;

    @Test
    @DisplayName("Unauthenticated call to secured method throws AuthenticationCredentialsNotFoundException")
    void unauthenticatedCallToSecuredMethodShouldFail() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            testSecuredService.adminVerify();
        });
    }

    @Test
    @DisplayName("Authorized patient authority can access patient profile")
    @WithMockUser(authorities = {"ROLE_PATIENT", "PATIENT_VIEW_OWN_PROFILE"})
    void patientAuthorityCanAccessPatientProfile() {
        assertEquals("patient-profile", testSecuredService.patientProfile());
    }

    @Test
    @DisplayName("Patient authority cannot access doctor clinical records")
    @WithMockUser(authorities = {"ROLE_PATIENT", "PATIENT_VIEW_OWN_PROFILE"})
    void patientAuthorityCannotAccessDoctorRecord() {
        assertThrows(AccessDeniedException.class, () -> {
            testSecuredService.doctorRecord();
        });
    }

    @Test
    @DisplayName("Authorized doctor authority can access clinical record")
    @WithMockUser(authorities = {"ROLE_DOCTOR", "DOCTOR_VIEW_AUTHORIZED_RECORD"})
    void doctorAuthorityCanAccessDoctorRecord() {
        assertEquals("doctor-record", testSecuredService.doctorRecord());
    }

    @Test
    @DisplayName("Doctor authority cannot access admin operations")
    @WithMockUser(authorities = {"ROLE_DOCTOR", "DOCTOR_VIEW_AUTHORIZED_RECORD"})
    void doctorAuthorityCannotAccessAdminVerify() {
        assertThrows(AccessDeniedException.class, () -> {
            testSecuredService.adminVerify();
        });
    }

    @Test
    @DisplayName("Authorized emergency authority can access emergency triage vitals")
    @WithMockUser(authorities = {"ROLE_EMERGENCY", "EMERGENCY_VIEW_EMERGENCY_INFO"})
    void emergencyAuthorityCanAccessEmergencyTriage() {
        assertEquals("emergency-triage", testSecuredService.emergencyTriage());
    }

    @Test
    @DisplayName("Emergency authority cannot access historical clinical records")
    @WithMockUser(authorities = {"ROLE_EMERGENCY", "EMERGENCY_VIEW_EMERGENCY_INFO"})
    void emergencyAuthorityCannotAccessHistoricalClinicalRecords() {
        assertThrows(AccessDeniedException.class, () -> {
            testSecuredService.doctorRecord();
        });
    }

    @Test
    @DisplayName("Authorized admin authority can access admin verify")
    @WithMockUser(authorities = {"ROLE_ADMIN", "ADMIN_VERIFY_PROVIDER"})
    void adminAuthorityCanAccessAdminVerify() {
        assertEquals("admin-verify", testSecuredService.adminVerify());
    }

    @Test
    @DisplayName("Admin authority cannot access clinical doctor records (Security Boundary 1)")
    @WithMockUser(authorities = {"ROLE_ADMIN", "ADMIN_VERIFY_PROVIDER"})
    void adminAuthorityCannotAccessClinicalDoctorRecord() {
        assertThrows(AccessDeniedException.class, () -> {
            testSecuredService.doctorRecord();
        });
    }

    @Test
    @DisplayName("Authorized medical organization authority can access medical admission")
    @WithMockUser(authorities = {"ROLE_MEDICAL", "MEDICAL_CREATE_ADMISSION"})
    void medicalAuthorityCanAccessMedicalAdmission() {
        assertEquals("medical-admission", testSecuredService.medicalAdmission());
    }

    @Test
    @DisplayName("Authorized laboratory authority can access lab order")
    @WithMockUser(authorities = {"ROLE_LAB", "LAB_CREATE_TEST_ORDER"})
    void labAuthorityCanAccessLabOrder() {
        assertEquals("lab-order", testSecuredService.labOrder());
    }

    @Test
    @DisplayName("Missing permission authority throws AccessDeniedException even if role matches")
    @WithMockUser(authorities = {"ROLE_PATIENT"})
    void missingAuthorityThrowsAccessDeniedException() {
        assertThrows(AccessDeniedException.class, () -> {
            testSecuredService.patientProfile();
        });
    }
}
