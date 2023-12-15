package com.codecat.catsurvey.service;

import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.common.valid.group.validationTime;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class SurveyService {
    @Validated(validationTime.FullUpdate.class)
    public void checkFullUpdate(@Valid Survey survey) {}
}
