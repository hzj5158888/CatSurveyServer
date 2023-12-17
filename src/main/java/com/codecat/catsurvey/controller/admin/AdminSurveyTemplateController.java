package com.codecat.catsurvey.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.SurveyTemplate;
import com.codecat.catsurvey.repository.SurveyTemplateRepository;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.utils.Util;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.service.SurveyTemplateService;
import com.codecat.catsurvey.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/survey/template")
public class AdminSurveyTemplateController {
    @Autowired
    private SurveyTemplateRepository surveyTemplateRepository;

    @Autowired
    private SurveyTemplateService surveyTemplateService;

    @Autowired
    private UserService userService;

    @SaCheckLogin
    @SaCheckPermission("TemplateManage")
    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) SurveyTemplate surveyTemplate) {
        surveyTemplate.setStatus("草稿");
        surveyTemplate.setUserId(userService.getLoginId());
        surveyTemplateRepository.saveAndFlush(surveyTemplate);
        return Result.successData(surveyTemplate.getId());
    }

    @SaCheckLogin
    @SaCheckPermission("TemplateManage")
    @DeleteMapping("/{surveyTemplateId}")
    public Result del(@PathVariable Integer surveyTemplateId) {
        if (!surveyTemplateRepository.existsById(surveyTemplateId))
            return Result.validatedFailed("问卷模板不存在");

        surveyTemplateRepository.deleteById(surveyTemplateId);
        return Result.success();
    }

    @SaCheckLogin
    @SaCheckPermission("TemplateManage")
    @PutMapping("/{surveyTemplateId}")
    public Result modify(@PathVariable Integer surveyTemplateId, @RequestBody SurveyTemplate newSurveyTemplate) {
        if (newSurveyTemplate == null)
            return Result.validatedFailed("无效请求, 数据为空");

        SurveyTemplate surveyTemplate = surveyTemplateRepository.findById(surveyTemplateId).orElseThrow(() ->
                new CatValidationException("问卷模板不存在")
        );

        Set<String> notAllow = new HashSet<>() {{
            add("id");
            add("userId");
            add("status");
        }};
        Set<String> continueItem = new HashSet<>() {{
            add("questionTemplateList");
        }};
        Map<String, Object> surveyTemplateMap = Util.objectToMap(surveyTemplate);
        Map<String, Object> newSurveyTemplateMap = Util.objectToMap(newSurveyTemplate);
        for (Map.Entry<String, Object> entry : newSurveyTemplateMap.entrySet()) {
            if (entry.getValue() == null || continueItem.contains(entry.getKey()))
                continue;
            if (notAllow.contains(entry.getKey()))
                return Result.validatedFailed("修改失败, 属性" + entry.getKey() + "为只读");

            surveyTemplateMap.put(entry.getKey(), entry.getValue());
        }

        SurveyTemplate surveyTemplateFinal = Util.mapToObject(surveyTemplateMap, SurveyTemplate.class);
        surveyTemplateService.checkFullUpdate(surveyTemplateFinal);
        surveyTemplateRepository.saveAndFlush(surveyTemplateFinal);

        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/{surveyTemplateId}")
    public Result get(@PathVariable Integer surveyTemplateId) {
        SurveyTemplate surveyTemplate = surveyTemplateRepository.findById(surveyTemplateId).orElseThrow(() ->
                new CatValidationException("问卷模板不存在")
        );

        return Result.successData(surveyTemplate);
    }

    @SaCheckLogin
    @GetMapping("")
    public Result getAll() {
        return Result.successData(surveyTemplateRepository.findAll());
    }

    @RequestMapping(value = {"/{surveyTemplateId}/question", "/{surveyTemplateId}/question/**"})
    public void doQuestionTemplate(@PathVariable Integer surveyTemplateId,
                                   HttpServletRequest req,
                                   HttpServletResponse resp)
            throws ServletException, IOException
    {
        List<String> pathSplit = List.of(req.getServletPath().split("/"));
        List<String> pathNeed = new ArrayList<>();
        if (pathSplit.size() > 5)
            pathNeed.addAll(pathSplit.subList(5, pathSplit.size()));

        String suf = String.join("/", pathNeed);
        String pre = "/question/template/survey/" + surveyTemplateId + (suf.isEmpty() ? "" : "/");
        if (req.getServletPath().charAt(req.getServletPath().length() - 1) == '/')
            suf += "/";

        System.out.println(pre + suf);
        req.getRequestDispatcher(pre + suf).forward(req, resp);
    }
}
