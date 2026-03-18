package com.investrac.auth.config.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validates password strength:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one digit
 * - At least one special character
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT     = Pattern.compile("\\d");
    private static final Pattern SPECIAL   = Pattern.compile("[@#$%^&+=!*()_\\-]");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.length() < 8) return false;

        return UPPERCASE.matcher(password).find()
            && LOWERCASE.matcher(password).find()
            && DIGIT.matcher(password).find()
            && SPECIAL.matcher(password).find();
    }
}
