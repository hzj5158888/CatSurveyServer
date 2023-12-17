package com.codecat.catsurvey.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.common.Enum.survey.SurveyStatusEnum;
import com.codecat.catsurvey.exception.CatAuthorizedException;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Question;
import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.repository.SurveyRepository;
import com.codecat.catsurvey.repository.UserRepository;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.utils.Util;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.service.QuestionService;
import com.codecat.catsurvey.service.SurveyService;
import com.codecat.catsurvey.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.*;

@RestController
@RequestMapping("/admin/survey")
@CrossOrigin()
@SaCheckLogin
@SaCheckPermission("SurveyManage")
public class AdminSurveyController {

    @Autowired
    private UserService userService;

    @Autowired
    private SurveyService surveyService;

    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) Survey survey) {
        surveyService.add(survey);
        return Result.successData(survey.getId());
    }

    @DeleteMapping("/{surveyId}")
    public Result del(@PathVariable Integer surveyId) {
        surveyService.del(surveyId);
        return Result.success();
    }

    @DeleteMapping("")
    public Result del(@RequestBody JSONObject delSurvey) {
        surveyService.delAll(delSurvey);
        return Result.success();
    }

    @DeleteMapping("/user/{userId}/{surveyId}")
    public Result delByUser(@PathVariable Integer userId, @PathVariable Integer surveyId) {
        surveyService.del(userId, surveyId);
        return Result.success();
    }

    @PutMapping("/{surveyId}")
    public Result modify(@PathVariable Integer surveyId, @RequestBody Survey newSurvey) {
        surveyService.modify(surveyId, newSurvey);
        return Result.success();
    }

    @PutMapping("/user/{userId}/{surveyId}")
    public Result modifyByUser(@PathVariable Integer userId,
                               @PathVariable Integer surveyId,
                               @RequestBody Survey newSurvey)
    {
        return modify(surveyId, newSurvey);
    }

    @SaIgnore
    @GetMapping("/{surveyId}")
    public Result get(@PathVariable Integer surveyId) {
        return Result.successData(surveyService.get(surveyId));
    }

    @GetMapping("/user/{userId}/{surveyId}")
    public Result getByUser(@PathVariable Integer userId, @PathVariable Integer surveyId) {


        return Result.successData(surveyService.getByUser(userId, surveyId));
    }


    @GetMapping("/user/{userId}")
    public Result getAllByUser(@PathVariable Integer userId) {
        return Result.successData(surveyService.getAllByUser(userId));
    }


    @GetMapping("")
    public Result getAllByLoginUser() {
        return Result.successData(
                surveyService.getAllByUser(userService.getLoginId())
        );
    }
}