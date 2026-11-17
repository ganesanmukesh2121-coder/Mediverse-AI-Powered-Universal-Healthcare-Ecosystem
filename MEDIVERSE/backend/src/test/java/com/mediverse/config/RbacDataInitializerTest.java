package com.mediverse.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.entity.Permission;
import com.mediverse.entity.Role;
import com.mediverse.entity.RolePermission;
import com.mediverse.repository.PermissionRepository;
import com.mediverse.repository.RolePermissionRepository;
import com.mediverse.repository.RoleRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class RbacDataInitializerTest {

    @Autowired
    private RbacDataInitializer rbacDataInitializer;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @BeforeEach
    void setUp() {
        rbacDataInitializer.initializeRbacData();
    }

    @Test
    @DisplayName("Verify exactly six required system roles are seeded")
    void testSixRolesAreSeeded() {
        List<String> expectedRoles = List.of("PATIENT", "EMERGENCY", "DOCTOR", "MEDICAL", "LAB", "ADMIN");

        for (String roleName : expectedRoles) {
            assertTrue(roleRepository.findByRoleName(roleName).isPresent(),
                    "Role " + roleName + " must be present in the repository");
        }

        List<Role> allRoles = roleRepository.findAll();
        Set<String> roleNames = allRoles.stream().map(Role::getRoleName).collect(Collectors.toSet());
        assertTrue(roleNames.containsAll(expectedRoles), "All 6 expected roles must be present");
    }

    @Test
    @DisplayName("Verify exactly 22 approved permissions are seeded")
    void testTwentyTwoPermissionsAreSeeded() {
        List<String> expectedPermissions = List.of(
                // Patient (6)
                "PATIENT_VIEW_OWN_PROFILE",
                "PATIENT_UPDATE_OWN_PROFILE",
                "PATIENT_UPLOAD_DOCUMENT",
                "PATIENT_MANAGE_CONSENT",
                "PATIENT_VIEW_OWN_AUDIT",
                "PATIENT_USE_AI_ASSISTANT",
                // Emergency (1)
                "EMERGENCY_VIEW_EMERGENCY_INFO",
                // Doctor (5)
                "DOCTOR_VIEW_AUTHORIZED_RECORD",
                "DOCTOR_CREATE_CONSULTATION",
                "DOCTOR_CREATE_PRESCRIPTION",
                "DOCTOR_ATTACH_DOCUMENT",
                "DOCTOR_USE_RAG_AGENT",
                // Medical Organization (4)
                "MEDICAL_CREATE_ADMISSION",
                "MEDICAL_CREATE_TREATMENT",
                "MEDICAL_CREATE_DISCHARGE",
                "MEDICAL_UPLOAD_DOCUMENT",
                // Laboratory (3)
                "LAB_CREATE_TEST_ORDER",
                "LAB_ENTER_RESULTS",
                "LAB_UPLOAD_REPORT",
                // Admin (3)
                "ADMIN_VERIFY_PROVIDER",
                "ADMIN_MANAGE_ACCOUNT",
                "ADMIN_VIEW_AUDIT"
        );

        assertEquals(22, expectedPermissions.size(), "Specification must define exactly 22 permissions");

        for (String permKey : expectedPermissions) {
            assertTrue(permissionRepository.findByPermissionKey(permKey).isPresent(),
                    "Permission " + permKey + " must be present in the repository");
        }

        List<Permission> allPermissions = permissionRepository.findAll();
        Set<String> permKeys = allPermissions.stream().map(Permission::getPermissionKey).collect(Collectors.toSet());
        assertTrue(permKeys.containsAll(expectedPermissions), "All 22 expected permissions must be present");
        assertEquals(22, allPermissions.size(), "Exactly 22 permissions should be seeded");
    }

    @Test
    @DisplayName("Verify approved role-permission mappings match 09-rbac-architecture.md")
    void testRolePermissionMappings() {
        Map<String, Set<String>> expectedMappings = Map.of(
                "PATIENT", Set.of(
                        "PATIENT_VIEW_OWN_PROFILE",
                        "PATIENT_UPDATE_OWN_PROFILE",
                        "PATIENT_UPLOAD_DOCUMENT",
                        "PATIENT_MANAGE_CONSENT",
                        "PATIENT_VIEW_OWN_AUDIT",
                        "PATIENT_USE_AI_ASSISTANT"
                ),
                "EMERGENCY", Set.of(
                        "EMERGENCY_VIEW_EMERGENCY_INFO"
                ),
                "DOCTOR", Set.of(
                        "DOCTOR_VIEW_AUTHORIZED_RECORD",
                        "DOCTOR_CREATE_CONSULTATION",
                        "DOCTOR_CREATE_PRESCRIPTION",
                        "DOCTOR_ATTACH_DOCUMENT",
                        "DOCTOR_USE_RAG_AGENT"
                ),
                "MEDICAL", Set.of(
                        "MEDICAL_CREATE_ADMISSION",
                        "MEDICAL_CREATE_TREATMENT",
                        "MEDICAL_CREATE_DISCHARGE",
                        "MEDICAL_UPLOAD_DOCUMENT"
                ),
                "LAB", Set.of(
                        "LAB_CREATE_TEST_ORDER",
                        "LAB_ENTER_RESULTS",
                        "LAB_UPLOAD_REPORT"
                ),
                "ADMIN", Set.of(
                        "ADMIN_VERIFY_PROVIDER",
                        "ADMIN_MANAGE_ACCOUNT",
                        "ADMIN_VIEW_AUDIT"
                )
        );

        for (Map.Entry<String, Set<String>> entry : expectedMappings.entrySet()) {
            String roleName = entry.getKey();
            Set<String> expectedPerms = entry.getValue();

            List<RolePermission> rolePerms = rolePermissionRepository.findByRole_RoleName(roleName);
            Set<String> actualPerms = rolePerms.stream()
                    .map(rp -> rp.getPermission().getPermissionKey())
                    .collect(Collectors.toSet());

            assertEquals(expectedPerms.size(), actualPerms.size(),
                    "Role " + roleName + " must have exactly " + expectedPerms.size() + " permissions");
            assertEquals(expectedPerms, actualPerms,
                    "Role " + roleName + " permissions do not match expected set");
        }
    }

    @Test
    @DisplayName("Verify idempotency: repeated execution does not duplicate roles, permissions, or mappings")
    void testIdempotencyRepeatedExecutionDoesNotDuplicate() {
        long roleCountBefore = roleRepository.count();
        long permCountBefore = permissionRepository.count();
        long mappingCountBefore = rolePermissionRepository.count();

        // Run second time
        rbacDataInitializer.initializeRbacData();

        assertEquals(roleCountBefore, roleRepository.count(),
                "Role count must remain identical after second initialization");
        assertEquals(permCountBefore, permissionRepository.count(),
                "Permission count must remain identical after second initialization");
        assertEquals(mappingCountBefore, rolePermissionRepository.count(),
                "Role-Permission mapping count must remain identical after second initialization");

        // Run third time
        rbacDataInitializer.initializeRbacData();

        assertEquals(roleCountBefore, roleRepository.count(),
                "Role count must remain identical after third initialization");
        assertEquals(permCountBefore, permissionRepository.count(),
                "Permission count must remain identical after third initialization");
        assertEquals(mappingCountBefore, rolePermissionRepository.count(),
                "Role-Permission mapping count must remain identical after third initialization");
    }
}
