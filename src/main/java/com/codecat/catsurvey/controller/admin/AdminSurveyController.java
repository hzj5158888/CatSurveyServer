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
        if (surveyService.isTemplate(surveyId))
            return Result.validatedFailed("无法删除模板, 权限不足");

        surveyService.del(surveyId);
        return Result.success();
    }

    @DeleteMapping("")
    public Result del(@RequestBody JSONObject delSurvey) {
        if (delSurvey.entrySet().isEmpty()) {
            surveyService.deleteAllByUserId(userService.getLoginId());
            return Result.success();
        }

        Object surveyIdListObj = delSurvey.get("surveyIdList");
        if (!(surveyIdListObj instanceof List<?>))
            throw new CatValidationException("类型错误");

        List<Integer> surveyIdList = (List<Integer>) surveyIdListObj;
        for (Integer curId : surveyIdList) {
            surveyService.del(curId);
        }

        return Result.success();
    }

    @PutMapping("/{surveyId}")
    public Result modify(@PathVariable Integer surveyId, @RequestBody Survey newSurvey) {
        if (surveyService.isTemplate(surveyId))
            return Result.validatedFailed("无法修改模板");

        surveyService.modify(surveyId, newSurvey);
        return Result.success();
    }

    @GetMapping("/{surveyId}")
    public Result get(@PathVariable Integer surveyId) {
        return Result.successData(surveyService.get(surveyId));
    }

    @GetMapping("")
    public Result getAll() { return Result.successData(surveyService.getAll()); }
}
