package com.codecat.catsurvey.commcon.repository;

import com.codecat.catsurvey.commcon.models.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer> {
    List<RolePermission> findAllByPermissionId(Integer permissionId);

    List<RolePermission> findAllByRoleId(Integer roleId);
}
