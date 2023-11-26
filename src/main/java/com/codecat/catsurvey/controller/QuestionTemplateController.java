package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.codecat.catsurvey.commcon.exception.ValidationException;
import com.codecat.catsurvey.commcon.models.Question;
import com.codecat.catsurvey.commcon.models.QuestionTemplate;
import com.codecat.catsurvey.commcon.models.Survey;
import com.codecat.catsurvey.commcon.repository.QuestionTemplateRepository;
import com.codecat.catsurvey.commcon.repository.SurveyTemplateRepository;
import com.codecat.catsurvey.commcon.utils.Result;
import com.codecat.catsurvey.commcon.utils.Util;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import com.codecat.catsurvey.service.QuestionTemplateService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/question/template")
public class QuestionTemplateController {
    @Autowired
    private QuestionTemplateRepository questionTemplateRepository;

    @Autowired
    private QuestionTemplateService questionTemplateService;

    @Autowired
    private SurveyTemplateRepository surveyTemplateRepository;

    @SaCheckLogin
    @SaCheckPermission("TemplateManage")
    @PostMapping("/survey/{surveyTemplateId}")
    public Result addBySurvey(@PathVariable Integer surveyTemplateId,
                              @RequestBody @Validated({validationTime.Add.class}) QuestionTemplate questionTemplate)
    {
        if (!surveyTemplateRepository.existsById(surveyTemplateId))
            return Result.validatedFailed("问卷模板不存在");

        if (questionTemplate.getIOrder() == null)
            questionTemplate.setIOrder(Integer.MAX_VALUE);

        questionTemplate.setTemplateId(surveyTemplateId);
        questionTemplateRepository.saveAndFlush(questionTemplate);
        questionTemplateService.setIOrder(questionTemplate.getId(), questionTemplate.getIOrder());

        return Result.success();
    }

    @SaCheckLogin
    @SaCheckPermission("TemplateManage")
    @DeleteMapping("/survey/{surveyTemplateId}/{questionTemplateId}")
    public Result delBySurvey(@PathVariable Integer surveyTemplateId,
                              @PathVariable Integer questionTemplateId)
    {
        if (!questionTemplateRepository.existsByIdAndTemplateId(questionTemplateId, surveyTemplateId))
            return Result.validatedFailed("问题不存在或不属于该问卷");

        questionTemplateRepository.deleteById(questionTemplateId);
        return Result.success();
    }

    @SaCheckLogin
    @SaCheckPermission("TemplateManage")
    @PutMapping("/survey/{surveyTemplateId}")
    public Result setBySurvey(@PathVariable Integer surveyTemplateId,
                              @RequestBody List<QuestionTemplate> questionTemplates)
    {
        for (int i = 0; i < questionTemplates.size(); i++) {
            QuestionTemplate questionTemplate = questionTemplates.get(i);

            questionTemplate.setIOrder(i);
            questionTemplate.setTemplateId(surveyTemplateId);
            questionTemplateService.checkFullAdd(questionTemplate);
        }

        questionTemplateRepository.deleteAllByTemplateId(surveyTemplateId);
        questionTemplateRepository.saveAllAndFlush(questionTemplates);

        return Result.successData(
                questionTemplates.stream().map(QuestionTemplate::getId).collect(Collectors.toList())
        );
    }

    @SaCheckLogin
    @SaCheckPermission("TemplateManage")
    @PutMapping("/survey/{surveyTemplateId}/{questionTemplateId}")
    public Result modifyBySurvey(@PathVariable Integer surveyTemplateId,
                                 @PathVariable Integer questionTemplateId,
                                 @RequestBody QuestionTemplate newQuestionTemplate)
    {
        if (newQuestionTemplate == null)
            return Result.validatedFailed("无效请求, 数据为空");

        QuestionTemplate questionTemplate = questionTemplateRepository.findByIdAndTemplateId(questionTemplateId, surveyTemplateId)
                .orElseThrow(() -> new ValidationException("问题不存在或不属于该问卷"));

        Set<String> notAllow = new HashSet<>() {{
            add("id");
            add("templateId");
            add("surveyTemplate");
        }};
        Set<String> continueItem = new HashSet<>() {{
            add("optionTemplateList");
        }};
        Map<String, Object> questionTemplateMap = Util.objectToMap(questionTemplate);
        Map<String, Object> newQuestionTemplateMap = Util.objectToMap(newQuestionTemplate);
        for (Map.Entry<String, Object> entry : newQuestionTemplateMap.entrySet()) {
            if (entry.getValue() == null || continueItem.contains(entry.getKey()))
                continue;
            if (notAllow.contains(entry.getKey()))
                return Result.validatedFailed("问题模板信息修改失败, 属性" + entry.getKey() + "为只读");

            questionTemplateMap.put(entry.getKey(), entry.getValue());
        }

        QuestionTemplate questionTemplateFinal = Util.mapToObject(questionTemplateMap, QuestionTemplate.class);
        questionTemplateService.checkFullUpdate(questionTemplateFinal);
        questionTemplateRepository.saveAndFlush(questionTemplateFinal);
        questionTemplateService.setIOrder(questionTemplateFinal.getId(), questionTemplateFinal.getIOrder());

        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/{questionTemplateId}")
    public Result get(@PathVariable Integer questionTemplateId) {
        QuestionTemplate questionTemplate = questionTemplateRepository.findById(questionTemplateId).orElseThrow(() ->
                new ValidationException("问题模板不存在")
        );

        return Result.successData(questionTemplate);
    }

    @SaCheckLogin
    @GetMapping("/survey/{surveyTemplateId}/{questionTemplateId}")
    public Result getBySurvey(@PathVariable Integer surveyTemplateId,
                              @PathVariable Integer questionTemplateId)
    {
        QuestionTemplate questionTemplate = questionTemplateRepository.findByIdAndTemplateId(questionTemplateId, surveyTemplateId).orElseThrow(() ->
                new ValidationException("问题模板不存在或不属于此问卷模板")
        );

        return Result.successData(questionTemplate);
    }

    @SaCheckLogin
    @GetMapping("/survey/{surveyTemplateId}")
    public Result getAllBySurvey(@PathVariable Integer surveyTemplateId) {
        return Result.successData(
                questionTemplateRepository.findAllByTemplateId(surveyTemplateId, Sort.by("iOrder"))
        );
    }

    @RequestMapping(value = {"/{questionTemplateId}/option", "/{questionTemplateId}/option/**"})
    public void doOptionTemplate(@PathVariable Integer questionTemplateId,
                                   HttpServletRequest req,
                                   HttpServletResponse resp)
            throws ServletException, IOException
    {
        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 4)
            pathNeed.addAll(pathSplit.subList(4, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/option/template/question/" + questionTemplateId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }

    @RequestMapping(value = {"/survey/{surveyTemplateId}/{questionTemplateId}/option", "/survey/{surveyTemplateId}/{questionTemplateId}/option/**"})
    public void doOptionTemplateBySurvey(@PathVariable Integer surveyTemplateId,
                                         @PathVariable Integer questionTemplateId,
                                         HttpServletRequest req,
                                         HttpServletResponse resp)
            throws ServletException, IOException
    {
        if (!questionTemplateRepository.existsByIdAndTemplateId(questionTemplateId, surveyTemplateId))
            throw new ValidationException("问题不存在或不属于此问卷");

        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 7)
            pathNeed.addAll(pathSplit.subList(7, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/option/template/question/" + questionTemplateId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        System.out.println(pre + suf);
        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }
}
