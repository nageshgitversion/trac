package com.investrac.user.dto.request;

import com.investrac.user.entity.UserProfile;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100)
    private String name;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String phone;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @DecimalMin(value = "0")
    private Long monthlyIncome;

    private UserProfile.RiskProfile riskProfile;

    private UserProfile.TaxRegime taxRegime;

    @Min(value = 40, message = "Retirement age must be at least 40")
    @Max(value = 80, message = "Retirement age cannot exceed 80")
    private Integer retirementAge;

    @Size(max = 200)
    private String financialGoal;

    private UserProfile.Language language;

    @Size(max = 10)
    private String theme;

    private Boolean biometricEnabled;
}
