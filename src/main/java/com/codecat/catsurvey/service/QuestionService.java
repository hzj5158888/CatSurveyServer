package com.codecat.catsurvey.service;

import com.codecat.catsurvey.exception.CatAuthorizedException;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Option;
import com.codecat.catsurvey.models.Question;
import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.repository.QuestionRepository;
import com.codecat.catsurvey.repository.SurveyRepository;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.utils.Util;
import com.codecat.catsurvey.common.valid.group.validationTime;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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

    @Transactional
    public void add(Question question) {
        if (question == null)
            throw new CatValidationException("无效请求，数据为空");
        
        Survey survey = surveyRepository.findById(question.getSurveyId()).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );
        if (!userService.isLoginId(survey.getUserId()) && !userService.containsPermissionName("SurveyManage"))
            throw new CatAuthorizedException("无法访问，权限不足");

        if (question.getIOrder() == null)
            question.setIOrder(Integer.MAX_VALUE);

        List<Option> optionList = new ArrayList<>(question.getOptionList());
        questionRepository.saveAndFlush(question);
        setIOrder(question.getId(), question.getIOrder());
        if (!question.getOptionList().isEmpty())
            optionService.setByQuestion(question.getId(), optionList);
    }

    @Transactional
    public void del(Integer questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new CatValidationException("问题不存在")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法删除，权限不足");

        questionRepository.delete(question);
    }

    @Transactional
    public void delBySurvey(Integer surveyId, Integer questionId) {
        Question question = questionRepository.findByIdAndSurveyId(questionId, surveyId).orElseThrow(() ->
                new CatValidationException("问题不存在或不属于该问卷")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法删除，权限不足");

        questionRepository.delete(question);
    }

    @Transactional
    public List<Integer> setBySurvey(Integer surveyId, List<Question> questions) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );
        if (!userService.isLoginId(survey.getUserId()) && !userService.containsPermissionName("SurveyManage"))
            throw new CatAuthorizedException("无法访问，权限不足");

        Set<Integer> modify_set = new HashSet<>();
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);

            question.setIOrder(i);
            question.setSurveyId(surveyId);
            if (question.getId() == null)
                checkFullAdd(question);
            else {
                if (!questionRepository.existsByIdAndSurveyId(question.getId(), question.getSurveyId()))
                    throw new CatValidationException("问题不存在或不属于该问卷");

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

    @Transactional
    public void modify(Integer questionId, Question newQuestion) {
        if (newQuestion == null)
            throw new CatValidationException("无效请求, 数据为空");

        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new CatValidationException("问题不存在")
        );
        if (!userService.isLoginId(question.getSurvey().getUserId()) && !userService.containsPermissionName("SurveyManage"))
            throw new CatAuthorizedException("无法修改，权限不足");

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
                throw new CatValidationException("问题信息修改失败, 属性" + entry.getKey() + "为只读");

            questionMap.put(entry.getKey(), entry.getValue());
        }

        Question questionFinal = Util.mapToObject(questionMap, Question.class);
        List<Option> options = new ArrayList<>(newQuestion.getOptionList());
        checkFullUpdate(questionFinal);
        questionRepository.saveAndFlush(questionFinal);
        setIOrder(questionFinal.getId(), questionFinal.getIOrder());
        if (!options.isEmpty())
            optionService.setByQuestion(questionFinal.getId(), options);
    }

    @Transactional
    public void modifyBySurvey(Integer surveyId, Integer questionId, Question newQuestion) {
        Question question = questionRepository.findByIdAndSurveyId(questionId, surveyId).orElseThrow(() ->
                new CatValidationException("问题不存在或不属于该问卷")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法修改，权限不足");

        modify(questionId, newQuestion);
    }

    @Transactional
    public Question get(Integer questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new CatValidationException("问题不存在")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法获取，权限不足");

        return question;
    }

    @Transactional
    public List<Question> getAllBySurvey(Integer surveyId) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );
        if (!userService.isLoginId(survey.getUserId()))
            throw new CatAuthorizedException("无法访问，权限不足");

        return questionRepository.findAllBySurveyId(surveyId, Sort.by("iOrder"));
    }

    @Transactional
    public Question getBySurvey(Integer surveyId, Integer questionId) {
        Question question = questionRepository.findByIdAndSurveyId(questionId, surveyId).orElseThrow(() ->
                new CatValidationException("问题不存在或不属于该问卷")
        );

        Integer userId = question.getSurvey().getUserId();
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法修改，权限不足");

        return question;
    }

    @Transactional
    public boolean existsByIdAndSurveyId(Integer questionId, Integer surveyId) {
        return questionRepository.existsByIdAndSurveyId(questionId, surveyId);
    }

    public void setIOrder(Integer questionId, Integer iOrder) {
        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new CatValidationException("问题不存在")
        );

        List<Question> sortedQuestion = questionRepository.findAllBySurveyId(
                question.getSurveyId(), Sort.by("iOrder")
        );

        if (iOrder == null || iOrder >= sortedQuestion.size())
            iOrder = Math.max(sortedQuestion.size() - 1, 0);
        if (iOrder < 0)
            throw new CatValidationException("iOrder非法");

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
