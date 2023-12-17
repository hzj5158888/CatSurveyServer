package com.codecat.catsurvey.controller.front;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.models.Role;
import com.codecat.catsurvey.service.UserService;
import com.codecat.catsurvey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin()
public class LoginController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody JSONObject loginInfo) {
        String userName = (String) loginInfo.get("userName");
        String password = (String) loginInfo.get("password");
        if (userName == null || password == null)
            return Result.failedMsg("用户名和密码不能为空");

        SaTokenInfo saTokenInfo = userService.getToken(userName, password);
        if (saTokenInfo == null)
            return Result.validatedFailed("用户名或密码错误");

        Integer loginId = Integer.parseInt( (String) saTokenInfo.getLoginId());

        Map<String, Object> loginRes = new HashMap<>();
        loginRes.put("accessToken", saTokenInfo.getTokenValue());
        loginRes.put("userId", loginId);
        loginRes.put("userName", userName);
        loginRes.put("role",
                userService.getRoleList(loginId).stream().map(Role::getName).collect(Collectors.toList())
        );

        return Result.successData(loginRes);
    }

    @PostMapping("/logout")
    public Result logout() {
        if (!StpUtil.isLogin())
            return Result.successMsg("尚未登录");

        StpUtil.logout();
        return Result.successMsg("注销成功");
    }

    @GetMapping("/token")
    public Result checkToken() { return Result.successData(StpUtil.getTokenTimeout()); }
}
