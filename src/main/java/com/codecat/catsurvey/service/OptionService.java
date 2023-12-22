package com.codecat.catsurvey.service;

import com.codecat.catsurvey.common.Enum.question.QuestionTypeEnum;
import com.codecat.catsurvey.exception.CatAuthorizedException;
import com.codecat.catsurvey.exception.CatForbiddenException;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Option;
import com.codecat.catsurvey.models.Question;
import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.repository.OptionRepository;
import com.codecat.catsurvey.repository.QuestionRepository;
import com.codecat.catsurvey.repository.SurveyRepository;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.utils.Util;
import com.codecat.catsurvey.common.valid.group.validationTime;
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
    private SurveyRepository surveyRepository;

    @Autowired
    private UserService userService;

    @Validated(value = {validationTime.FullAdd.class})
    public void checkFullAdd(@Valid Option option) {}

    @Validated(value = {validationTime.FullUpdate.class})
    public void checkFullUpdate(@Valid Option option) {}

    @Transactional
    @Validated(value = validationTime.FullAdd.class)
    public void add(@Valid Option option) {
        if (option == null)
            throw new CatValidationException("无效请求，数据为空");

        Question question = questionRepository.findById(option.getQuestionId()).orElseThrow(() ->
                new CatValidationException("问题不存在")
        );
        if (question.getType().equals(QuestionTypeEnum.TEXT.getName()))
            throw new CatValidationException("无法添加选项，该问题非选择题");

        if (option.getIOrder() == null)
            option.setIOrder(Integer.MAX_VALUE);

        option.setId(null);
        optionRepository.saveAndFlush(option);
        setIOrder(option.getId(), option.getIOrder());
    }

    @Transactional
    public void add(List<Option> optionList) {
        if (optionList == null)
            throw new CatValidationException("无效请求，数据为空");

        for (Option option : optionList)
            add(option);
    }

    @Transactional
    public void addByQuestion(Integer questionId, Option option) {
        if (!questionRepository.existsById(questionId))
            throw new CatValidationException("问题不存在");

        option.setQuestionId(questionId);
        add(option);
    }

    @Transactional
    public void addByQuestion(Integer questionId, List<Option> optionList) {
        if (questionId == null || optionList == null)
            throw new CatValidationException("无效请求，数据为空");

        for (Option option : optionList)
            addByQuestion(questionId, option);
    }

    @Transactional
    public void del(Integer optionId) {
        if (!optionRepository.existsById(optionId))
            throw new CatValidationException("选项不存在");

        optionRepository.deleteById(optionId);
    }

    @Transactional
    public void delByQuestion(Integer questionId, Integer optionId) {
        if (!optionRepository.existsByIdAndQuestionId(optionId, questionId))
            throw new CatValidationException("选项不存在或不属于此问题");

        optionRepository.deleteById(optionId);
    }

    @Transactional
    public void modify(Integer optionId, Option newOption) {
        if (newOption == null)
            throw new CatValidationException("无效请求, 数据为空");

        Option option = optionRepository.findById(optionId).orElseThrow(() ->
                new CatValidationException("选项不存在")
        );

        Set<String> continueItem = new HashSet<>() {{
            add("id");
            add("questionId");
            add("question");
        }};
        Map<String, Object> optionMap = Util.objectToMap(option);
        Map<String, Object> newOptionMap = Util.objectToMap(newOption);
        for (Map.Entry<String, Object> entry : newOptionMap.entrySet()) {
            if (entry.getValue() == null || continueItem.contains(entry.getKey()))
                continue;

            optionMap.put(entry.getKey(), entry.getValue());
        }

        Option optionFinal = Util.mapToObject(optionMap, Option.class);
        checkFullUpdate(optionFinal);
        optionRepository.saveAndFlush(optionFinal);
        setIOrder(optionFinal.getId(), optionFinal.getIOrder());
    }

    @Transactional
    public void modifyByQuestion(Integer questionId, Integer optionId, Option newOption) {
        if (!optionRepository.existsByIdAndQuestionId(optionId, questionId))
            throw new CatValidationException("选项不存在或不属于此问题");

        modify(optionId, newOption);
    }

    @Transactional
    public void setByQuestion(Integer questionId, List<Option> options) {
        if (!questionRepository.existsById(questionId))
            throw new CatValidationException("问题不存在");

        Set<Integer> modify_set = new HashSet<>();
        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);

            option.setIOrder(i);
            option.setQuestionId(questionId);
            if (option.getId() == null)
                checkFullAdd(option);
            else {
                if (!optionRepository.existsByIdAndQuestionId(option.getId(), questionId))
                    throw new CatValidationException("选项不存在或不属于此问题");

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

            add(option);
        }
    }

    @Transactional
    public Option get(Integer optionId) {
        return optionRepository.findById(optionId).orElseThrow(() ->
                new CatValidationException("选项不存在")
        );
    }

    @Transactional
    public Option getByQuestion(Integer questionId, Integer optionId) {
        return optionRepository.findByIdAndQuestionId(optionId, questionId).orElseThrow(() ->
                new CatValidationException("选项不存在或不属于此问题")
        );
    }

    @Transactional
    public List<Option> getAllByQuestion(Integer questionId) {
        return optionRepository.findAllByQuestionId(questionId, Sort.by("iOrder"));
    }

    public void isLoginUser(Integer optionId) {
        Option option = optionRepository.findById(optionId).orElseThrow(() ->
                new CatValidationException("选项不存在")
        );
        Question question = questionRepository.findById(option.getQuestionId()).orElseThrow(() ->
                new CatValidationException("问题不存在")
        );
        Survey survey = surveyRepository.findById(question.getSurveyId()).orElseThrow(() ->
                new CatValidationException("答卷不存在")
        );

        if (!userService.isLoginId(survey.getUserId()))
            throw new CatForbiddenException("无法读取或修改其它用户的选项");
    }

    @Transactional
    public void setIOrder(Integer optionId, Integer iOrder) {
        Option option = optionRepository.findById(optionId).orElseThrow(() ->
                new CatValidationException("选项不存在")
        );

        List<Option> sortedOption = optionRepository.findAllByQuestionId(
                option.getQuestionId(), Sort.by("iOrder")
        );

        if (iOrder == null || iOrder >= sortedOption.size())
            iOrder = Math.max(sortedOption.size() - 1, 0);
        if (iOrder < 0)
            throw new CatValidationException("iOrder非法");

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
