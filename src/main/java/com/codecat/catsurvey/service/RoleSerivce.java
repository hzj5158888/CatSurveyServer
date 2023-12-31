package com.codecat.catsurvey.service;

import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Permission;
import com.codecat.catsurvey.models.Role;
import com.codecat.catsurvey.models.RolePermission;
import com.codecat.catsurvey.repository.RoleRepository;
import com.codecat.catsurvey.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoleSerivce {
    @Autowired
    private RoleRepository roleRepository;

    public List<Role> getAll() {
        return roleRepository.findAll();
    }

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
