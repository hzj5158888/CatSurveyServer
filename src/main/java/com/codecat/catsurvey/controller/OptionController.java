package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.codecat.catsurvey.commcon.Enum.question.QuestionTypeEnum;
import com.codecat.catsurvey.commcon.exception.ValidationException;
import com.codecat.catsurvey.commcon.models.Option;
import com.codecat.catsurvey.commcon.models.Question;
import com.codecat.catsurvey.commcon.repository.OptionRepository;
import com.codecat.catsurvey.commcon.repository.QuestionRepository;
import com.codecat.catsurvey.commcon.utils.Result;
import com.codecat.catsurvey.commcon.utils.Util;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import com.codecat.catsurvey.service.OptionService;
import com.codecat.catsurvey.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/option")
public class OptionController {
    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private OptionService optionService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserService userService;

    @SaCheckLogin
    @PostMapping("")
    public Result add(@RequestBody @Validated({validationTime.FullAdd.class}) Option option) {
        Question question = questionRepository.findById(option.getQuestionId()).orElseThrow(() ->
                new ValidationException("问题不存在")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法添加，权限不足");
        if (question.getType().equals(QuestionTypeEnum.TEXT.getName()))
            return Result.validatedFailed("无法添加选项，该问题非选择题");

        if (option.getIOrder() == null)
            option.setIOrder(Integer.MAX_VALUE);

        optionRepository.saveAndFlush(option);
        optionService.setIOrder(option.getId(), option.getIOrder());

        return Result.successData(option.getId());
    }

    @SaCheckLogin
    @PostMapping("/question/{questionId}")
    public Result addByQuestion(@PathVariable Integer questionId,
                                @RequestBody @Validated({validationTime.Add.class}) Option option)
    {
        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new ValidationException("问题不存在")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法添加，权限不足");

        if (question.getType().equals(QuestionTypeEnum.TEXT.getName()))
            return Result.validatedFailed("无法添加选项，该问题非选择题");

        if (option.getIOrder() == null)
            option.setIOrder(Integer.MAX_VALUE);

        option.setQuestionId(questionId);
        optionRepository.saveAndFlush(option);
        optionService.setIOrder(option.getId(), option.getIOrder());

        return Result.successData(option.getId());
    }

    @SaCheckLogin
    @DeleteMapping("/{optionId}")
    public Result del(@PathVariable Integer optionId) {
        Option option = optionRepository.findById(optionId).orElseThrow(() ->
                new ValidationException("选项不存在")
        );

        Integer userId = option.getQuestion().getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法删除，权限不足");

        optionRepository.deleteById(optionId);
        return Result.success();
    }

    @SaCheckLogin
    @DeleteMapping("/question/{questionId}/{optionId}")
    public Result delByQuestion(@PathVariable Integer questionId, @PathVariable Integer optionId) {
        Option option = optionRepository.findByIdAndQuestionId(optionId, questionId).orElseThrow(() ->
                new ValidationException("选项不存在或不属于此问题")
        );

        Integer userId = option.getQuestion().getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法删除，权限不足");

        optionRepository.deleteById(optionId);
        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/question/{questionId}")
    public Result setByQuestion(@PathVariable Integer questionId, @RequestBody List<Option> options) {
        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new ValidationException("问题不存在")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法删除，权限不足");

        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);

            option.setIOrder(i);
            option.setQuestionId(questionId);
            optionService.checkFullAdd(option);
        }

        optionRepository.deleteAllByQuestionId(questionId);
        optionRepository.saveAllAndFlush(options);

        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/{optionId}")
    public Result modify(@PathVariable Integer optionId, @RequestBody Option newOption) {
        if (newOption == null)
            return Result.validatedFailed("非法请求，数据为空");

        Option option = optionRepository.findById(optionId).orElseThrow(() ->
                new ValidationException("选项不存在")
        );

        Integer userId = option.getQuestion().getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法修改，权限不足");

        Set<String> notAllow = new HashSet<>(){{
            add("id");
            add("questionId");
            add("question");
        }};
        Set<String> optionField = Util.getObjectFiledName(option);
        Map<String, Object> optionMap = Util.objectToMap(option);
        Map<String, Object> newOptionTemplateMap = Util.objectToMap(newOption);
        for (Map.Entry<String, Object> entry : newOptionTemplateMap.entrySet()) {
            if (entry.getValue() == null)
                continue;
            if (!optionField.contains(entry.getKey()))
                return Result.validatedFailed("修改失败, 非法属性: " + entry.getKey());
            if (notAllow.contains(entry.getKey()))
                return Result.validatedFailed("修改失败, 属性" + entry.getKey() + "为只读");

            optionMap.put(entry.getKey(), entry.getValue());
        }

        Option optionFinal = Util.mapToObject(optionMap, Option.class);
        optionService.checkFullUpdate(optionFinal);
        optionRepository.saveAndFlush(optionFinal);
        optionService.setIOrder(optionFinal.getId(), optionFinal.getIOrder());

        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/question/{questionId}/{optionId}")
    public Result modifyByQuestion(@PathVariable Integer questionId,
                                   @PathVariable Integer optionId,
                                   @RequestBody Option newOption)
    {
        if (!optionRepository.existsByIdAndQuestionId(optionId, questionId))
            return Result.validatedFailed("选项不存在或不属于此问题");

        return modify(optionId, newOption);
    }

    @SaCheckLogin
    @GetMapping("/{optionId}")
    public Result get(@PathVariable Integer optionId) {
        Option option = optionRepository.findById(optionId).orElseThrow(() ->
                new ValidationException("选项不存在")
        );

        return Result.successData(option);
    }

    @SaCheckLogin
    @GetMapping("/question/{questionId}/{optionId}")
    public Result getByQuestion(@PathVariable Integer questionId, @PathVariable Integer optionId) {
        Option option = optionRepository.findByIdAndQuestionId(optionId, questionId).orElseThrow(() ->
                new ValidationException("选项不存在或不属于此问题")
        );

        Integer userId = option.getQuestion().getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法修改，权限不足");

        return Result.successData(option);
    }

    @SaCheckLogin
    @GetMapping("/question/{questionId}")
    public Result getAllByQuestion(@PathVariable Integer questionId) {
        return Result.successData(
                optionRepository.findAllByQuestionId(questionId, Sort.by("iOrder"))
        );
    }
}
