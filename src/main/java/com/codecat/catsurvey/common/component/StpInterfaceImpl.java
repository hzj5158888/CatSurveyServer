package com.codecat.catsurvey.common.component;

import cn.dev33.satoken.stp.StpInterface;
import com.codecat.catsurvey.models.*;
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
            System.out.println("getAllRole: loginId is null");
            return null;
        }

        Integer loginId = Integer.parseInt((String) loginIdObj);
        return userService.getAllRoleName(loginId);
    }
}
