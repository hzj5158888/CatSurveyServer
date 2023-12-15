package com.codecat.catsurvey.common.valid.function.survey;

import com.codecat.catsurvey.repository.SurveyRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class SurveyIdExistsValid implements ConstraintValidator<SurveyIdExists, Integer> {
    @Autowired
    private SurveyRepository surveyRepository;

    @Override
    public boolean isValid(Integer surveyId, ConstraintValidatorContext constraintValidatorContext) {
        if (surveyId == null) {
            System.out.println("SurveyIdExistsValid: surveyId is null, skip");
            return true;
        }

        return surveyRepository.existsById(surveyId);
    }
}
