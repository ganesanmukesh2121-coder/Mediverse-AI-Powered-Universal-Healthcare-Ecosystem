package com.mediverse.repository;

import com.mediverse.entity.Permission;
import com.mediverse.entity.Role;
import com.mediverse.entity.RolePermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class RbacRepositoryTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        String roleName = "TEST_ROLE_" + UUID.randomUUID().toString().substring(0, 8);
        testRole = roleRepository.save(new Role(roleName, "Test role description"));

        String permKey = "TEST_PERM_" + UUID.randomUUID().toString().substring(0, 8);
        testPermission = permissionRepository.save(new Permission(permKey, "Test permission description"));
    }

    @Test
    void testPermissionRepositoryFindByPermissionKey() {
        Optional<Permission> foundPerm = permissionRepository.findByPermissionKey(testPermission.getPermissionKey());
        assertTrue(foundPerm.isPresent());
        assertEquals(testPermission.getPermissionKey(), foundPerm.get().getPermissionKey());
        assertEquals(testPermission.getDescription(), foundPerm.get().getDescription());
    }

    @Test
    void testRolePermissionRepositoryMappingAndQueries() {
        RolePermission rolePermission = new RolePermission(testRole, testPermission);
        rolePermissionRepository.save(rolePermission);

        List<RolePermission> byRole = rolePermissionRepository.findByRole(testRole);
        assertFalse(byRole.isEmpty());
        assertEquals(testRole.getRoleId(), byRole.get(0).getRole().getRoleId());
        assertEquals(testPermission.getPermissionId(), byRole.get(0).getPermission().getPermissionId());

        List<RolePermission> byRoleId = rolePermissionRepository.findByRole_RoleId(testRole.getRoleId());
        assertFalse(byRoleId.isEmpty());

        List<RolePermission> byRoleName = rolePermissionRepository.findByRole_RoleName(testRole.getRoleName());
        assertFalse(byRoleName.isEmpty());
    }
}
