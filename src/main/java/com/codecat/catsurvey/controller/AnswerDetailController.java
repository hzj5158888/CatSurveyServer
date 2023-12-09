package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
import com.codecat.catsurvey.commcon.utils.Result;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import com.codecat.catsurvey.service.AnswerDetailService;
import com.codecat.catsurvey.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/response/detail")
public class AnswerDetailController {
    @Autowired
    private AnswerDetailRepository answerDetailRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private AnswerDetailService answerDetailService;

    @Autowired
    private UserService userService;

    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) AnswerDetail answerDetail) {
        return Result.successData(answerDetailService.add(answerDetail));
    }

    @PostMapping("/response/{responseId}")
    public Result addByResponse(@PathVariable Integer responseId,
                                @RequestBody @Validated(validationTime.Add.class) AnswerDetail answerDetail)
    {
        answerDetail.setResponseId(responseId);
        return Result.successData(this.add(answerDetail));
    }

    @SaCheckLogin
    @DeleteMapping("/{answerDetailId}")
    public Result del(@PathVariable Integer answerDetailId) {
        AnswerDetail answerDetail = answerDetailRepository.findById(answerDetailId).orElseThrow(() ->
                new ValidationException("答案不存在")
        );

        Integer userId = answerDetail.getResponse().getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法删除，权限不足");

        answerDetailRepository.deleteById(answerDetailId);
        return Result.success();
    }

    @SaCheckLogin
    @DeleteMapping("/response/{responseId}/{answerDetailId}")
    public Result delByResponse(@PathVariable Integer responseId, @PathVariable Integer answerDetailId) {
        AnswerDetail answerDetail = answerDetailRepository.findByIdAndResponseId(answerDetailId, responseId).orElseThrow(() ->
                new ValidationException("答案不存在或不属于此答卷")
        );

        Integer userId = answerDetail.getResponse().getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法删除，权限不足");

        answerDetailRepository.deleteById(answerDetailId);
        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/{answerDetailId}")
    public Result get(@PathVariable Integer answerDetailId) {
        AnswerDetail answerDetail = answerDetailRepository.findById(answerDetailId).orElseThrow(() ->
                new ValidationException("答案ID不存在")
        );

        Integer userId = answerDetail.getResponse().getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法获取，权限不足");

        return Result.successData(answerDetail);
    }

    @SaCheckLogin
    @GetMapping("/response/{responseId}/{answerDetailId}")
    public Result getByResponse(@PathVariable Integer responseId,
                                @PathVariable Integer answerDetailId)
    {
        AnswerDetail answerDetail = answerDetailRepository.findByIdAndResponseId(answerDetailId, responseId).orElseThrow(() ->
                new ValidationException("答案不存在或不属于此答卷")
        );

        Integer userId = answerDetail.getResponse().getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法获取，权限不足");

        return Result.successData(answerDetail);
    }

    @SaCheckLogin
    @GetMapping("/response/{responseId}")
    public Result getAllByResponse(@PathVariable Integer responseId) {
        Response response = responseRepository.findById(responseId).orElseThrow(() ->
                new ValidationException("答卷不存在")
        );

        Integer userId = response.getSurvey().getUserId();
        if (!userService.isLoginId(userId) && !userService.containsPermissionName("SurveyManage"))
            return Result.unauthorized("无法获取，权限不足");

        return Result.successData(
                answerDetailRepository.findAllByResponseId(responseId)
        );
    }
}
