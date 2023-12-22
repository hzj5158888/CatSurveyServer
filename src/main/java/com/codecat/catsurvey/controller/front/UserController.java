package com.codecat.catsurvey.controller.front;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.bean.UserPassword;
import com.codecat.catsurvey.exception.CatAuthorizedException;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Role;
import com.codecat.catsurvey.models.User;
import com.codecat.catsurvey.service.TemplateService;
import com.codecat.catsurvey.utils.MD5Util;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.common.valid.group.validationTime;
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
@SaCheckLogin
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private TemplateService templateService;

    @SaIgnore
    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) User user) {
        if (user.getId() != null)
            return Result.validatedFailed("数据传输出错，多给了id");

        user.setPassword(MD5Util.getMD5(user.getPassword())); // 密码加密
        if (userService.existsByUserName(user.getUserName())) //查询用户是否存在
            return Result.validatedFailed("用户已经存在");

        userService.add(user);
        if (user.getId() == null)
            return Result.failedMsg("注册失败！");

        //成功
        return Result.successData(user.getId());
    }

    /**
     * 修改个人信息，不包括密码
     * @param user
     * @return
     */
    @PutMapping("")
    public Result update(@RequestBody User user)
    {
        if (user.getPassword() != null)
            return Result.validatedFailed("此接口无法修改密码");

        User oldUser = userService.getById(userService.getLoginId()); // 查询当前用户

        user.setId(oldUser.getId()); // 只能修改自己
        user.setPassword(oldUser.getPassword());
        user.setUserName(oldUser.getUserName());
        if (user.getEmail() == null) // 覆盖数据
            user.setEmail(oldUser.getEmail());
        if (user.getPhone() == null)
            user.setPhone(oldUser.getPhone());
        if (user.getWechat() == null)
            user.setWechat(oldUser.getWechat());

        User ret = userService.update(user);
        if (ret == null || ret.getId() == null)
            return Result.failedMsg("修改失败");

        return Result.successMsg("修改个人信息成功");
    }

    @PutMapping("/password")
    public Result updatePassword(@RequestBody UserPassword userPassword)
    {
        User curUser = userService.getById(userService.getLoginId()); // 查询当前用户

        if (!curUser.getPassword().equals(MD5Util.getMD5(userPassword.getOldPassword())))
            return Result.forbidden("原密码错误");
        if (userPassword.getPassword() == null)
            return Result.validatedFailed("密码不能为空");

        curUser.setPassword(MD5Util.getMD5(userPassword.getPassword()));
        User ret = userService.update(curUser);
        if (ret == null || ret.getId() == null)
            return Result.failedMsg("修改密码失败");

        return Result.successMsg("修改密码成功");
    }

    @PutMapping("/{userId}")
    public Result modify(@PathVariable Integer userId, @RequestBody JSONObject user) {
        if (!userService.isLoginId(userId))
            return Result.forbidden("无法修改其它用户的信息");

        if (user.get("password") != null) {
            String oldPassword = (String) user.get("oldPassword");
            if (oldPassword == null)
                return Result.validatedFailed("原密码为空");
            if (!userService.getPassword(userId).equals(MD5Util.getMD5(oldPassword)))
                return Result.forbidden("原密码错误");
        }

        userService.modify(userId, user);
        return Result.success();
    }

    /**
     * 获取登陆用户个人信息
     * @return
     */
    @GetMapping("")
    public Result getLoginUser() {
        User user = userService.getById(userService.getLoginId());

        Map<String, Object> userMap = UserService.filter(user);
        userMap.put("role", userService.getAllRoleName(user.getId()));
        userMap.put("permission", userService.getAllPermission(user.getId()));

        return Result.successData(userMap);
    }

    @GetMapping("/template")
    public Result getAllTemplate() {
        return Result.successData(
                templateService.filter(templateService.getAll())
        );
    }

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

    /*
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
    }*/
}
