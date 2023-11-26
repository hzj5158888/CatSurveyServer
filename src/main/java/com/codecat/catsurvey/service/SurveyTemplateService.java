package com.codecat.catsurvey.service;

import com.codecat.catsurvey.commcon.models.SurveyTemplate;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class SurveyTemplateService {
    @Validated(validationTime.FullUpdate.class)
    public void checkFullUpdate(@Valid SurveyTemplate surveyTemplate) {}
}
