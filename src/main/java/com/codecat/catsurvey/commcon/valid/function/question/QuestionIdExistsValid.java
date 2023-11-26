package com.codecat.catsurvey.commcon.valid.function.question;

import com.codecat.catsurvey.commcon.repository.QuestionRepository;
import com.codecat.catsurvey.commcon.repository.SurveyRepository;
import com.codecat.catsurvey.commcon.valid.function.survey.SurveyIdExists;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class QuestionIdExistsValid implements ConstraintValidator<QuestionIdExists, Integer> {
    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public boolean isValid(Integer questionId, ConstraintValidatorContext constraintValidatorContext) {
        if (questionId == null) {
            System.out.println("ResponseIdExistsValid: surveyId is null, skip");
            return true;
        }

        return questionRepository.existsById(questionId);
    }
}
