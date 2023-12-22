package com.codecat.catsurvey.controller.front;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
@RequestMapping("/survey")
@CrossOrigin()
@SaCheckLogin
public class SurveyController {
    @Autowired
    private UserService userService;

    @Autowired
    private SurveyService surveyService;

    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) Survey survey) {
        if (survey.getUserId() == null)
            survey.setUserId(userService.getLoginId());
        if (!userService.isLoginId(survey.getUserId()))
            return Result.forbidden("无法添加到其它用户");

        surveyService.add(survey);
        return Result.successData(survey.getId());
    }

    @PostMapping("/user/{userId}")
    public Result addByUser(@PathVariable Integer userId,
                            @RequestBody @Validated(validationTime.Add.class) Survey survey)
    {
        if (!userService.existsById(userId))
            return Result.forbidden("用户不存在");

        survey.setUserId(userId);
        return add(survey);
    }

    @DeleteMapping("/{surveyId}")
    public Result del(@PathVariable Integer surveyId) {
        if (!userService.isLoginId(surveyService.getUserId(surveyId)))
            return Result.forbidden("无法删除其它用户的问卷");
        if (surveyService.isTemplate(surveyId))
            return Result.validatedFailed("无法删除模板");

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
            return Result.validatedFailed("类型错误");

        List<Integer> surveyIdList = (List<Integer>) surveyIdListObj;
        for (Integer curId : surveyIdList) {
            if (!surveyService.existsByIdAndUserId(curId, userService.getLoginId()))
                return Result.forbidden("无法删除其它用户问卷");
            if (surveyService.isTemplate(curId))
                return Result.validatedFailed("无法删除模板");

            surveyService.del(curId);
        }

        return Result.success();
    }

    @DeleteMapping("/user/{userId}/{surveyId}")
    public Result delByUser(@PathVariable Integer userId, @PathVariable Integer surveyId) {
        Survey survey = surveyService.get(surveyId);
        if (!userService.isLoginId(survey.getUserId()))
            return Result.forbidden("无法删除其它用户问卷");
        if (surveyService.isTemplate(surveyId))
            return Result.validatedFailed("无法删除模板");

        surveyService.del(userId, surveyId);
        return Result.success();
    }

    @PutMapping("/{surveyId}")
    public Result modify(@PathVariable Integer surveyId, @RequestBody Survey newSurvey) {
        if (!userService.isLoginId(surveyService.getUserId(surveyId)))
            return Result.forbidden("无法修改其它用户问卷");
        if (surveyService.isTemplate(surveyId))
            return Result.validatedFailed("无法修改模板");

        surveyService.modify(surveyId, newSurvey);
        return Result.success();
    }

    @PutMapping("/user/{userId}/{surveyId}")
    public Result modifyByUser(@PathVariable Integer userId,
                               @PathVariable Integer surveyId,
                                @RequestBody Survey newSurvey)
    {
        if (!surveyService.existsByIdAndUserId(surveyId, userId))
            return Result.validatedFailed("问卷不存在或不属于此用户");

        return modify(surveyId, newSurvey);
    }

    @SaIgnore
    @GetMapping("/{surveyId}")
    public Result get(@PathVariable Integer surveyId) {
        if (!userService.isLoginId(surveyService.getUserId(surveyId))
            && !surveyService.getStatus(surveyId).equals(SurveyStatusEnum.CARRYOUT.getName()))
            return Result.forbidden("无法获取其它用户的非进行中问卷");

        Survey survey = surveyService.get(surveyId);
        if (!userService.isLoginId(surveyService.getUserId(surveyId))) {
            survey.setResponseList(new ArrayList<>());
            for (int i = 0; i < survey.getQuestionList().size(); i++)
                survey.getQuestionList().get(i).setAnswerDetailList(new ArrayList<>());
        }

        return Result.successData(survey);
    }

    @GetMapping("/user/{userId}/{surveyId}")
    public Result getByUser(@PathVariable Integer userId, @PathVariable Integer surveyId) {
        if (!userService.isLoginId(userId))
            return Result.forbidden("无法获取其它用户的问卷");

        return Result.successData(surveyService.getByUser(userId, surveyId));
    }


    @GetMapping("/user/{userId}")
    public Result getAllByUser(@PathVariable Integer userId) {
        if (!userService.isLoginId(userId))
            return Result.forbidden("无法获取其它用户的问卷");

        return Result.successData(surveyService.getAllByUser(userId));
    }


    @GetMapping("")
    public Result getAllByLoginUser() {
        return Result.successData(
                surveyService.getAllByUser(userService.getLoginId())
        );
    }

    @SaIgnore
    @RequestMapping(value = {"/{surveyId}/response", "/{surveyId}/response/**"})
    public void doResponse(@PathVariable Integer surveyId,
                           HttpServletRequest req,
                           HttpServletResponse resp)
            throws ServletException, IOException
    {
        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 4)
            pathNeed.addAll(pathSplit.subList(4, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/response/survey/" + surveyId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }

    @SaIgnore
    @RequestMapping(value = {"/user/{userId}/{surveyId}/response", "/user/{userId}/{surveyId}/response/**"})
    public void doResponseByUser(@PathVariable Integer userId,
                                 @PathVariable Integer surveyId,
                                 HttpServletRequest req,
                                 HttpServletResponse resp)
            throws ServletException, IOException
    {
        if (!surveyService.existsByIdAndUserId(surveyId, userId))
            throw new CatValidationException("问卷不存在或不属于此用户");

        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 6)
            pathNeed.addAll(pathSplit.subList(6, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/response/survey/" + surveyId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }
}
