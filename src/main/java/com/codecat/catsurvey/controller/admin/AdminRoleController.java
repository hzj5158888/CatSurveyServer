package com.codecat.catsurvey.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.common.Enum.role.RoleNameEnum;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Role;
import com.codecat.catsurvey.service.RoleSerivce;
import com.codecat.catsurvey.service.UserService;
import com.codecat.catsurvey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/role")
@SaCheckLogin
@CrossOrigin()
public class AdminRoleController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleSerivce roleSerivce;

    @SaCheckRole("SuperAdmin")
    @PostMapping("/{userId}")
    public Result addRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleNameObj) {
        if (!userService.existsById(userId))
            return Result.validatedFailed("用户不存在");
        if (userService.isSuperAdmin(userId))
            return Result.forbidden("无法添加超级管理员的角色");
        if (!(roleNameObj.get("roleNameList") instanceof List<?>))
            return Result.validatedFailed("类型错误或数据为空");

        List<String> roleNameList = (List<String>) roleNameObj.get("roleNameList");
        for (String roleName : roleNameList) {
            if (RoleNameEnum.SUPER_ADMIN.getName().equals(roleName))
                return Result.forbidden("无法将超级管理员角色添加给此用户");
        }

        userService.addRoleAll(userId, roleNameList);
        return Result.success();
    }

    @SaCheckRole("SuperAdmin")
    @DeleteMapping("/{userId}")
    public Result delRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleNameObj) {
        if (!userService.existsById(userId))
            return Result.validatedFailed("用户不存在");
        if (userService.isSuperAdmin(userId))
            return Result.forbidden("无法删除超级管理员的角色");
        if (!(roleNameObj.get("roleNameList") instanceof List<?>))
            return Result.validatedFailed("类型错误或数据为空");

        List<String> roleNameList = (List<String>) roleNameObj.get("roleNameList");
        userService.delRoleAll(userId, roleNameList);
        return Result.success();
    }

    @SaCheckRole("SuperAdmin")
    @PutMapping("/{userId}")
    public Result setRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleNameObj) {
        if (!userService.existsById(userId))
            return Result.validatedFailed("用户不存在");
        if (userService.isSuperAdmin(userId))
            return Result.forbidden("无法设置超级管理员的角色");
        if (!(roleNameObj.get("roleNameList") instanceof List<?>))
            return Result.validatedFailed("类型错误或数据为空");

        List<String> roleNameList = (List<String>) roleNameObj.get("roleNameList");
        for (String roleName : roleNameList) {
            if (RoleNameEnum.SUPER_ADMIN.getName().equals(roleName))
                return Result.forbidden("无法将该用户设置为超级管理员");
        }

        userService.setRoleAll(userId, roleNameList);
        return Result.success();
    }

    @SaCheckRole("SuperAdmin")
    @GetMapping("/{userId}")
    public Result getAllRoleByUser(@PathVariable Integer userId) {
        if (!userService.existsById(userId))
            return Result.validatedFailed("用户不存在");

        return Result.successData(userService.getAllRoleName(userId));
    }

    @GetMapping("")
    public Result getAll() {
        Map<String, String> ans = new HashMap<>();

        List<Role> roleList = roleSerivce.getAll();
        for (Role role : roleList) {
            ans.put(role.getName(), RoleNameEnum.getChnName(role.getName()));
        }

        return Result.successData(ans);
    }
}
