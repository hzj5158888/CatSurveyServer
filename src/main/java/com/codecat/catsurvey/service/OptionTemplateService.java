package com.codecat.catsurvey.service;

import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.OptionTemplate;
import com.codecat.catsurvey.repository.OptionTemplateRepository;
import com.codecat.catsurvey.common.valid.group.validationTime;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Service
@Validated
public class OptionTemplateService {
    @Autowired
    private OptionTemplateRepository optionTemplateRepository;

    public void setIOrder(Integer optionTemplateId, Integer iOrder) {
        OptionTemplate optionTemplate = optionTemplateRepository.findById(optionTemplateId).orElseThrow(() ->
                new CatValidationException("选项不存在")
        );

        List<OptionTemplate> sortedOptionTemplate = optionTemplateRepository.findAllByQuestionTemplateId(
                optionTemplate.getQuestionTemplateId(), Sort.by("iOrder")
        );

        if (iOrder == null || iOrder >= sortedOptionTemplate.size())
            iOrder = Math.max(sortedOptionTemplate.size() - 1, 0);
        if (iOrder < 0)
            throw new CatValidationException("iOrder非法");

        List<OptionTemplate> res = new ArrayList<>();
        for (int i = 0; i < sortedOptionTemplate.size(); i++)
        {
            sortedOptionTemplate.get(i).setIOrder(i);

            Integer curId = sortedOptionTemplate.get(i).getId();
            if (optionTemplateId.equals(curId) && !iOrder.equals(i))
                continue;
            else if (iOrder.equals(i))
            {
                res.add(optionTemplate);
                iOrder = -1;
                if (!curId.equals(optionTemplateId))
                    i--;

                continue;
            }

            res.add(sortedOptionTemplate.get(i));
        }
        if (!iOrder.equals(-1))
            res.add(optionTemplate);

        for (int i = 0; i < res.size(); i++)
            res.get(i).setIOrder(i);

        optionTemplateRepository.saveAllAndFlush(res);
    }

    @Validated(validationTime.FullAdd.class)
    public void checkFullAdd(@Valid OptionTemplate optionTemplate) {}

    @Validated(validationTime.FullUpdate.class)
    public void checkFullUpdate(@Valid OptionTemplate optionTemplate) {}
}
