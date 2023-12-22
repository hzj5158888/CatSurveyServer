package com.codecat.catsurvey.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.common.Enum.role.RoleNameEnum;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.models.User;
import com.codecat.catsurvey.service.UserService;
import com.codecat.catsurvey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@SaCheckLogin
@SaCheckPermission("UserManage")
@CrossOrigin()
public class AdminUserController {
    @Autowired
    private UserService userService;

    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) User user) {
        userService.add(user);
        return Result.successData(user.getId());
    }

    @DeleteMapping("/{userId}")
    public Result del(@PathVariable Integer userId) {
        List<String> roleList = userService.getAllRoleName(userId);
        if (userService.isAdmin(userId)) {
            List<String> loginRoleList = userService.getAllRoleName(userService.getLoginId());
            if (!loginRoleList.contains(RoleNameEnum.SUPER_ADMIN.getName()))
                return Result.forbidden("非超级管理员无法删除其它管理员");
            if (roleList.contains(RoleNameEnum.SUPER_ADMIN.getName()))
                return Result.forbidden("无法删除其它超级管理员");
        }

        userService.del(userId);
        return Result.success();
    }

    @PutMapping("/{userId}")
    public Result modify(@PathVariable Integer userId, @RequestBody JSONObject user) {
        List<String> roleList = userService.getAllRoleName(userId);
        if (userService.isAdmin(userId) && !userService.isLoginId(userId)) {
            List<String> loginRoleList = userService.getAllRoleName(userService.getLoginId());
            if (!loginRoleList.contains(RoleNameEnum.SUPER_ADMIN.getName()))
                return Result.forbidden("非超级管理员无法修改其它管理员");
            if (roleList.contains(RoleNameEnum.SUPER_ADMIN.getName()))
                return Result.forbidden("无法修改其它超级管理员");
        }

        userService.modify(userId, user);
        return Result.success();
    }

    @GetMapping("/{userId}")
    public Result get(@PathVariable Integer userId) {
        User user = userService.getById(userService.getLoginId());
        Map<String, Object> userMap = UserService.filter(user);
        userMap.put("role", userService.getAllRoleName(user.getId()));
        userMap.put("permission", userService.getAllPermissionName(user.getId()));

        return Result.successData(userMap);
    }

    @GetMapping("")
    public Result getAll() {
        List<User> userList = userService.getAll();

        List<Map<String, Object>> userInfoList = new ArrayList<>();
        for (User user : userList) {
            Map<String, Object> userInfo = UserService.filter(user);

            userInfo.put("role", userService.getAllRoleName(user.getId()));
            userInfo.put("permission", userService.getAllPermissionName(user.getId()));
            userInfoList.add(userInfo);
        }

        return Result.successData(userInfoList);
    }
}
