package com.investrac.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class UserProfileResponse {
    private Long   id;
    private Long   userId;
    private String name;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Integer age;
    private String profilePhotoUrl;

    // Financial profile
    private Long   monthlyIncome;
    private String riskProfile;
    private String taxRegime;
    private Integer retirementAge;
    private String financialGoal;

    // KYC — never expose PAN or full Aadhaar
    private boolean kycVerified;
    private boolean hasPan;
    private String  aadhaarLast4;

    // Preferences
    private String language;
    private String currency;
    private String theme;
    private boolean biometricEnabled;

    private Instant createdAt;
    private Instant updatedAt;
}
