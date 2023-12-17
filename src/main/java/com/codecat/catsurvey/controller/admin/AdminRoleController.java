package com.codecat.catsurvey.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.service.RoleSerivce;
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


    @PostMapping("/user/{userId}")
    public Result addRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        roleSerivce.addByUser(userId, roleName);
        return Result.success();
    }


    @DeleteMapping("/user/{userId}")
    public Result delRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        roleSerivce.delByUser(userId, roleName);
        return Result.success();
    }


    @PutMapping("/user/{userId}")
    public Result setRoleByUser(@PathVariable Integer userId, @RequestBody JSONObject roleName) {
        roleSerivce.setByUser(userId, roleName);
        return Result.success();
    }

    @GetMapping("/user/{userId}")
    public Result getRoleByUser(@PathVariable Integer userId) {
        return Result.successData(roleSerivce.getAllByUser(userId));
    }
}
