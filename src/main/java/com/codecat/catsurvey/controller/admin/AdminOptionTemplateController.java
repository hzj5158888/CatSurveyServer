package com.codecat.catsurvey.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.codecat.catsurvey.common.Enum.question.QuestionTypeEnum;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.OptionTemplate;
import com.codecat.catsurvey.models.QuestionTemplate;
import com.codecat.catsurvey.repository.OptionTemplateRepository;
import com.codecat.catsurvey.repository.QuestionTemplateRepository;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.utils.Util;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.service.OptionTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin/template/option")
@SaCheckLogin
@SaCheckPermission("TemplateManage")
public class AdminOptionTemplateController {
    @Autowired
    private OptionTemplateRepository optionTemplateRepository;

    @Autowired
    private OptionTemplateService optionTemplateService;

    @Autowired
    private QuestionTemplateRepository questionTemplateRepository;

    @SaCheckLogin
    @SaCheckPermission("TemplateManage")
    @PostMapping("/question/{questionTemplateId}")
    public Result addByQuestion(@PathVariable Integer questionTemplateId,
                                @RequestBody @Validated({validationTime.Add.class}) OptionTemplate optionTemplate)
    {
        QuestionTemplate questionTemplate = questionTemplateRepository.findById(questionTemplateId).orElseThrow(() ->
                new CatValidationException("问题不存在")
        );

        if (questionTemplate.getType().equals(QuestionTypeEnum.TEXT.getName()))
            return Result.validatedFailed("无法添加选项，该问题非选择题");

        if (optionTemplate.getIOrder() == null)
            optionTemplate.setIOrder(Integer.MAX_VALUE);

        optionTemplate.setQuestionTemplateId(questionTemplateId);
        optionTemplateRepository.saveAndFlush(optionTemplate);
        optionTemplateService.setIOrder(optionTemplate.getId(), optionTemplate.getIOrder());

        return Result.successData(optionTemplate.getId());
    }

    @SaCheckLogin
    @SaCheckPermission("TemplateManage")
    @DeleteMapping("/question/{questionTemplateId}/{optionTemplateId}")
    public Result delByQuestion(@PathVariable Integer questionTemplateId, @PathVariable Integer optionTemplateId) {
        if (!optionTemplateRepository.existsByIdAndQuestionTemplateId(optionTemplateId, questionTemplateId))
            return Result.validatedFailed("选项不存在或不属于此问题");

        optionTemplateRepository.deleteById(optionTemplateId);
        return Result.success();
    }

    @SaCheckLogin
    @SaCheckPermission("TemplateManage")
    @PutMapping("/question/{questionTemplateId}")
    public Result setByQuestion(@PathVariable Integer questionTemplateId,
                                @RequestBody List<OptionTemplate> optionTemplates)
    {
        QuestionTemplate questionTemplate = questionTemplateRepository.findById(questionTemplateId).orElseThrow(() ->
                new CatValidationException("问题不存在")
        );

        if (questionTemplate.getType().equals(QuestionTypeEnum.TEXT.getName()))
            return Result.validatedFailed("无法设置选项，该问题非选择题");

        for (int i = 0; i < optionTemplates.size(); i++) {
            OptionTemplate optionTemplate = optionTemplates.get(i);

            optionTemplate.setIOrder(i);
            optionTemplate.setQuestionTemplateId(questionTemplateId);
            optionTemplateService.checkFullAdd(optionTemplate);
        }

        optionTemplateRepository.deleteAllByQuestionTemplateId(questionTemplateId);
        optionTemplateRepository.saveAllAndFlush(optionTemplates);

        return Result.success();
    }

    @SaCheckLogin
    @SaCheckPermission("TemplateManage")
    @PutMapping("/question/{questionTemplateId}/{optionTemplateId}")
    public Result modifyByQuestion(@PathVariable Integer questionTemplateId,
                                   @PathVariable Integer optionTemplateId,
                                   @RequestBody OptionTemplate newOptionTemplate)
    {
        OptionTemplate optionTemplate = optionTemplateRepository.findByIdAndQuestionTemplateId(optionTemplateId, questionTemplateId)
                .orElseThrow(() -> new CatValidationException("选项模板不存在或不属于此问题模板"));

        Set<String> notAllow = new HashSet<>(){{
            add("id");
            add("questionTemplateId");
            add("questionTemplate");
        }};
        Map<String, Object> optionTemplateMap = Util.objectToMap(optionTemplate);
        Map<String, Object> newOptionTemplateMap = Util.objectToMap(newOptionTemplate);
        for (Map.Entry<String, Object> entry : newOptionTemplateMap.entrySet()) {
            if (entry.getValue() == null)
                continue;
            if (notAllow.contains(entry.getKey()))
                return Result.validatedFailed("修改失败, 属性" + entry.getKey() + "为只读");

            optionTemplateMap.put(entry.getKey(), entry.getValue());
        }

        OptionTemplate optionTemplateFinal = Util.mapToObject(optionTemplateMap, OptionTemplate.class);
        optionTemplateService.checkFullUpdate(optionTemplateFinal);
        optionTemplateRepository.saveAndFlush(optionTemplateFinal);
        optionTemplateService.setIOrder(optionTemplateFinal.getId(), optionTemplateFinal.getIOrder());

        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/{optionTemplateId}")
    public Result get(@PathVariable Integer optionTemplateId) {
        OptionTemplate optionTemplate = optionTemplateRepository.findById(optionTemplateId).orElseThrow(() ->
                new CatValidationException("选项不存在")
        );

        return Result.successData(optionTemplate);
    }

    @SaCheckLogin
    @GetMapping("/question/{questionTemplateId}/{optionTemplateId}")
    public Result getByQuestion(@PathVariable Integer questionTemplateId, @PathVariable Integer optionTemplateId) {
        OptionTemplate optionTemplate = optionTemplateRepository.findByIdAndQuestionTemplateId(optionTemplateId, questionTemplateId)
                .orElseThrow(() -> new CatValidationException("选项不存在")
        );

        return Result.successData(optionTemplate);
    }

    @SaCheckLogin
    @GetMapping("/question/{questionTemplateId}")
    public Result getAllByQuestion(@PathVariable Integer questionTemplateId) {
        return Result.successData(optionTemplateRepository.findAllByQuestionTemplateId(questionTemplateId));
    }
}