package com.codecat.catsurvey.service;

import com.codecat.catsurvey.commcon.models.Permission;
import com.codecat.catsurvey.commcon.models.Role;
import com.codecat.catsurvey.commcon.models.RolePermission;
import com.codecat.catsurvey.commcon.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoleSerivce {
    @Autowired
    private RoleRepository roleRepository;

    public List<Permission> getPermissionList(Integer roleId) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty())
            return new ArrayList<>();

        List<Permission> ans = new ArrayList<>();
        List<RolePermission> rolePermissionList = roleOpt.get().getRolePermissionList();
        for (RolePermission rolePermission : rolePermissionList) {
            ans.add(rolePermission.getPermission());
        }

        return ans;
    }
}
