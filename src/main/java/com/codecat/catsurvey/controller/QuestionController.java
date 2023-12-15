package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Question;
import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.repository.QuestionRepository;
import com.codecat.catsurvey.repository.SurveyRepository;
import com.codecat.catsurvey.repository.UserRepository;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.service.OptionService;
import com.codecat.catsurvey.service.QuestionService;
import com.codecat.catsurvey.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/question")
@CrossOrigin()
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
        question.setSurveyId(surveyId);
        questionService.add(question);
        return Result.successData(question.getId());
    }

    @SaCheckLogin
    @DeleteMapping("/{questionId}")
    public Result del(@PathVariable Integer questionId) {
        questionService.del(questionId);
        return Result.success();
    }

    @SaCheckLogin
    @DeleteMapping("/survey/{surveyId}/{questionId}")
    public Result delBySurvey(@PathVariable Integer surveyId, @PathVariable Integer questionId) {
        questionService.delBySurvey(surveyId, questionId);
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
        questionService.modifyBySurvey(surveyId, questionId, newQuestion);
        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/{questionId}")
    public Result get(@PathVariable Integer questionId) {
        return Result.successData(questionService.get(questionId));
    }

    @SaCheckLogin
    @GetMapping("/survey/{surveyId}")
    public Result getAllBySurvey(@PathVariable Integer surveyId) {
        return Result.successData(questionService.getAllBySurvey(surveyId));
    }

    @SaCheckLogin
    @GetMapping("/survey/{surveyId}/{questionId}")
    public Result getBySurvey(@PathVariable Integer surveyId, @PathVariable Integer questionId) {
        return Result.successData(questionService.getBySurvey(surveyId, questionId));
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
        if (!questionService.existsByIdAndSurveyId(questionId, surveyId))
            throw new CatValidationException("问题不存在或不属于该问卷");

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
