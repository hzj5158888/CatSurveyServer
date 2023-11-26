package com.codecat.catsurvey.commcon.valid.function.survey;

import com.codecat.catsurvey.commcon.Enum.survey.SurveyStatusEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

public class SurveyStatusExistsValid implements ConstraintValidator<SurveyStatusExists, String> {
    @Override
    public boolean isValid(String status, ConstraintValidatorContext constraintValidatorContext) {
        if (status == null) {
            System.out.println("SurveyStatusExistsValid: status is null, skip");
            return true;
        }

        Set<String> nameSet = new HashSet<>();
        for (SurveyStatusEnum surveyStatusEnum : SurveyStatusEnum.values()) {
            nameSet.add(surveyStatusEnum.getName());
        }
        return nameSet.contains(status);
    }
}
