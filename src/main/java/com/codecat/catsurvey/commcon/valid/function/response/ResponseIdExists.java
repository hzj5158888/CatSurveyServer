package com.codecat.catsurvey.commcon.valid.function.response;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ResponseIdExistsValid.class)
public @interface ResponseIdExists {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
