package com.codecat.catsurvey.commcon.component;

import cn.dev33.satoken.stp.StpInterface;
import com.codecat.catsurvey.commcon.models.*;
import com.codecat.catsurvey.service.RoleSerivce;
import com.codecat.catsurvey.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class StpInterfaceImpl implements StpInterface {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleSerivce roleSerivce;

    public StpInterfaceImpl() {}

    @Override
    public List<String> getPermissionList(Object loginIdObj, String loginType) {
        if (loginIdObj == null) {
            System.out.println("getPermissionList: loginId is null");
            return null;
        }

        Integer loginId = Integer.parseInt((String) loginIdObj);
        return userService.getAllPermissionName(loginId).stream().toList();
    }

    @Override
    public List<String> getRoleList(Object loginIdObj, String loginType) {
        if (loginIdObj == null) {
            System.out.println("getRoleList: loginId is null");
            return null;
        }

        Integer loginId = Integer.parseInt((String) loginIdObj);
        List<Role> RoleList = userService.getRoleList(loginId);

        Set<String> ans = new HashSet<>();
        for (Role role : RoleList) {
            ans.add(role.getName());
        }

        return ans.stream().toList();
    }
}
