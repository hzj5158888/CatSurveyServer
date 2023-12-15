package com.codecat.catsurvey.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Option;
import com.codecat.catsurvey.repository.OptionRepository;
import com.codecat.catsurvey.repository.QuestionRepository;
import com.codecat.catsurvey.utils.Result;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.service.OptionService;
import com.codecat.catsurvey.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/option")
@CrossOrigin()
public class OptionController {
    @Autowired
    private OptionService optionService;

    @SaCheckLogin
    @PostMapping("")
    public Result add(@RequestBody @Validated({validationTime.FullAdd.class}) Option option) {
        optionService.add(option);
        return Result.successData(option.getId());
    }

    @SaCheckLogin
    @PostMapping("/question/{questionId}")
    public Result addByQuestion(@PathVariable Integer questionId,
                                @RequestBody @Validated({validationTime.Add.class}) Option option)
    {
        option.setQuestionId(questionId);
        optionService.add(option);
        return Result.successData(option.getId());
    }

    @SaCheckLogin
    @DeleteMapping("/{optionId}")
    public Result del(@PathVariable Integer optionId) {
        optionService.del(optionId);
        return Result.success();
    }

    @SaCheckLogin
    @DeleteMapping("/question/{questionId}/{optionId}")
    public Result delByQuestion(@PathVariable Integer questionId, @PathVariable Integer optionId) {
        optionService.delByQuestion(questionId, optionId);
        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/question/{questionId}")
    public Result setByQuestion(@PathVariable Integer questionId, @RequestBody List<Option> options) {
        optionService.setByQuestion(questionId, options);
        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/{optionId}")
    public Result modify(@PathVariable Integer optionId, @RequestBody Option newOption) {
        optionService.modify(optionId, newOption);
        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/question/{questionId}/{optionId}")
    public Result modifyByQuestion(@PathVariable Integer questionId,
                                   @PathVariable Integer optionId,
                                   @RequestBody Option newOption)
    {
        optionService.modifyByQuestion(questionId, optionId, newOption);
        return Result.success();
    }

    @SaCheckLogin
    @GetMapping("/{optionId}")
    public Result get(@PathVariable Integer optionId) {
        return Result.successData(optionService.get(optionId));
    }

    @SaCheckLogin
    @GetMapping("/question/{questionId}/{optionId}")
    public Result getByQuestion(@PathVariable Integer questionId, @PathVariable Integer optionId) {
        return Result.successData(optionService.getByQuestion(questionId, optionId));
    }

    @SaCheckLogin
    @GetMapping("/question/{questionId}")
    public Result getAllByQuestion(@PathVariable Integer questionId) {
        return Result.successData(optionService.getAllByQuestion(questionId));
    }
}
