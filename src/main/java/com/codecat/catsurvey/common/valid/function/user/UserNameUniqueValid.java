package com.codecat.catsurvey.common.valid.function.user;

import com.codecat.catsurvey.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UserNameUniqueValid implements ConstraintValidator<UserNameUnique, String> {
    @Autowired
    UserRepository userRepository;

    @Override
    public boolean isValid(String userName, ConstraintValidatorContext constraintValidatorContext) {
        if (userName == null) {
            System.out.println("UserNameUniqueValid: userName is null, skip");
            return true;
        }

        return !userRepository.existsByUserName(userName);
    }
}
