package com.mediverse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.Role;
import com.mediverse.entity.RolePermission;
import com.mediverse.entity.RolePermissionId;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    List<RolePermission> findByRole(Role role);
    List<RolePermission> findByRole_RoleId(Integer roleId);
    List<RolePermission> findByRole_RoleName(String roleName);
}
