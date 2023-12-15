package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.repository.UserRepository;
import com.codecat.catsurvey.service.RoleSerivce;
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

    @Autowired
    private RoleSerivce roleSerivce;

    @SaCheckLogin
    @SaCheckRole("SuperAdmin")
    @PostMapping("/user/{userId}")
    public Result addRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        roleSerivce.addByUser(userId, roleName);
        return Result.success();
    }

    @SaCheckLogin
    @SaCheckRole("SuperAdmin")
    @DeleteMapping("/user/{userId}")
    public Result delRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        roleSerivce.delByUser(userId, roleName);
        return Result.success();
    }

    @SaCheckLogin
    @SaCheckRole("SuperAdmin")
    @PutMapping("/user/{userId}")
    public Result setRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        roleSerivce.setByUser(userId, roleName);
        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/user/{userId}")
    public Result getRoleByUser(@PathVariable Integer userId) {
        return Result.successData(roleSerivce.getAllByUser(userId));
    }
}
