package com.codecat.catsurvey.commcon.valid.function.question;

import com.codecat.catsurvey.commcon.valid.function.survey.SurveyIdExistsValid;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = QuestionIdExistsValid.class)
public @interface QuestionIdExists {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
