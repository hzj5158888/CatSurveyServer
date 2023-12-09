package com.codecat.catsurvey.service;

import com.alibaba.fastjson2.JSONObject;
import com.codecat.catsurvey.commcon.Enum.question.QuestionTypeEnum;
import com.codecat.catsurvey.commcon.exception.ValidationException;
import com.codecat.catsurvey.commcon.models.AnswerDetail;
import com.codecat.catsurvey.commcon.models.Option;
import com.codecat.catsurvey.commcon.models.Question;
import com.codecat.catsurvey.commcon.models.Response;
import com.codecat.catsurvey.commcon.repository.AnswerDetailRepository;
import com.codecat.catsurvey.commcon.repository.OptionRepository;
import com.codecat.catsurvey.commcon.repository.QuestionRepository;
import com.codecat.catsurvey.commcon.repository.ResponseRepository;
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
    private UserService userService;

    public Integer add(AnswerDetail answerDetail) {
        if (answerDetail == null)
            throw new ValidationException("无效请求，数据为空");

        Response response = responseRepository.findById(answerDetail.getResponseId()).orElseThrow(() ->
                new ValidationException("答卷不存在")
        );

        Question question = questionRepository.findById(answerDetail.getQuestionId()).orElseThrow(() ->
                new ValidationException("问题不存在")
        );

        if (!response.getSurveyId().equals(question.getSurveyId()))
            throw new ValidationException("responseId与questionId不属于同一问卷survey");
        if (!userService.getLoginId().equals(response.getUserId()) && !userService.containsPermissionName("SurveyManage"))
            throw new ValidationException("无法添加，权限不足");
        if (answerDetailRepository.existsByResponseIdAndQuestionId(response.getId(), question.getId()))
            throw new ValidationException("该问题答案已存在");

        if (!question.getType().equals(QuestionTypeEnum.TEXT.getName())) {
            Option option = optionRepository.findById(answerDetail.getOptionId()).orElseThrow(() ->
                    new ValidationException("选项不存在")
            );

            if (!option.getQuestionId().equals(question.getId()))
                throw new ValidationException("option中questionId与answer中questionId不同");
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
            add(answerDetail);
            ans.add(answerDetail.getId());
        }

        return ans;
    }
}
