package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.AnswerDetail;
import com.codecat.catsurvey.models.Response;
import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.repository.ResponseRepository;
import com.codecat.catsurvey.repository.SurveyRepository;
import com.codecat.catsurvey.service.ResponseService;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.service.AnswerDetailService;
import com.codecat.catsurvey.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/response")
@CrossOrigin()
public class ResponseController {

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ResponseService responseService;

    @Transactional
    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) Response response) {
        return Result.successData(responseService.add(response));
    }

    @Transactional
    @PostMapping("/survey/{surveyId}")
    public Result addBySurvey(@PathVariable Integer surveyId,
                              @RequestBody @Validated(validationTime.Add.class) Response response)
    {
        response.setSurveyId(surveyId);
        return add(response);
    }

    @SaCheckLogin
    @DeleteMapping("/{responseId}")
    public Result del(@PathVariable Integer responseId) {
        responseService.del(responseId);
        return Result.success();
    }

    @SaCheckLogin
    @DeleteMapping("/survey/{surveyId}/{responseId}")
    public Result delBySurvey(@PathVariable Integer surveyId, @PathVariable Integer responseId) {
        responseService.delBySurvey(surveyId, responseId);
        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/{responseId}")
    public Result get(@PathVariable Integer responseId) {
        return Result.successData(responseService.get(responseId));
    }

    @SaCheckLogin
    @GetMapping("/survey/{surveyId}/{responseId}")
    public Result getBySurvey(@PathVariable Integer surveyId, @PathVariable Integer responseId) {
        return Result.successData(responseService.getBySurvey(surveyId, responseId));
    }

    @SaCheckLogin
    @GetMapping("/survey/{surveyId}")
    public Result getAllBySurvey(@PathVariable Integer surveyId) {
        return Result.successData(responseService.getAllBySurvey(surveyId));
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
        if (!responseService.existsByIdAndSurveyId(responseId, surveyId))
            throw new CatValidationException("答卷不存在或不属于该问卷");

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
