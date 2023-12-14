package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.commcon.Enum.survey.SurveyStatusEnum;
import com.codecat.catsurvey.commcon.exception.ValidationException;
import com.codecat.catsurvey.commcon.models.Option;
import com.codecat.catsurvey.commcon.models.Question;
import com.codecat.catsurvey.commcon.models.Survey;
import com.codecat.catsurvey.commcon.repository.QuestionRepository;
import com.codecat.catsurvey.commcon.repository.SurveyRepository;
import com.codecat.catsurvey.commcon.repository.SurveyTemplateRepository;
import com.codecat.catsurvey.commcon.repository.UserRepository;
import com.codecat.catsurvey.commcon.utils.Result;
import com.codecat.catsurvey.commcon.utils.Util;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
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
        if (survey.getUserId() == null)
            survey.setUserId(userService.getLoginId());
        if (!userService.isLoginId(survey.getUserId()) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法添加，权限不足");

        if (!survey.getStatus().equals("草稿")) {
            if (survey.getStartDate() == null || survey.getEndDate() == null)
                return Result.validatedFailed("开始时间和截止时间不能为空");
            if (survey.getStartDate().getTime() > survey.getEndDate().getTime())
                return Result.validatedFailed("开始时间不得先于截至时间");
        }

        List<Question> questions = new ArrayList<>(survey.getQuestionList());
        surveyRepository.saveAndFlush(survey);
        if (!questions.isEmpty())
            questionService.setBySurvey(survey.getId(), questions);

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
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new ValidationException("问卷不存在")
        );
        if (!userService.isLoginId(survey.getUserId()) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法删除，权限不足");

        surveyRepository.delete(survey);
        return Result.success();
    }

    @SaCheckLogin
    @DeleteMapping("/user/{userId}/{surveyId}")
    public Result delByUser(@PathVariable Integer userId, @PathVariable Integer surveyId) {
        Survey survey = surveyRepository.findByIdAndUserId(surveyId, userId).orElseThrow(() ->
                new ValidationException("问卷不存在或不属于此用户")
        );
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法删除，权限不足");

        surveyRepository.delete(survey);
        return Result.success();
    }

    @SaCheckLogin
    @Transactional
    @PutMapping("/{surveyId}")
    public Result modify(@PathVariable Integer surveyId, @RequestBody Survey newSurvey) {
        if (newSurvey == null)
            return Result.validatedFailed("无效请求, 数据为空");

        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new ValidationException("问卷不存在")
        );
        if (!userService.isLoginId(survey.getUserId()) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法修改，权限不足");

        Set<String> notAllow = new HashSet<>() {{
            add("id");
            add("userId");
            add("createDate");
        }};
        Set<String> continueItem = new HashSet<>() {{
            add("questionList");
            add("responseList");
        }};
        Set<String> surveyFiled = Util.getObjectFiledName(survey);
        Map<String, Object> surveyMap = Util.objectToMap(survey);
        Map<String, Object> newSurveyMap = Util.objectToMap(newSurvey);
        for (Map.Entry<String, Object> entry : newSurveyMap.entrySet()) {
            if (entry.getValue() == null || continueItem.contains(entry.getKey()))
                continue;
            if (!surveyFiled.contains(entry.getKey()))
                return Result.validatedFailed("修改失败, 非法属性: " + entry.getKey());
            if (notAllow.contains(entry.getKey()))
                return Result.validatedFailed("修改失败, 属性" + entry.getKey() + "为只读");
            if (entry.getKey().equals("status") && !entry.getValue().equals("草稿")
                    || (entry.getKey().equals("startDate") || entry.getKey().equals("endDate")) )
            {
                Date startDate = (newSurvey.getStartDate() == null ? survey.getStartDate() : newSurvey.getStartDate());
                Date endDate = (newSurvey.getEndDate() == null ? survey.getEndDate() : newSurvey.getEndDate());

                if (startDate == null || endDate == null)
                    return Result.validatedFailed("开始时间和截止时间不能为空");
                if (startDate.getTime() > endDate.getTime())
                    return Result.validatedFailed("开始时间不得先于截至时间");
            }

            surveyMap.put(entry.getKey(), entry.getValue());
        }

        Survey surveyFinal = Util.mapToObject(surveyMap, Survey.class);
        List<Question> questions = new ArrayList<>(newSurvey.getQuestionList());
        surveyService.checkFullUpdate(surveyFinal);
        surveyRepository.saveAndFlush(surveyFinal);
        if (!questions.isEmpty()) // 设置questionList
            questionService.setBySurvey(surveyFinal.getId(), questions);

        return Result.success();
    }

    @SaCheckLogin
    @Transactional
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
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new ValidationException("问卷不存在")
        );

        if (!userService.isLoginId(survey.getUserId()) &&
                (!survey.getStatus().equals(SurveyStatusEnum.CARRYOUT.getName()) &&
                        !userService.containsPermissionName("SurveyManage")))
            return Result.unauthorized("无法访问，权限不足");

        return Result.successData(survey);
    }

    @SaCheckLogin
    @GetMapping("/user/{userId}/{surveyId}")
    public Result getByUser(@PathVariable Integer userId, @PathVariable Integer surveyId) {
        Survey survey = surveyRepository.findByIdAndUserId(surveyId, userId).orElseThrow(() ->
                new ValidationException("问卷不存在或不属于此用户")
        );
        if (!userService.isLoginId(survey.getUserId()) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法访问，权限不足");

        return Result.successData(survey);
    }

    @SaCheckLogin
    @GetMapping("/user/{userId}")
    public Result getAllByUser(@PathVariable Integer userId) {
        if (!userRepository.existsById(userId))
            return Result.validatedFailed("用户不存在");
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法获取, 权限不足");

        return Result.successData(
                surveyRepository.findAllByUserId(userId, Sort.by(Sort.Direction.DESC, "createDate"))
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
            throw new ValidationException("问卷不存在或不属于此用户");

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
            throw new ValidationException("问卷不存在或不属于此用户");

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
