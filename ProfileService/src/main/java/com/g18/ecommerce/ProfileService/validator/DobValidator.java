package com.g18.ecommerce.ProfileService.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

public class DobValidator implements ConstraintValidator<DobConstraint, Date> {
    private int min;

    @Override
    public void initialize(DobConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(Date value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) return true;
        int years = Period.between(value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()).getYears();
        return years >= min;
    }


}
