package com.investrac.account.config.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Class-level constraint: maturityDate must be after startDate.
 * Applied on CreateAccountRequest to validate the date range together.
 */
@Documented
@Constraint(validatedBy = MaturityAfterStartValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaturityAfterStart {
    String message() default "Maturity date must be after start date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
