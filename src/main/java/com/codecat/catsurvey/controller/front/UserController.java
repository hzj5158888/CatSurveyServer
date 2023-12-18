package com.codecat.catsurvey.controller.front;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.exception.CatAuthorizedException;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Role;
import com.codecat.catsurvey.models.User;
import com.codecat.catsurvey.repository.SurveyRepository;
import com.codecat.catsurvey.repository.UserRepository;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.utils.Util;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.sql.In;
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

    @SaIgnore
    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) User user) {
        if (user.getId()!=null)
            return Result.validatedFailed("数据传输出错，多给了id");
        userService.add(user);
        if(user.getId()!=null)
            return Result.failedMsg("注册失败！");
        //成功
        return Result.successData(user.getId());
    }

    @PutMapping("")
    public Result update(@RequestBody User user) {
        if(user.getId()!=null && StpUtil.getLoginIdAsInt() != user.getId()){
            return Result.failedMsg("不能修改别人的信息");
        }
        User ret = userService.update(user);
        if(ret==null)
            return Result.failedMsg("修改失败");
        return Result.successMsg("修改个人信息成功");
    }

    @PutMapping("/{userId}")
    public Result modify(@PathVariable Integer userId, @RequestBody JSONObject user) {
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法修改, 权限不足");

        userService.modify(userId, user);
        return Result.success();
    }

    @GetMapping("")
    public Result getLoginUser() {
        User user = userService.getById(userService.getLoginId());
        Map<String, Object> userMap = UserService.filter(user);
        userMap.put("role",
                userService.getRoleList(user.getId())
                            .stream()
                            .map(Role::getName)
                            .collect(Collectors.toList())
        );

        return Result.successData(userMap);
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
