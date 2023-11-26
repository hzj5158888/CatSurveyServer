package com.codecat.catsurvey.commcon.valid.function.survey;

import com.codecat.catsurvey.commcon.valid.function.user.UserIdExistsValid;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SurveyIdExistsValid.class)
public @interface SurveyIdExists {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
