package com.codecat.catsurvey.service;

import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.common.Enum.survey.SurveyStatusEnum;
import com.codecat.catsurvey.exception.CatAuthorizedException;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Question;
import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.repository.SurveyRepository;
import com.codecat.catsurvey.repository.TemplateRepository;
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
    private TemplateRepository templateRepository;

    @Transactional
    @Validated(validationTime.FullAdd.class)
    public void add(@Valid Survey survey) {
        if (survey.getStatus().equals(SurveyStatusEnum.CARRYOUT.getName())) {
            if (survey.getStartDate() == null || survey.getEndDate() == null)
                throw new CatValidationException("开始时间和截止时间不能为空");
            if (survey.getStartDate().getTime() > survey.getEndDate().getTime())
                throw new CatValidationException("开始时间不得先于截至时间");
        }

        survey.setId(null);
        List<Question> questions = new ArrayList<>(survey.getQuestionList());
        survey.setQuestionList(null);
        survey.setResponseList(null);
        surveyRepository.saveAndFlush(survey);
        if (!questions.isEmpty())
            questionService.addBySurvey(survey.getId(), questions);
    }

    @Transactional
    public void del(Integer surveyId) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );

        surveyRepository.delete(survey);
    }

    @Transactional
    public void del(Integer userId, Integer surveyId) {
        Survey survey = surveyRepository.findByIdAndUserId(surveyId, userId).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );

        del(survey.getId());
    }

    @Transactional
    public void modify(@PathVariable Integer surveyId, @RequestBody Survey newSurvey) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );

        Set<String> continueItem = new HashSet<>() {{
            add("id");
            add("userId");
            add("createDate");
            add("questionList");
            add("responseList");
        }};
        Map<String, Object> surveyMap = Util.objectToMap(survey);
        Map<String, Object> newSurveyMap = Util.objectToMap(newSurvey);
        for (Map.Entry<String, Object> entry : newSurveyMap.entrySet()) {
            if (entry.getValue() == null || continueItem.contains(entry.getKey()))
                continue;

            if (SurveyStatusEnum.CARRYOUT.getName().equals(newSurveyMap.get("status"))
                    && ((entry.getKey().equals("startDate")
                        || entry.getKey().equals("endDate")
                        || entry.getKey().equals("status")))
                )
            {
                Date startDate = (newSurvey.getStartDate() == null ? survey.getStartDate() : newSurvey.getStartDate());
                Date endDate = (newSurvey.getEndDate() == null ? survey.getEndDate() : newSurvey.getEndDate());

                if (startDate == null)
                    startDate = new Date(System.currentTimeMillis());
                if (endDate == null)
                    endDate = new Date((long) Integer.MAX_VALUE * 1000);

                if (startDate.getTime() > endDate.getTime())
                    throw new CatValidationException("开始时间不得先于截至时间");

                surveyMap.put("startDate", startDate);
                surveyMap.put("endDate", endDate);
                if (entry.getKey().equals("startDate") || entry.getKey().equals("endDate"))
                    continue;
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

    public Survey get(Integer surveyId) {
        return surveyRepository.findById(surveyId).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );
    }

    public Survey getByUser(Integer userId, Integer surveyId) {
        return surveyRepository.findByIdAndUserId(surveyId, userId).orElseThrow(() ->
                new CatValidationException("问卷不存在或不属于此用户")
        );
    }

    public List<Survey> getAll() {
        return surveyRepository.findAll(Sort.by(Sort.Direction.DESC, "createDate"));
    }

    public List<Survey> getAllByUser(Integer userId) {
        if (!userRepository.existsById(userId))
            throw new CatValidationException("用户不存在");

        return surveyRepository.findAllByUserId(userId, Sort.by(Sort.Direction.DESC, "createDate"));
    }

    public boolean isTemplate(Integer surveyId) { return templateRepository.existsBySurveyId(surveyId); }

    public String getStatus(Integer surveyId) {
        return get(surveyId).getStatus();
    }

    public Integer getUserId(Integer surveyId) {
        return get(surveyId).getUserId();
    }

    public List<Question> getQuestionList(Integer surveyId) {
        return questionService.getAllBySurvey(surveyId);
    }

    public boolean existsByIdAndUserId(Integer surveyId, Integer userId) {
        return surveyRepository.existsByIdAndUserId(surveyId, userId);
    }

    public void deleteAllByUserId(Integer userId) {
        surveyRepository.deleteAllByUserId(userId);
    }

    @Validated(validationTime.FullUpdate.class)
    public void checkFullUpdate(@Valid Survey survey) {}
}
