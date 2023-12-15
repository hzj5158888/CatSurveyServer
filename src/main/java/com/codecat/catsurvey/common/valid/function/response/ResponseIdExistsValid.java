package com.codecat.catsurvey.common.valid.function.response;

import com.codecat.catsurvey.repository.ResponseRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ResponseIdExistsValid implements ConstraintValidator<ResponseIdExists, Integer> {
    @Autowired
    private ResponseRepository responseRepository;

    @Override
    public boolean isValid(Integer responseId, ConstraintValidatorContext constraintValidatorContext) {
        if (responseId == null) {
            System.out.println("ResponseIdExistsValid: responseId is null, skip");
            return true;
        }

        return responseRepository.existsById(responseId);
    }
}
