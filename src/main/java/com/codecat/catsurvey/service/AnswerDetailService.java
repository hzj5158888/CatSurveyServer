package com.codecat.catsurvey.service;

import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.commcon.Enum.question.QuestionTypeEnum;
import com.codecat.catsurvey.commcon.exception.CatAuthorizedException;
import com.codecat.catsurvey.commcon.exception.CatValidationException;
import com.codecat.catsurvey.commcon.models.*;
import com.codecat.catsurvey.commcon.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnswerDetailService {
    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private AnswerDetailRepository answerDetailRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private UserService userService;

    public Integer add(AnswerDetail answerDetail) {
        if (answerDetail == null)
            throw new CatValidationException("无效请求，数据为空");

        Response response = responseRepository.findById(answerDetail.getResponseId()).orElseThrow(() ->
                new CatValidationException("答卷不存在")
        );
        Question question = questionRepository.findById(answerDetail.getQuestionId()).orElseThrow(() ->
                new CatValidationException("问题不存在")
        );
        Survey survey = surveyRepository.findById(response.getSurveyId()).orElseThrow(() ->
                new CatValidationException("问卷不存在")
        );

        if (!survey.getStatus().equals("进行中") && !userService.containsPermissionName("SurveyManage"))
            throw new CatAuthorizedException("权限不足");
        if (!response.getSurveyId().equals(question.getSurveyId()))
            throw new CatValidationException("responseId与questionId不属于同一问卷survey");
        if (answerDetailRepository.existsByResponseIdAndQuestionId(response.getId(), question.getId()))
            throw new CatValidationException("该问题答案已存在");

        if (!question.getType().equals(QuestionTypeEnum.TEXT.getName())) {
            Option option = optionRepository.findById(answerDetail.getOptionId()).orElseThrow(() ->
                    new CatValidationException("选项不存在")
            );

            if (!option.getQuestionId().equals(question.getId()))
                throw new CatValidationException("option中questionId与answer中questionId不同");
        }

        JSONObject jsonAns = (JSONObject) answerDetail.getJsonAnswer();
        answerDetail.setJsonAnswer(jsonAns.toString());
        answerDetailRepository.saveAndFlush(answerDetail);

        return answerDetail.getId();
    }

    @Transactional
    public List<Integer> setByResponse(Integer responseId, List<AnswerDetail> newAnswerDetailList) {
        if (newAnswerDetailList == null || newAnswerDetailList.isEmpty())
            return new ArrayList<>();

        answerDetailRepository.deleteAllByResponseId(responseId);

        List<Integer> ans = new ArrayList<>();
        for (AnswerDetail answerDetail : newAnswerDetailList) {
            answerDetail.setResponseId(responseId);

            add(answerDetail);
            ans.add(answerDetail.getId());
        }

        return ans;
    }
}
