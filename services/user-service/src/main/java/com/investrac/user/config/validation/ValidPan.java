package com.investrac.user.config.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates Indian PAN format: ABCDE1234F
 * - 5 uppercase letters
 * - 4 digits
 * - 1 uppercase letter
 * 4th char = entity type (P=Person, C=Company, H=HUF, F=Firm, etc.)
 */
@Documented
@Constraint(validatedBy = PanValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPan {
    String message() default "Invalid PAN format. Expected: ABCDE1234F (5 letters, 4 digits, 1 letter)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
