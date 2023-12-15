package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.common.Enum.survey.SurveyStatusEnum;
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
public class SurveyController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private QuestionService questionService;

    @SaCheckLogin
    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) Survey survey) {
        surveyService.add(survey);
        return Result.successData(survey.getId());
    }

    @SaCheckLogin
    @PostMapping("/user/{userId}")
    public Result addByUser(@PathVariable Integer userId,
                            @RequestBody @Validated(validationTime.Add.class) Survey survey)
    {
        if (!userRepository.existsById(userId))
            return Result.validatedFailed("用户不存在");

        survey.setUserId(userId);
        return this.add(survey);
    }

    @SaCheckLogin
    @DeleteMapping("/{surveyId}")
    public Result del(@PathVariable Integer surveyId) {
        surveyService.del(surveyId);
        return Result.success();
    }

    @SaCheckLogin
    @DeleteMapping("")
    public Result del(@RequestBody JSONObject delSurvey) {
        surveyService.delAll(delSurvey);
        return Result.success();
    }

    @SaCheckLogin
    @DeleteMapping("/user/{userId}/{surveyId}")
    public Result delByUser(@PathVariable Integer userId, @PathVariable Integer surveyId) {
        surveyService.del(userId, surveyId);
        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/{surveyId}")
    public Result modify(@PathVariable Integer surveyId, @RequestBody Survey newSurvey) {
        surveyService.modify(surveyId, newSurvey);
        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/user/{userId}/{surveyId}")
    public Result modifyByUser(@PathVariable Integer userId,
                               @PathVariable Integer surveyId,
                                @RequestBody Survey newSurvey)
    {
        if (!surveyRepository.existsByIdAndUserId(surveyId, userId))
            return Result.validatedFailed("问卷不存在或不属于此用户");

        return modify(surveyId, newSurvey);
    }

    @GetMapping("/{surveyId}")
    public Result get(@PathVariable Integer surveyId) {
        return Result.successData(surveyService.get(surveyId));
    }

    @SaCheckLogin
    @GetMapping("/user/{userId}/{surveyId}")
    public Result getByUser(@PathVariable Integer userId, @PathVariable Integer surveyId) {
        return Result.successData(surveyService.getByUser(userId, surveyId));
    }

    @SaCheckLogin
    @GetMapping("/user/{userId}")
    public Result getAllByUser(@PathVariable Integer userId) {
        return Result.successData(surveyService.getAllByUser(userId));
    }

    @SaCheckLogin
    @GetMapping("")
    public Result getAllByLoginUser() {
        return Result.successData(
                surveyService.getAllByUser(userService.getLoginId())
        );
    }

    @RequestMapping(value = {"/{surveyId}/question", "/{surveyId}/question/**"})
    public void doQuestion(@PathVariable Integer surveyId, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 4)
            pathNeed.addAll(pathSplit.subList(4, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/question/survey/" + surveyId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }

    @RequestMapping(value = {"/user/{userId}/{surveyId}/question", "/user/{userId}/{surveyId}/question/**"})
    public void doQuestionByUser(@PathVariable Integer userId,
                                 @PathVariable Integer surveyId,
                                 HttpServletRequest req,
                                 HttpServletResponse resp)
            throws ServletException, IOException
    {
        if (!surveyRepository.existsByIdAndUserId(surveyId, userId))
            throw new CatValidationException("问卷不存在或不属于此用户");

        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 6)
            pathNeed.addAll(pathSplit.subList(6, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/question/survey/" + surveyId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }

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

    @RequestMapping(value = {"/user/{userId}/{surveyId}/response", "/user/{userId}/{surveyId}/response/**"})
    public void doResponseByUser(@PathVariable Integer userId,
                                 @PathVariable Integer surveyId,
                                 HttpServletRequest req,
                                 HttpServletResponse resp)
            throws ServletException, IOException
    {
        if (!surveyRepository.existsByIdAndUserId(surveyId, userId))
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
