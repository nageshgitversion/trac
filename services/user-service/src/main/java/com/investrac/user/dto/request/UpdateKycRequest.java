package com.investrac.user.dto.request;

import com.investrac.user.config.validation.ValidPan;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * KYC update — PAN and Aadhaar last 4 digits.
 *
 * SECURITY:
 *  - PAN is validated by format, then encrypted with AES-256 before storing
 *  - Only last 4 digits of Aadhaar accepted — we never store full Aadhaar
 *  - These fields must NEVER appear in logs
 */
@Data
public class UpdateKycRequest {

    @ValidPan    // Custom validator: ABCDE1234F pattern
    private String pan;

    @Pattern(regexp = "^\\d{4}$", message = "Provide last 4 digits of Aadhaar only")
    private String aadhaarLast4;
}
