package com.codecat.catsurvey.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.service.RoleSerivce;
import com.codecat.catsurvey.service.UserService;
import com.codecat.catsurvey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/role")
@SaCheckLogin
@SaCheckRole("SuperAdmin")
@CrossOrigin()
public class AdminRoleController {
    @Autowired
    private RoleSerivce roleSerivce;

    @Autowired
    private UserService userService;

    @PostMapping("/{userId}")
    public Result addRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        if (!userService.existsById(userId))
            return Result.validatedFailed("用户不存在");

        roleSerivce.addByUser(userId, roleName);
        return Result.success();
    }

    @DeleteMapping("/{userId}")
    public Result delRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        if (!userService.existsById(userId))
            return Result.validatedFailed("用户不存在");

        roleSerivce.delByUser(userId, roleName);
        return Result.success();
    }

    @PutMapping("/{userId}")
    public Result setRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        if (!userService.existsById(userId))
            return Result.validatedFailed("用户不存在");

        roleSerivce.setByUser(userId, roleName);
        return Result.success();
    }

    @GetMapping("/{userId}")
    public Result getRoleByUser(@PathVariable Integer userId) {
        if (!userService.existsById(userId))
            return Result.validatedFailed("用户不存在");

        return Result.successData(roleSerivce.getAllByUser(userId));
    }
}
