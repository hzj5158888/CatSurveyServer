package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.codecat.catsurvey.commcon.exception.ValidationException;
import com.codecat.catsurvey.commcon.models.Question;
import com.codecat.catsurvey.commcon.models.Survey;
import com.codecat.catsurvey.commcon.models.User;
import com.codecat.catsurvey.commcon.repository.QuestionRepository;
import com.codecat.catsurvey.commcon.repository.SurveyRepository;
import com.codecat.catsurvey.commcon.repository.UserRepository;
import com.codecat.catsurvey.commcon.utils.Result;
import com.codecat.catsurvey.commcon.utils.Util;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import com.codecat.catsurvey.service.OptionService;
import com.codecat.catsurvey.service.QuestionService;
import com.codecat.catsurvey.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.relational.core.sql.In;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.Date;
import java.util.*;

@RestController
@RequestMapping("/question")
public class QuestionController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private OptionService optionService;

    @SaCheckLogin
    @PostMapping("")
    public Result add(@RequestBody @Validated({validationTime.FullAdd.class}) Question question) {
        questionService.add(question);
        return Result.successData(question.getId());
    }

    @SaCheckLogin
    @PostMapping("/survey/{surveyId}")
    public Result addBySurvey(@PathVariable Integer surveyId,
                              @RequestBody @Validated({validationTime.Add.class}) Question question)
    {
        if (!surveyRepository.existsById(surveyId))
            return Result.validatedFailed("问卷不存在");

        question.setSurveyId(surveyId);
        questionService.add(question);
        return Result.successData(question.getId());
    }

    @SaCheckLogin
    @DeleteMapping("/{questionId}")
    public Result del(@PathVariable Integer questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new ValidationException("问题不存在")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法删除，权限不足");

        questionRepository.delete(question);
        return Result.success();
    }

    @SaCheckLogin
    @DeleteMapping("/survey/{surveyId}/{questionId}")
    public Result delBySurvey(@PathVariable Integer surveyId, @PathVariable Integer questionId) {
        Question question = questionRepository.findByIdAndSurveyId(questionId, surveyId).orElseThrow(() ->
                new ValidationException("问题不存在或不属于该问卷")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法删除，权限不足");

        questionRepository.delete(question);
        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/survey/{surveyId}")
    public Result setBySurvey(@PathVariable Integer surveyId, @RequestBody List<Question> questions) {
        return Result.successData(questionService.setBySurvey(surveyId, questions));
    }

    @SaCheckLogin
    @PutMapping("/{questionId}")
    public Result modify(@PathVariable Integer questionId, @RequestBody Question newQuestion) {
        questionService.modify(questionId, newQuestion);
        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/survey/{surveyId}/{questionId}")
    public Result modifyBySurvey(@PathVariable Integer surveyId, @PathVariable Integer questionId,
                                 @RequestBody Question newQuestion)
    {
        Question question = questionRepository.findByIdAndSurveyId(questionId, surveyId).orElseThrow(() ->
                new ValidationException("问题不存在或不属于该问卷")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法修改，权限不足");

        return modify(questionId, newQuestion);
    }

    @SaCheckLogin
    @GetMapping("/{questionId}")
    public Result get(@PathVariable Integer questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new ValidationException("问题不存在")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法获取，权限不足");

        return Result.successData(question);
    }

    @SaCheckLogin
    @GetMapping("/survey/{surveyId}")
    public Result getAllBySurvey(@PathVariable Integer surveyId) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new ValidationException("问卷不存在")
        );
        if (!userService.isLoginId(survey.getUserId()) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法访问，权限不足");

        return Result.successData(
                questionRepository.findAllBySurveyId(surveyId, Sort.by("iOrder"))
        );
    }

    @SaCheckLogin
    @GetMapping("/survey/{surveyId}/{questionId}")
    public Result getBySurvey(@PathVariable Integer surveyId, @PathVariable Integer questionId) {
        Question question = questionRepository.findByIdAndSurveyId(questionId, surveyId).orElseThrow(() ->
                new ValidationException("问题不存在或不属于该问卷")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法修改，权限不足");

        return Result.successData(question);
    }

    @SaCheckLogin
    @RequestMapping(value = {"/{questionId}/option", "/{questionId}/option/**"})
    public void doOption(@PathVariable Integer questionId, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 4)
            pathNeed.addAll(pathSplit.subList(4, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/option/question/" + questionId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }

    @SaCheckLogin
    @RequestMapping(value = {"/survey/{surveyId}/{questionId}/option", "/survey/{surveyId}/{questionId}/option/**"})
    public void doOptionBySurvey(@PathVariable Integer surveyId,
                                 @PathVariable Integer questionId,
                                 HttpServletRequest req,
                                 HttpServletResponse resp)
            throws ServletException, IOException
    {
        if (!questionRepository.existsByIdAndSurveyId(questionId, surveyId))
            throw new ValidationException("问题不存在或不属于该问卷");

        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 6)
            pathNeed.addAll(pathSplit.subList(6, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/option/question/" + questionId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }
}
