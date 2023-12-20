package com.codecat.catsurvey.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.models.Role;
import com.codecat.catsurvey.models.User;
import com.codecat.catsurvey.service.UserService;
import com.codecat.catsurvey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

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

    @PutMapping("/{userId}")
    public Result modify(@PathVariable Integer userId, @RequestBody JSONObject user) {
        userService.modify(userId, user);
        return Result.success();
    }

    @PutMapping("")
    public Result modifyLoginUser(@RequestBody JSONObject user) {
        return modify(userService.getLoginId(), user);
    }

    @GetMapping("/{userId}")
    public Result get(@PathVariable Integer userId) {
        User user = userService.getById(userService.getLoginId());
        Map<String, Object> userMap = UserService.filter(user);
        userMap.put("role",
                userService.getAllRole(user.getId()).stream().map(Role::getName).collect(Collectors.toList())
        );

        return Result.successData(userMap);
    }

    @GetMapping("")
    public Result getLoginUser() {
        return Result.successData(
                get(userService.getLoginId())
        );
    }
}
