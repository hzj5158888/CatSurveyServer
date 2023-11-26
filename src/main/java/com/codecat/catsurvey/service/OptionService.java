package com.codecat.catsurvey.service;

import com.codecat.catsurvey.commcon.exception.ValidationException;
import com.codecat.catsurvey.commcon.models.Option;
import com.codecat.catsurvey.commcon.models.Question;
import com.codecat.catsurvey.commcon.repository.OptionRepository;
import com.codecat.catsurvey.commcon.repository.QuestionRepository;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Validated
public class OptionService {
    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Validated(value = {validationTime.FullAdd.class})
    public void checkFullAdd(@Valid Option option) {}

    @Validated(value = {validationTime.FullUpdate.class})
    public void checkFullUpdate(@Valid Option option) {}


    public void setIOrder(Integer optionId, Integer iOrder) {
        Option option = optionRepository.findById(optionId).orElseThrow(() ->
                new ValidationException("选项不存在")
        );

        List<Option> sortedOption = optionRepository.findAllByQuestionId(
                option.getQuestionId(), Sort.by("iOrder")
        );

        if (iOrder == null || iOrder >= sortedOption.size())
            iOrder = Math.max(sortedOption.size() - 1, 0);
        if (iOrder < 0)
            throw new ValidationException("iOrder非法");

        List<Option> res = new ArrayList<>();
        for (int i = 0; i < sortedOption.size(); i++)
        {
            sortedOption.get(i).setIOrder(i);

            Integer curId = sortedOption.get(i).getId();
            if (optionId.equals(curId) && !iOrder.equals(i))
                continue;
            else if (iOrder.equals(i))
            {
                res.add(option);
                iOrder = -1;
                if (!curId.equals(optionId))
                    i--;

                continue;
            }

            res.add(sortedOption.get(i));
        }
        if (!iOrder.equals(-1))
            res.add(option);

        for (int i = 0; i < res.size(); i++)
            res.get(i).setIOrder(i);

        optionRepository.saveAllAndFlush(res);
    }
}
