package com.codecat.catsurvey.service;

import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.common.Enum.question.QuestionTypeEnum;
import com.codecat.catsurvey.exception.CatAuthorizedException;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.*;
import com.codecat.catsurvey.repository.*;
import com.codecat.catsurvey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

        if (!survey.getStatus().equals("进行中"))
            throw new CatAuthorizedException("权限不足");
        if (!response.getSurveyId().equals(question.getSurveyId()))
            throw new CatValidationException("responseId与questionId不属于同一问卷survey");
        if (answerDetailRepository.existsByResponseIdAndQuestionId(response.getId(), question.getId()))
            throw new CatValidationException("该问题答案已存在");
        Set<String> textName = new HashSet<>() {{ // 文本类型
            add(QuestionTypeEnum.TEXT.getName());
            add(QuestionTypeEnum.TEXTAREA.getName());
        }};
        if (!textName.contains(question.getType())) {
            JSONObject jsonAnswer = JSONObject.parseObject((String) answerDetail.getJsonAnswer());
            Set<String> keySet = jsonAnswer.keySet();
            if (!keySet.contains("selected") || keySet.size() > 2)
                throw new CatValidationException("json数据错误");
            if (!(jsonAnswer.get("selected") instanceof List<?>))
                throw new CatValidationException("selected为null或类型错误");

            List<Integer> optionIdList = (List<Integer>) jsonAnswer.get("selected");
            for (Integer curOptionId : optionIdList) {
                Option option = optionRepository.findById(curOptionId).orElseThrow(() ->
                        new CatValidationException("选项不存在")
                );

                if (!option.getQuestionId().equals(question.getId()))
                    throw new CatValidationException("option中questionId与answer中questionId不同");
            }
        }

        JSONObject jsonAns = JSONObject.parseObject((String) answerDetail.getJsonAnswer());
        answerDetail.setJsonAnswer(jsonAns.toString());
        answerDetailRepository.saveAndFlush(answerDetail);

        return answerDetail.getId();
    }

    @Transactional
    public void del(Integer answerDetailId) {
        AnswerDetail answerDetail = answerDetailRepository.findById(answerDetailId).orElseThrow(() ->
                new CatValidationException("答案不存在")
        );

        Integer userId = answerDetail.getResponse().getSurvey().getUserId();
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法删除，权限不足");

        answerDetailRepository.deleteById(answerDetailId);
    }

    @Transactional
    public void delByResponse(Integer responseId, Integer answerDetailId) {
        AnswerDetail answerDetail = answerDetailRepository.findByIdAndResponseId(answerDetailId, responseId).orElseThrow(() ->
                new CatValidationException("答案不存在或不属于此答卷")
        );

        Integer userId = answerDetail.getResponse().getSurvey().getUserId();
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法删除，权限不足");

        answerDetailRepository.deleteById(answerDetailId);
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

    @Transactional
    public AnswerDetail get(Integer answerDetailId) {
        AnswerDetail answerDetail = answerDetailRepository.findById(answerDetailId).orElseThrow(() ->
                new CatValidationException("答案ID不存在")
        );

        Integer userId = answerDetail.getResponse().getSurvey().getUserId();
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法获取，权限不足");

        return answerDetail;
    }

    @Transactional
    public AnswerDetail getByResponse(Integer responseId, Integer answerDetailId) {
        AnswerDetail answerDetail = answerDetailRepository.findByIdAndResponseId(answerDetailId, responseId).orElseThrow(() ->
                new CatValidationException("答案不存在或不属于此答卷")
        );

        Integer userId = answerDetail.getResponse().getSurvey().getUserId();
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法获取，权限不足");

        return answerDetail;
    }

    @Transactional
    public List<AnswerDetail> getAllByResponse(Integer responseId) {
        Response response = responseRepository.findById(responseId).orElseThrow(() ->
                new CatValidationException("答卷不存在")
        );

        Integer userId = response.getSurvey().getUserId();
        if (!userService.isLoginId(userId))
            throw new CatAuthorizedException("无法获取，权限不足");

        return answerDetailRepository.findAllByResponseId(responseId);
    }
}
