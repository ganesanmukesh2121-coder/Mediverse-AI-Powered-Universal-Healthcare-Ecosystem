package com.mediverse.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.entity.Permission;
import com.mediverse.entity.Role;
import com.mediverse.entity.RolePermission;
import com.mediverse.entity.RolePermissionId;
import com.mediverse.repository.PermissionRepository;
import com.mediverse.repository.RolePermissionRepository;
import com.mediverse.repository.RoleRepository;

@Component
public class RbacDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RbacDataInitializer.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RbacDataInitializer(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    public void run(String... args) {
        initializeRbacData();
    }

    @Transactional
    public void initializeRbacData() {
        log.info("Starting RBAC data initialization...");

        // 1. Seed Roles
        Map<String, Role> roleMap = seedRoles();

        // 2. Seed Permissions
        Map<String, Permission> permissionMap = seedPermissions();

        // 3. Seed Role-Permission Mappings
        seedRolePermissions(roleMap, permissionMap);

        log.info("RBAC data initialization completed successfully.");
    }

    private Map<String, Role> seedRoles() {
        Map<String, String> rolesToSeed = new LinkedHashMap<>();
        rolesToSeed.put("PATIENT", "Patient user account");
        rolesToSeed.put("EMERGENCY", "Emergency responder user account");
        rolesToSeed.put("DOCTOR", "Licensed physician user account");
        rolesToSeed.put("MEDICAL", "Medical organization user account");
        rolesToSeed.put("LAB", "Diagnostic laboratory user account");
        rolesToSeed.put("ADMIN", "System administrator user account");

        Map<String, Role> roleMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : rolesToSeed.entrySet()) {
            String roleName = entry.getKey();
            String description = entry.getValue();

            Role role = roleRepository.findByRoleName(roleName).orElseGet(() -> {
                log.info("Creating role: {}", roleName);
                return roleRepository.save(new Role(roleName, description));
            });
            roleMap.put(roleName, role);
        }
        return roleMap;
    }

    private Map<String, Permission> seedPermissions() {
        Map<String, String> permissionsToSeed = new LinkedHashMap<>();

        // Patient Permissions (6)
        permissionsToSeed.put("PATIENT_VIEW_OWN_PROFILE", "View own patient profile and demographics");
        permissionsToSeed.put("PATIENT_UPDATE_OWN_PROFILE", "Update own patient profile details");
        permissionsToSeed.put("PATIENT_UPLOAD_DOCUMENT", "Upload own medical documents and records");
        permissionsToSeed.put("PATIENT_MANAGE_CONSENT", "Grant, view, and revoke healthcare provider consents");
        permissionsToSeed.put("PATIENT_VIEW_OWN_AUDIT", "View audit logs of accesses to own medical records");
        permissionsToSeed.put("PATIENT_USE_AI_ASSISTANT", "Interact with the Patient AI Assistant");

        // Emergency Permissions (1)
        permissionsToSeed.put("EMERGENCY_VIEW_EMERGENCY_INFO", "View patient emergency vitals, allergies, critical medications, and emergency contacts");

        // Doctor Permissions (5)
        permissionsToSeed.put("DOCTOR_VIEW_AUTHORIZED_RECORD", "View patient clinical records subject to active consent");
        permissionsToSeed.put("DOCTOR_CREATE_CONSULTATION", "Create consultation notes and clinical records");
        permissionsToSeed.put("DOCTOR_CREATE_PRESCRIPTION", "Create and issue electronic prescriptions");
        permissionsToSeed.put("DOCTOR_ATTACH_DOCUMENT", "Attach clinical documents to patient records");
        permissionsToSeed.put("DOCTOR_USE_RAG_AGENT", "Use the Healthcare Record RAG Agent for clinical record insights");

        // Medical Organization Permissions (4)
        permissionsToSeed.put("MEDICAL_CREATE_ADMISSION", "Create patient hospital admission records");
        permissionsToSeed.put("MEDICAL_CREATE_TREATMENT", "Record treatments and clinical procedures");
        permissionsToSeed.put("MEDICAL_CREATE_DISCHARGE", "Issue patient discharge summaries");
        permissionsToSeed.put("MEDICAL_UPLOAD_DOCUMENT", "Upload hospital and clinical documentation");

        // Laboratory Permissions (3)
        permissionsToSeed.put("LAB_CREATE_TEST_ORDER", "Create and process laboratory diagnostic test orders");
        permissionsToSeed.put("LAB_ENTER_RESULTS", "Enter laboratory test results and structured metrics");
        permissionsToSeed.put("LAB_UPLOAD_REPORT", "Upload signed diagnostic test reports");

        // Admin Permissions (3)
        permissionsToSeed.put("ADMIN_VERIFY_PROVIDER", "Verify and approve healthcare provider accounts");
        permissionsToSeed.put("ADMIN_MANAGE_ACCOUNT", "Manage user account status and administrative profiles");
        permissionsToSeed.put("ADMIN_VIEW_AUDIT", "View system-wide security audit and compliance logs");

        Map<String, Permission> permissionMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : permissionsToSeed.entrySet()) {
            String permissionKey = entry.getKey();
            String description = entry.getValue();

            Permission permission = permissionRepository.findByPermissionKey(permissionKey).orElseGet(() -> {
                log.info("Creating permission: {}", permissionKey);
                return permissionRepository.save(new Permission(permissionKey, description));
            });
            permissionMap.put(permissionKey, permission);
        }
        return permissionMap;
    }

    private void seedRolePermissions(Map<String, Role> roleMap, Map<String, Permission> permissionMap) {
        Map<String, List<String>> rolePermissionMap = new LinkedHashMap<>();

        rolePermissionMap.put("PATIENT", Arrays.asList(
                "PATIENT_VIEW_OWN_PROFILE",
                "PATIENT_UPDATE_OWN_PROFILE",
                "PATIENT_UPLOAD_DOCUMENT",
                "PATIENT_MANAGE_CONSENT",
                "PATIENT_VIEW_OWN_AUDIT",
                "PATIENT_USE_AI_ASSISTANT"
        ));

        rolePermissionMap.put("EMERGENCY", Collections.singletonList(
                "EMERGENCY_VIEW_EMERGENCY_INFO"
        ));

        rolePermissionMap.put("DOCTOR", Arrays.asList(
                "DOCTOR_VIEW_AUTHORIZED_RECORD",
                "DOCTOR_CREATE_CONSULTATION",
                "DOCTOR_CREATE_PRESCRIPTION",
                "DOCTOR_ATTACH_DOCUMENT",
                "DOCTOR_USE_RAG_AGENT"
        ));

        rolePermissionMap.put("MEDICAL", Arrays.asList(
                "MEDICAL_CREATE_ADMISSION",
                "MEDICAL_CREATE_TREATMENT",
                "MEDICAL_CREATE_DISCHARGE",
                "MEDICAL_UPLOAD_DOCUMENT"
        ));

        rolePermissionMap.put("LAB", Arrays.asList(
                "LAB_CREATE_TEST_ORDER",
                "LAB_ENTER_RESULTS",
                "LAB_UPLOAD_REPORT"
        ));

        rolePermissionMap.put("ADMIN", Arrays.asList(
                "ADMIN_VERIFY_PROVIDER",
                "ADMIN_MANAGE_ACCOUNT",
                "ADMIN_VIEW_AUDIT"
        ));

        for (Map.Entry<String, List<String>> entry : rolePermissionMap.entrySet()) {
            String roleName = entry.getKey();
            Role role = roleMap.get(roleName);
            if (role == null) {
                continue;
            }

            for (String permKey : entry.getValue()) {
                Permission permission = permissionMap.get(permKey);
                if (permission == null) {
                    continue;
                }

                RolePermissionId rpId = new RolePermissionId(role.getRoleId(), permission.getPermissionId());
                if (!rolePermissionRepository.existsById(rpId)) {
                    log.info("Mapping role {} to permission {}", roleName, permKey);
                    RolePermission rp = new RolePermission(role, permission);
                    rolePermissionRepository.save(rp);
                }
            }
        }
    }
}
