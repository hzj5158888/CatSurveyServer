package com.codecat.catsurvey.service;

import com.codecat.catsurvey.common.Enum.survey.SurveyStatusEnum;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.exception.CatAuthorizedException;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.AnswerDetail;
import com.codecat.catsurvey.models.Response;
import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.repository.ResponseRepository;
import com.codecat.catsurvey.repository.SurveyRepository;
import com.codecat.catsurvey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResponseService {
    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private UserService userService;

    @Autowired
    private AnswerDetailService answerDetailService;

    @Transactional
    public Integer add(Response response) {
        Survey survey = surveyService.get(response.getSurveyId());

        if (!survey.getStatus().equals(SurveyStatusEnum.CARRYOUT.getName()))
            throw new CatValidationException("无法回答非进行中的问卷");
        if (survey.getEndDate().getTime() < System.currentTimeMillis())
            throw new CatValidationException("时间已过，无法作答");

        List<AnswerDetail> answerDetails = new ArrayList<>(response.getAnswerDetailList());

        response.setId(null);
        response.setUserId(userService.getLoginId());
        responseRepository.saveAndFlush(response);
        if (!answerDetails.isEmpty())
            answerDetailService.setByResponse(response.getId(), answerDetails);

        return response.getId();
    }

    @Transactional
    public void del(Integer responseId) {
        Response response = responseRepository.findById(responseId).orElseThrow(() ->
                new CatValidationException("答卷不存在")
        );

        responseRepository.deleteById(response.getId());
    }

    @Transactional
    public void delBySurvey(Integer surveyId, Integer responseId) {
        Response response = responseRepository.findByIdAndSurveyId(responseId, surveyId).orElseThrow(() ->
                new CatValidationException("答卷不存在或不属于该问卷")
        );

        responseRepository.deleteById(response.getId());
    }

    @Transactional
    public Response get(Integer responseId) {
        return responseRepository.findById(responseId).orElseThrow(() ->
                new CatValidationException("答卷不存在")
        );
    }

    @Transactional
    public Response getBySurvey(Integer surveyId, Integer responseId) {
        return responseRepository.findByIdAndSurveyId(responseId, surveyId).orElseThrow(() ->
                new CatValidationException("答卷不存在或不属于该问卷")
        );
    }

    @Transactional
    public List<Response> getAllBySurvey(Integer surveyId) {
        if (!surveyRepository.existsById(surveyId))
            throw new CatValidationException("问卷不存在");

        return responseRepository.findAllBySurveyId(surveyId);
    }

    @Transactional
    public Survey getSurvey(Integer responseId) {
        Response response = responseRepository.findById(responseId).orElseThrow(() ->
                new CatValidationException("答卷不存在")
        );

        return surveyRepository.findById(response.getSurveyId()).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );
    }


    @Transactional
    public Integer getUserId(Integer responseId) {
        Response response = responseRepository.findById(responseId).orElseThrow(() ->
                new CatValidationException("答卷不存在")
        );

        return response.getUserId();
    }

    @Transactional
    public boolean existsByIdAndSurveyId(Integer responseId, Integer surveyId) {
        return responseRepository.existsByIdAndSurveyId(responseId, surveyId);
    }

    public List<Response> getAll() {
        return responseRepository.findAll();
    }
}
