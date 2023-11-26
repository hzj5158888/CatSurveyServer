package com.codecat.catsurvey.commcon.repository;

import com.codecat.catsurvey.commcon.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
}
