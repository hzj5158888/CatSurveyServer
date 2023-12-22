package com.codecat.catsurvey.controller.front;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.models.User;
import com.codecat.catsurvey.bean.LoginInfo;
import com.codecat.catsurvey.service.LoginService;
import com.codecat.catsurvey.service.UserService;
import com.codecat.catsurvey.utils.MD5Util;
import com.codecat.catsurvey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin()
public class LoginController {
    @Autowired
    private UserService userService;

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Result login(@RequestBody JSONObject loginInfo) {
        String userName = (String) loginInfo.get("userName");
        String password = (String) loginInfo.get("password");
        if (userName == null || password == null)
            return Result.validatedFailed("用户名或密码不能为空");

        password = MD5Util.getMD5(password);
        User user = loginService.login(userName, password);
        if (user == null || !userName.equals(user.getUserName()) || !password.equals(user.getPassword()))
            return Result.forbidden("用户名或密码错误");

        // 登陆成功,发放token SaToken
        StpUtil.login(user.getId());
        SaTokenInfo saTokenInfo = StpUtil.getTokenInfo();
        String accessToken = saTokenInfo.getTokenValue();
        List<String> roles = userService.getAllRoleName(user.getId());
        List<String> permissions = userService.getAllPermissionName(user.getId()).stream().toList();

        LoginInfo userInfo = new LoginInfo(user.getId(), userName, roles, permissions, accessToken);
        return Result.successData(userInfo);
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
