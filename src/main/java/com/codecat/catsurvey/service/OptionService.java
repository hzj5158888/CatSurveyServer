package com.codecat.catsurvey.service;

import com.codecat.catsurvey.commcon.Enum.question.QuestionTypeEnum;
import com.codecat.catsurvey.commcon.exception.AuthorizedException;
import com.codecat.catsurvey.commcon.exception.ValidationException;
import com.codecat.catsurvey.commcon.models.Option;
import com.codecat.catsurvey.commcon.models.Question;
import com.codecat.catsurvey.commcon.repository.OptionRepository;
import com.codecat.catsurvey.commcon.repository.QuestionRepository;
import com.codecat.catsurvey.commcon.utils.Result;
import com.codecat.catsurvey.commcon.utils.Util;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

@Service
@Validated
public class OptionService {
    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserService userService;

    @Validated(value = {validationTime.FullAdd.class})
    public void checkFullAdd(@Valid Option option) {}

    @Validated(value = {validationTime.FullUpdate.class})
    public void checkFullUpdate(@Valid Option option) {}

    public void add(Option option) {
        if (option == null)
            throw new ValidationException("无效请求，数据为空");
        
        Question question = questionRepository.findById(option.getQuestionId()).orElseThrow(() ->
                new ValidationException("问题不存在")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            throw new AuthorizedException("无法添加，权限不足");
        if (question.getType().equals(QuestionTypeEnum.TEXT.getName()))
            throw new ValidationException("无法添加选项，该问题非选择题");

        if (option.getIOrder() == null)
            option.setIOrder(Integer.MAX_VALUE);

        optionRepository.saveAndFlush(option);
        setIOrder(option.getId(), option.getIOrder());
    }

    public void modify(Integer optionId, Option newOption) {
        if (newOption == null)
            throw new ValidationException("无效请求, 数据为空");

        Option option = optionRepository.findById(optionId).orElseThrow(() ->
                new ValidationException("选项不存在")
        );

        Integer userId = option.getQuestion().getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            throw new AuthorizedException("无法修改，权限不足");

        Set<String> notAllow = new HashSet<>(){{
            add("id");
            add("questionId");
            add("question");
        }};
        Set<String> optionField = Util.getObjectFiledName(option);
        Map<String, Object> optionMap = Util.objectToMap(option);
        Map<String, Object> newOptionMap = Util.objectToMap(newOption);
        for (Map.Entry<String, Object> entry : newOptionMap.entrySet()) {
            if (entry.getValue() == null)
                continue;
            if (!optionField.contains(entry.getKey()))
                throw new ValidationException("修改失败, 非法属性: " + entry.getKey());
            if (notAllow.contains(entry.getKey()))
                throw new ValidationException("修改失败, 属性" + entry.getKey() + "为只读");

            optionMap.put(entry.getKey(), entry.getValue());
        }

        Option optionFinal = Util.mapToObject(optionMap, Option.class);
        checkFullUpdate(optionFinal);
        optionRepository.saveAndFlush(optionFinal);
        setIOrder(optionFinal.getId(), optionFinal.getIOrder());
    }

    @Transactional
    public void setByQuestion(Integer questionId, List<Option> options) {
        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new ValidationException("问题不存在")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            throw new AuthorizedException("无法删除，权限不足");

        Set<Integer> modify_set = new HashSet<>();
        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);

            option.setIOrder(i);
            option.setQuestionId(questionId);

            if (option.getId() == null)
                checkFullAdd(option);
            else {
                if (!optionRepository.existsByIdAndQuestionId(option.getId(), questionId))
                    throw new ValidationException("选项不存在或不属于此问题");

                modify_set.add(option.getId());
                option.setQuestionId(null);
            }
        }

        List<Option> option_all = optionRepository.findAllByQuestionId(questionId);
        for (Option option : option_all) {
            if (!modify_set.contains(option.getId()))
                optionRepository.deleteById(option.getId());
        }

        for (Option option : options) {
            if (modify_set.contains(option.getId())) {
                Integer optionId = option.getId();
                option.setId(null);
                modify(optionId, option);
                continue;
            }

            optionRepository.saveAndFlush(option);
        }
    }

    public void setIOrder(Integer optionId, Integer iOrder) {
        Option option = optionRepository.findById(optionId).orElseThrow(() ->
                new ValidationException("选项不存在")
        );

        List<Option> sortedOption = optionRepository.findAllByQuestionId(
                option.getQuestionId(), Sort.by("iOrder")
        );

        if (iOrder == null || iOrder >= sortedOption.size())
            iOrder = Math.max(sortedOption.size() - 1, 0);
        if (iOrder < 0)
            throw new ValidationException("iOrder非法");

        List<Option> res = new ArrayList<>();
        for (int i = 0; i < sortedOption.size(); i++)
        {
            sortedOption.get(i).setIOrder(i);

            Integer curId = sortedOption.get(i).getId();
            if (optionId.equals(curId) && !iOrder.equals(i))
                continue;
            else if (iOrder.equals(i))
            {
                res.add(option);
                iOrder = -1;
                if (!curId.equals(optionId))
                    i--;

                continue;
            }

            res.add(sortedOption.get(i));
        }
        if (!iOrder.equals(-1))
            res.add(option);

        for (int i = 0; i < res.size(); i++)
            res.get(i).setIOrder(i);

        optionRepository.saveAllAndFlush(res);
    }
}
