package com.github.jetfuzz.urlshortener.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotReservedValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotReserved {

  String message() default "Alias is reserved";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
