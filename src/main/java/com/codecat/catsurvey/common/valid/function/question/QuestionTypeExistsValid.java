package com.codecat.catsurvey.common.valid.function.question;

import com.codecat.catsurvey.common.Enum.question.QuestionTypeEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

public class QuestionTypeExistsValid implements ConstraintValidator<QuestionTypeExists, String> {

    @Override
    public boolean isValid(String questionType, ConstraintValidatorContext constraintValidatorContext) {
        if (questionType == null) {
            System.out.println("QuestionTypeExistsValid: questionType is null, skip");
            return true;
        }

        Set<String> typeName = new HashSet<>();
        for (QuestionTypeEnum questionTypeEnum : QuestionTypeEnum.values()) {
            typeName.add(questionTypeEnum.getName());
        }

        return typeName.contains(questionType);
    }
}
