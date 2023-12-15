package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.AnswerDetail;
import com.codecat.catsurvey.models.Response;
import com.codecat.catsurvey.repository.AnswerDetailRepository;
import com.codecat.catsurvey.repository.ResponseRepository;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.service.AnswerDetailService;
import com.codecat.catsurvey.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/response/detail")
@CrossOrigin()
public class AnswerDetailController {
    @Autowired
    private AnswerDetailService answerDetailService;

    @PostMapping("")
    public Result add(@RequestBody @Validated(validationTime.FullAdd.class) AnswerDetail answerDetail) {
        return Result.successData(answerDetailService.add(answerDetail));
    }

    @PostMapping("/response/{responseId}")
    public Result addByResponse(@PathVariable Integer responseId,
                                @RequestBody @Validated(validationTime.Add.class) AnswerDetail answerDetail)
    {
        answerDetail.setResponseId(responseId);
        return this.add(answerDetail);
    }

    @SaCheckLogin
    @DeleteMapping("/{answerDetailId}")
    public Result del(@PathVariable Integer answerDetailId) {
        answerDetailService.del(answerDetailId);
        return Result.success();
    }

    @SaCheckLogin
    @DeleteMapping("/response/{responseId}/{answerDetailId}")
    public Result delByResponse(@PathVariable Integer responseId, @PathVariable Integer answerDetailId) {
        answerDetailService.delByResponse(responseId, answerDetailId);
        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/{answerDetailId}")
    public Result get(@PathVariable Integer answerDetailId) {
        return Result.successData(answerDetailService.get(answerDetailId));
    }

    @SaCheckLogin
    @GetMapping("/response/{responseId}/{answerDetailId}")
    public Result getByResponse(@PathVariable Integer responseId,
                                @PathVariable Integer answerDetailId)
    {
        return Result.successData(answerDetailService.getByResponse(responseId, answerDetailId));
    }

    @SaCheckLogin
    @GetMapping("/response/{responseId}")
    public Result getAllByResponse(@PathVariable Integer responseId) {
        return Result.successData(answerDetailService.getAllByResponse(responseId));
    }
}
