package com.codecat.catsurvey.common.valid.function.user;

import com.codecat.catsurvey.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UserIdExistsValid implements ConstraintValidator<UserIdExists, Integer> {
    @Autowired
    UserRepository userRepository;

    @Override
    public boolean isValid(Integer userId, ConstraintValidatorContext constraintValidatorContext) {
        if (userId == null) {
            System.out.println("UserIdUniqueValid: userId is null, skip");
            return true;
        }

        return userRepository.existsById(userId);
    }
}
