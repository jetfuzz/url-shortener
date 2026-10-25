package com.github.jetfuzz.urlshortener.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.Set;

public class NotReservedValidator implements ConstraintValidator<NotReserved, String> {
  private static final Set<String> RESERVED =
      Set.of("shorten", "actuator", "error", "swagger-ui", "api", "admin", "login");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext ctx) {
    return value == null || !RESERVED.contains(value.toLowerCase(Locale.ROOT));
  }
}
