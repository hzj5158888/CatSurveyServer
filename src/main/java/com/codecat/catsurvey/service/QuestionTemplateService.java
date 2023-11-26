package com.codecat.catsurvey.service;

import com.codecat.catsurvey.commcon.exception.ValidationException;
import com.codecat.catsurvey.commcon.models.QuestionTemplate;
import com.codecat.catsurvey.commcon.repository.QuestionTemplateRepository;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Service
@Validated
public class QuestionTemplateService {
    @Autowired
    private QuestionTemplateRepository questionTemplateRepository;

    public void setIOrder(Integer questionTemplateId, Integer iOrder) {
        QuestionTemplate questionTemplate = questionTemplateRepository.findById(questionTemplateId).orElseThrow(() ->
                new ValidationException("问题模板不存在")
        );

        List<QuestionTemplate> sortedQuestionTemplate = questionTemplateRepository.findAllByTemplateId(
                questionTemplate.getTemplateId(), Sort.by("iOrder")
        );

        if (iOrder == null || iOrder >= sortedQuestionTemplate.size())
            iOrder = Math.max(sortedQuestionTemplate.size() - 1, 0);
        if (iOrder < 0)
            throw new ValidationException("iOrder非法");

        List<QuestionTemplate> res = new ArrayList<>();
        for (int i = 0; i < sortedQuestionTemplate.size(); i++)
        {
            sortedQuestionTemplate.get(i).setIOrder(i);

            Integer curId = sortedQuestionTemplate.get(i).getId();
            if (questionTemplateId.equals(curId) && !iOrder.equals(i))
                continue;
            else if (iOrder.equals(i))
            {
                res.add(questionTemplate);
                iOrder = -1;
                if (!curId.equals(questionTemplateId))
                    i--;

                continue;
            }

            res.add(sortedQuestionTemplate.get(i));
        }
        if (!iOrder.equals(-1))
            res.add(questionTemplate);

        for (int i = 0; i < res.size(); i++)
            res.get(i).setIOrder(i);

        questionTemplateRepository.saveAllAndFlush(res);
    }

    @Validated(validationTime.FullAdd.class)
    public void checkFullAdd(@Valid QuestionTemplate questionTemplate) {}

    @Validated(validationTime.FullUpdate.class)
    public void checkFullUpdate(@Valid QuestionTemplate questionTemplate) {}
}
