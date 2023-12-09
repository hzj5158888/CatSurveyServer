package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.codecat.catsurvey.commcon.exception.ValidationException;
import com.codecat.catsurvey.commcon.models.AnswerDetail;
import com.codecat.catsurvey.commcon.models.Response;
import com.codecat.catsurvey.commcon.models.Survey;
import com.codecat.catsurvey.commcon.repository.ResponseRepository;
import com.codecat.catsurvey.commcon.repository.SurveyRepository;
import com.codecat.catsurvey.commcon.utils.Result;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import com.codecat.catsurvey.service.AnswerDetailService;
import com.codecat.catsurvey.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/response")
public class ResponseController {
    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private AnswerDetailService answerDetailService;

    @Autowired
    private UserService userService;

    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) Response response) {
        List<AnswerDetail> answerDetails = new ArrayList<>(response.getAnswerDetailList());

        response.setUserId(userService.getLoginId());
        responseRepository.saveAndFlush(response);
        if (!answerDetails.isEmpty())
            answerDetailService.setByResponse(response.getId(), answerDetails);

        return Result.successData(response.getId());
    }

    @PostMapping("/survey/{surveyId}")
    public Result addBySurvey(@PathVariable Integer surveyId,
                              @RequestBody @Validated(validationTime.Add.class) Response response)
    {
        if (!surveyRepository.existsById(surveyId))
            return Result.validatedFailed("问卷不存在");

        response.setSurveyId(surveyId);
        return Result.successData(this.add(response));
    }

    @SaCheckLogin
    @DeleteMapping("/{responseId}")
    public Result del(@PathVariable Integer responseId) {
        Response response = responseRepository.findById(responseId).orElseThrow(() ->
                new ValidationException("答卷不存在")
        );

        Integer userId = response.getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法删除，权限不足");

        responseRepository.deleteById(response.getId());
        return Result.success();
    }

    @SaCheckLogin
    @DeleteMapping("/survey/{surveyId}/{responseId}")
    public Result delBySurvey(@PathVariable Integer surveyId, @PathVariable Integer responseId) {
        Response response = responseRepository.findByIdAndSurveyId(responseId, surveyId).orElseThrow(() ->
                new ValidationException("答卷不存在或不属于该问卷")
        );

        Integer userId = response.getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法删除，权限不足");

        responseRepository.deleteById(response.getId());
        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/{responseId}")
    public Result get(@PathVariable Integer responseId) {
        Response response = responseRepository.findById(responseId).orElseThrow(() ->
                new ValidationException("答卷不存在")
        );

        Integer userId = response.getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法获取，权限不足");

        return Result.successData(response);
    }

    @SaCheckLogin
    @GetMapping("/survey/{surveyId}/{responseId}")
    public Result getBySurvey(@PathVariable Integer surveyId, @PathVariable Integer responseId) {
        Response response = responseRepository.findByIdAndSurveyId(responseId, surveyId).orElseThrow(() ->
                new ValidationException("答卷不存在或不属于该问卷")
        );

        Integer userId = response.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法获取，权限不足");

        return Result.successData(response);
    }

    @SaCheckLogin
    @GetMapping("/survey/{surveyId}")
    public Result getAllBySurvey(@PathVariable Integer surveyId) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new ValidationException("问卷不存在")
        );

        Integer userId = survey.getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法获取，权限不足");

        return Result.successData(responseRepository.findAllBySurveyId(surveyId));
    }

    @RequestMapping(value = {"/{responseId}/detail", "/{responseId}/detail/**"})
    public void doDetail(@PathVariable Integer responseId, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 4)
            pathNeed.addAll(pathSplit.subList(4, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/response/detail/response/" + responseId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }

    @RequestMapping(value = {"/survey/{surveyId}/{responseId}/detail", "/survey/{surveyId}/{responseId}/detail/**"})
    public void doDetailBySurvey(@PathVariable Integer surveyId,
                                 @PathVariable Integer responseId,
                                 HttpServletRequest req,
                                 HttpServletResponse resp)
            throws ServletException, IOException
    {
        if (!responseRepository.existsByIdAndSurveyId(responseId, surveyId))
            throw new ValidationException("答卷不存在或不属于该问卷");

        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 6)
            pathNeed.addAll(pathSplit.subList(6, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/response/detail/response/" + responseId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }
}
