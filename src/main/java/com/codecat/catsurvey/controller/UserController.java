package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.commcon.exception.CatValidationException;
import com.codecat.catsurvey.commcon.models.Role;
import com.codecat.catsurvey.commcon.models.User;
import com.codecat.catsurvey.commcon.repository.SurveyRepository;
import com.codecat.catsurvey.commcon.repository.UserRepository;
import com.codecat.catsurvey.commcon.utils.Result;
import com.codecat.catsurvey.commcon.utils.Util;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import com.codecat.catsurvey.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@CrossOrigin()
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SurveyRepository surveyRepository;

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
            return Result.validatedFailed("尚未登录");

        StpUtil.logout();
        return Result.successMsg("注销成功");
    }

    @GetMapping("/token")
    public Result checkToken() { return Result.successData(StpUtil.getTokenTimeout()); }

    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) User user) {
        if (StpUtil.isLogin() && !userService.containsPermissionName("UserManage"))
                return Result.unauthorized("无法获取, 权限不足");

        userRepository.saveAndFlush(user);
        userService.addRole(user.getId(), "User");

        return Result.successData(user.getId());
    }

    @SaCheckLogin
    @PutMapping("/{userId}")
    public Result modify(@PathVariable Integer userId, @RequestBody JSONObject user) {
        if (user == null)
            return Result.validatedFailed("无效请求, 数据为空");

        User userOld = userRepository.findById(userId).orElseThrow(() ->
                new CatValidationException("非法userId: " + userId)
        );

        if (!userService.isLoginId(userId) && !userService.containsPermissionName("UserManage"))
            return Result.unauthorized("无法修改, 权限不足");

        Set<String> notAllow = new HashSet<>() {{
           add("id");
        }};
        Set<String> continueItem = new HashSet<>() {{
            add("userRoleList");
        }};
        Set<String> userFiled = Util.getObjectFiledName(userOld);
        Map<String, Object> userMap = Util.objectToMap(userOld);
        for (Map.Entry<String, Object> entry : user.entrySet()) {
            if (entry.getKey().equals("oldPassword") || continueItem.contains(entry.getKey()))
                continue;
            if (!userFiled.contains(entry.getKey()))
                return Result.validatedFailed("用户信息修改失败, 非法属性: " + entry.getKey());
            if (notAllow.contains(entry.getKey()))
                return Result.validatedFailed("用户信息修改失败, 属性" + entry.getKey() + "为只读");

            if (entry.getKey().equals("password")) {
                String oldPassword = (String) user.get("oldPassword");
                if (oldPassword == null)
                    return Result.validatedFailed("原密码为空");
                if (!SaSecureUtil.md5(userOld.getPassword()).equals(oldPassword))
                    return Result.validatedFailed("原密码错误");
            }

            userMap.put(entry.getKey(), entry.getValue());
        }

        User userFinal = Util.mapToObject(userMap, User.class);
        userService.checkFullUpdate(userFinal);
        userRepository.saveAndFlush(userFinal);

        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/{userId}")
    public Result get(@PathVariable Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new CatValidationException("非法userId: " + userId)
        );

        if (!userService.isLoginId(userId) && !userService.containsPermissionName("UserManage"))
            return Result.unauthorized("无法修改, 权限不足");

        Map<String, Object> userMap = UserService.filter(user);
        userMap.put("role", userService.getRoleList(user.getId()));
        return Result.successData(userMap);
    }

    @SaCheckLogin
    @RequestMapping(value = {"/{userId}/survey", "/{userId}/survey/**"})
    public void doSurvey(@PathVariable Integer userId, HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException
    {
        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 4)
            pathNeed.addAll(pathSplit.subList(4, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/survey/user/" + userId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }

    @SaCheckLogin
    @RequestMapping(value = {"/{userId}/role", "/{userId}/role/**"})
    public void doRole(@PathVariable Integer userId, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 4)
            pathNeed.addAll(pathSplit.subList(4, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/role/user/" + userId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }
}
