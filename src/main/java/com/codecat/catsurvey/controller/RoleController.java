package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.repository.UserRepository;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/role")
@CrossOrigin()
public class RoleController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @SaCheckLogin
    @SaCheckRole("SuperAdmin")
    @PostMapping("/user/{userId}")
    public Result addRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        if (!userRepository.existsById(userId))
            return Result.validatedFailed("非法的userID: " + userId);
        if (roleName == null || roleName.get("roleName") == null)
            return Result.validatedFailed("非法请求，数据为空");

        List<String> roleNameList;
        if (roleName.get("roleName") instanceof List<?>)
            roleNameList = (List<String>) roleName.get("roleName");
        else {
            roleNameList = new ArrayList<>();
            roleNameList.add((String) roleName.get("roleName"));
        }

        userService.addRoleAll(userId, roleNameList);
        return Result.success();
    }

    @SaCheckLogin
    @SaCheckRole("SuperAdmin")
    @DeleteMapping("/user/{userId}")
    public Result delRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        if (!userRepository.existsById(userId))
            return Result.validatedFailed("非法的userID: " + userId);
        if (roleName == null || roleName.get("roleName") == null)
            return Result.validatedFailed("非法请求，数据为空");

        List<String> roleNameList;
        if (roleName.get("roleName") instanceof List<?>)
            roleNameList = (List<String>) roleName.get("roleName");
        else {
            roleNameList = new ArrayList<>();
            roleNameList.add((String) roleName.get("roleName"));
        }

        userService.delRoleAll(userId, roleNameList);
        return Result.success();
    }

    @SaCheckLogin
    @SaCheckRole("SuperAdmin")
    @PutMapping("/user/{userId}")
    public Result setRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        if (!userRepository.existsById(userId))
            return Result.validatedFailed("非法的userID: " + userId);
        if (roleName == null || roleName.get("roleName") == null)
            return Result.validatedFailed("非法请求，数据为空");

        List<String> roleNameList;
        if (roleName.get("roleName") instanceof List<?>)
            roleNameList = (List<String>) roleName.get("roleName");
        else {
            roleNameList = new ArrayList<>();
            roleNameList.add((String) roleName.get("roleName"));
        }

        userService.setRoleAll(userId, roleNameList);
        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/user/{userId}")
    public Result getRoleByUser(@PathVariable Integer userId) {
        if (!userRepository.existsById(userId))
            return Result.validatedFailed("非法的userID: " + userId);
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("UserManage"))
            return Result.unauthorized("无法获取, 权限不足");

        return Result.successData(userService.getRoleList(userId));
    }
}
