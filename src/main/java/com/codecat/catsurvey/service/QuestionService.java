package com.codecat.catsurvey.service;

import com.alibaba.fastjson2.util.BeanUtils;
import com.codecat.catsurvey.commcon.exception.AuthorizedException;
import com.codecat.catsurvey.commcon.exception.ValidationException;
import com.codecat.catsurvey.commcon.models.Option;
import com.codecat.catsurvey.commcon.models.Question;
import com.codecat.catsurvey.commcon.models.Survey;
import com.codecat.catsurvey.commcon.repository.QuestionRepository;
import com.codecat.catsurvey.commcon.repository.SurveyRepository;
import com.codecat.catsurvey.commcon.utils.Result;
import com.codecat.catsurvey.commcon.utils.Util;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
@NoArgsConstructor
@Validated
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OptionService optionService;

    @Validated(value = validationTime.FullAdd.class)
    public void checkFullAdd(@Valid Question question) {}

    @Validated(value = validationTime.FullUpdate.class)
    public void checkFullUpdate(@Valid Question question) {}

    public void add(Question question) {
        if (question == null)
            throw new ValidationException("无效请求，数据为空");
        
        Survey survey = surveyRepository.findById(question.getSurveyId()).orElseThrow(() ->
                new ValidationException("问卷不存在")
        );
        if (!userService.isLoginId(survey.getUserId()) && !userService.containsPermissionName("SurveyManage"))
            throw new AuthorizedException("无法访问，权限不足");

        if (question.getIOrder() == null)
            question.setIOrder(Integer.MAX_VALUE);

        List<Option> optionList = new ArrayList<>(question.getOptionList());
        questionRepository.saveAndFlush(question);
        setIOrder(question.getId(), question.getIOrder());
        if (!question.getOptionList().isEmpty())
            optionService.setByQuestion(question.getId(), optionList);
    }

    @Transactional
    public List<Integer> setBySurvey(Integer surveyId, List<Question> questions) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new ValidationException("问卷不存在")
        );
        if (!userService.isLoginId(survey.getUserId()) && !userService.containsPermissionName("SurveyManage"))
            throw new AuthorizedException("无法访问，权限不足");

        Set<Integer> modify_set = new HashSet<>();
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);

            question.setIOrder(i);
            question.setSurveyId(surveyId);
            if (question.getId() == null)
                checkFullAdd(question);
            else {
                if (!questionRepository.existsByIdAndSurveyId(question.getId(), question.getSurveyId()))
                    throw new ValidationException("问题不存在或不属于该问卷");

                modify_set.add(question.getId());
                question.setSurveyId(null);
                question.setSurvey(null);
            }
        }

        List<Question> question_all = questionRepository.findAllBySurveyId(surveyId);
        for (Question question : question_all) {
            if (!modify_set.contains(question.getId()))
                questionRepository.deleteById(question.getId());
        }

        List<Integer> ans = new ArrayList<>();
        for (Question question : questions) {
            if (modify_set.contains(question.getId())) {
                Integer questionId = question.getId();
                question.setId(null);
                modify(questionId, question);

                ans.add(question.getId());
                continue;
            }

            add(question);
            ans.add(question.getId());
        }

        return ans;
    }

    public void modify(Integer questionId, Question newQuestion) {
        if (newQuestion == null)
            throw new ValidationException("无效请求, 数据为空");

        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new ValidationException("问题不存在")
        );
        if (!userService.isLoginId(question.getSurvey().getUserId()) && !userService.containsPermissionName("SurveyManage"))
            throw new AuthorizedException("无法修改，权限不足");

        Set<String> notAllow = new HashSet<>() {{
            add("id");
            add("surveyId");
            add("survey");
        }};
        Set<String> continueItem = new HashSet<>() {{
            add("optionList");
        }};
        Map<String, Object> questionMap = Util.objectToMap(question);
        Map<String, Object> newQuestionMap = Util.objectToMap(newQuestion);
        for (Map.Entry<String, Object> entry : newQuestionMap.entrySet()) {
            if (entry.getValue() == null || continueItem.contains(entry.getKey()))
                continue;
            if (notAllow.contains(entry.getKey()))
                throw new ValidationException("问题信息修改失败, 属性" + entry.getKey() + "为只读");

            questionMap.put(entry.getKey(), entry.getValue());
        }

        Question questionFinal = Util.mapToObject(questionMap, Question.class);
        checkFullUpdate(questionFinal);
        questionRepository.saveAndFlush(questionFinal);
        setIOrder(questionFinal.getId(), questionFinal.getIOrder());
        if (newQuestionMap.get("optionList") != null) { // 修改optionList
            if (!(newQuestionMap.get("optionList") instanceof List<?>))
                throw new ValidationException("optionList必须为数组类型");

            optionService.setByQuestion(questionFinal.getId(), (List<Option>) newQuestionMap.get("optionList"));
        }
    }

    public void setIOrder(Integer questionId, Integer iOrder) {
        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new ValidationException("问题不存在")
        );

        List<Question> sortedQuestion = questionRepository.findAllBySurveyId(
                question.getSurveyId(), Sort.by("iOrder")
        );

        if (iOrder == null || iOrder >= sortedQuestion.size())
            iOrder = Math.max(sortedQuestion.size() - 1, 0);
        if (iOrder < 0)
            throw new ValidationException("iOrder非法");

        List<Question> res = new ArrayList<>();
        for (int i = 0; i < sortedQuestion.size(); i++)
        {
            sortedQuestion.get(i).setIOrder(i);

            Integer curId = sortedQuestion.get(i).getId();
            if (questionId.equals(curId) && !iOrder.equals(i))
                continue;
            else if (iOrder.equals(i))
            {
                res.add(question);
                iOrder = -1;
                if (!curId.equals(questionId))
                    i--;

                continue;
            }

            res.add(sortedQuestion.get(i));
        }
        if (!iOrder.equals(-1))
            res.add(question);

        for (int i = 0; i < res.size(); i++)
            res.get(i).setIOrder(i);

        questionRepository.saveAllAndFlush(res);
    }
}
