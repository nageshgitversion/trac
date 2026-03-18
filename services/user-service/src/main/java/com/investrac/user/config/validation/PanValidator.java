package com.investrac.user.config.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PanValidator implements ConstraintValidator<ValidPan, String> {

    // ABCDE1234F — 5 uppercase letters, 4 digits, 1 uppercase letter
    private static final Pattern PAN_PATTERN =
        Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    @Override
    public boolean isValid(String pan, ConstraintValidatorContext ctx) {
        if (pan == null || pan.isBlank()) return true; // Optional field
        return PAN_PATTERN.matcher(pan.toUpperCase()).matches();
    }
}
