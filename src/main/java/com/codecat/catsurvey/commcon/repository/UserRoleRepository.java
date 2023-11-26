package com.codecat.catsurvey.commcon.repository;

import com.codecat.catsurvey.commcon.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    List<UserRole> findAllByUserId(Integer userId);

    void deleteAllByUserId(Integer userId);
}
