package com.investrac.account.config.validation;

import com.investrac.account.dto.request.CreateAccountRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that maturityDate > startDate when both are present.
 * Only enforced for FD and RD account types.
 */
public class MaturityAfterStartValidator
    implements ConstraintValidator<MaturityAfterStart, CreateAccountRequest> {

    @Override
    public boolean isValid(CreateAccountRequest req, ConstraintValidatorContext ctx) {
        if (req == null) return true;

        // Only FD and RD require maturity date
        if (req.getType() == null) return true;
        String type = req.getType().name();
        if (!type.equals("FD") && !type.equals("RD")) return true;

        if (req.getStartDate() == null || req.getMaturityDate() == null) return true;

        boolean valid = req.getMaturityDate().isAfter(req.getStartDate());
        if (!valid) {
            // Point the error at maturityDate field specifically
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("Maturity date must be after start date")
               .addPropertyNode("maturityDate")
               .addConstraintViolation();
        }
        return valid;
    }
}
