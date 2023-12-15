package com.codecat.catsurvey.service;

import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.common.Enum.survey.SurveyStatusEnum;
import com.codecat.catsurvey.exception.CatAuthorizedException;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Question;
import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.repository.SurveyRepository;
import com.codecat.catsurvey.repository.UserRepository;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.utils.Util;
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
public class SurveyService {
    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public void add(Survey survey) {
        if (survey.getUserId() == null)
            survey.setUserId(userService.getLoginId());
        if (!userService.isLoginId(survey.getUserId()))
            throw new CatAuthorizedException("无法添加，权限不足");

        if (!survey.getStatus().equals("草稿")) {
            if (survey.getStartDate() == null || survey.getEndDate() == null)
                throw new CatValidationException("开始时间和截止时间不能为空");
            if (survey.getStartDate().getTime() > survey.getEndDate().getTime())
                throw new CatValidationException("开始时间不得先于截至时间");
        }

        List<Question> questions = new ArrayList<>(survey.getQuestionList());
        surveyRepository.saveAndFlush(survey);
        if (!questions.isEmpty())
            questionService.setBySurvey(survey.getId(), questions);
    }

    public void del(Integer surveyId) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );
        if (!userService.isLoginId(survey.getUserId()))
            throw new CatAuthorizedException("无法删除，权限不足");

        surveyRepository.delete(survey);
    }

    public void del(Integer userId, Integer surveyId) {
        Survey survey = surveyRepository.findByIdAndUserId(surveyId, userId).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );
        if (!userService.isLoginId(survey.getUserId()))
            throw new CatAuthorizedException("无法删除，权限不足");

        surveyRepository.delete(survey);
    }

    public void delAll(@RequestBody JSONObject delSurvey) {
        if (delSurvey.entrySet().isEmpty()) {
            surveyRepository.deleteAllByUserId(userService.getLoginId());
            return;
        }

        Object surveyIdListObj = delSurvey.get("surveyIdList");
        if (!(surveyIdListObj instanceof List<?>))
            throw new CatValidationException("类型错误");

        List<Integer> surveyIdList = (List<Integer>) surveyIdListObj;
        for (Integer curId : surveyIdList) {
            if (!surveyRepository.existsByIdAndUserId(curId, userService.getLoginId()))
                throw new CatValidationException("无法删除，权限不足");

            surveyRepository.deleteById(curId);
        }
    }

    @Transactional
    public void modify(@PathVariable Integer surveyId, @RequestBody Survey newSurvey) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );
        if (!userService.isLoginId(survey.getUserId()))
            throw new CatAuthorizedException("无法修改，权限不足");

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
                throw new CatValidationException("修改失败, 非法属性: " + entry.getKey());
            if (notAllow.contains(entry.getKey()))
                throw new CatValidationException("修改失败, 属性" + entry.getKey() + "为只读");
            if (entry.getKey().equals("status") && !entry.getValue().equals("草稿")
                    || (entry.getKey().equals("startDate") || entry.getKey().equals("endDate")) )
            {
                Date startDate = (newSurvey.getStartDate() == null ? survey.getStartDate() : newSurvey.getStartDate());
                Date endDate = (newSurvey.getEndDate() == null ? survey.getEndDate() : newSurvey.getEndDate());

                if (startDate == null || endDate == null)
                    throw new CatValidationException("开始时间和截止时间不能为空");
                if (startDate.getTime() > endDate.getTime())
                    throw new CatValidationException("开始时间不得先于截至时间");
            }

            surveyMap.put(entry.getKey(), entry.getValue());
        }

        Survey surveyFinal = Util.mapToObject(surveyMap, Survey.class);
        List<Question> questions = new ArrayList<>(newSurvey.getQuestionList());
        checkFullUpdate(surveyFinal);
        surveyRepository.saveAndFlush(surveyFinal);
        if (!questions.isEmpty()) // 设置questionList
            questionService.setBySurvey(surveyFinal.getId(), questions);
    }

    @Transactional
    public Survey get(Integer surveyId) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );

        if (!userService.isLoginId(survey.getUserId())
                && !survey.getStatus().equals(SurveyStatusEnum.CARRYOUT.getName()))
            throw new CatValidationException("无法访问，权限不足");

        return survey;
    }

    @Transactional
    public Survey getByUser(Integer userId, Integer surveyId) {
        Survey survey = surveyRepository.findByIdAndUserId(surveyId, userId).orElseThrow(() ->
                new CatValidationException("问卷不存在或不属于此用户")
        );
        if (!userService.isLoginId(survey.getUserId()))
            throw new CatAuthorizedException("无法访问，权限不足");

        return survey;
    }

    public List<Survey> getAllByUser(Integer userId) {
        if (!userRepository.existsById(userId))
            throw new CatValidationException("用户不存在");
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法获取, 权限不足");

        return surveyRepository.findAllByUserId(userId, Sort.by(Sort.Direction.DESC, "createDate"));
    }

    @Validated(validationTime.FullUpdate.class)
    public void checkFullUpdate(@Valid Survey survey) {}
}
