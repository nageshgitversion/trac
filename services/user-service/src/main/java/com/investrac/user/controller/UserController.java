package com.investrac.user.controller;

import com.investrac.common.response.ApiResponse;
import com.investrac.user.dto.request.*;
import com.investrac.user.dto.response.*;
import com.investrac.user.service.FinancialSummaryService;
import com.investrac.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "Profile management, KYC, financial profile, app preferences")
public class UserController {

    private final UserService            userService;
    private final FinancialSummaryService summaryService;

    // ── GET MY PROFILE ──────────────────────────────────────
    @GetMapping("/me")
    @Operation(summary = "Get current user's full profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userId)));
    }

    // ── HOME SCREEN SUMMARY ─────────────────────────────────
    @GetMapping("/me/summary")
    @Operation(
        summary     = "Get aggregated financial summary for home screen",
        description = "Aggregates wallet balance, portfolio value, account totals, " +
                      "and net worth. Results are cached in Redis for 5 minutes."
    )
    public ResponseEntity<ApiResponse<FinancialSummaryResponse>> getFinancialSummary(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(summaryService.getSummary(userId)));
    }

    // ── UPDATE PROFILE ──────────────────────────────────────
    @PutMapping("/me")
    @Operation(summary = "Update profile — name, DOB, income, risk profile, tax regime")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            userService.updateProfile(userId, request),
            "Profile updated successfully"));
    }

    // ── UPDATE KYC ──────────────────────────────────────────
    @PutMapping("/me/kyc")
    @Operation(
        summary     = "Submit KYC — PAN and Aadhaar last 4 digits",
        description = "PAN is encrypted with AES-256-GCM before storage. " +
                      "Only last 4 digits of Aadhaar are accepted and stored. " +
                      "KYC is auto-verified when both are provided."
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateKyc(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateKycRequest request) {
        log.info("KYC update for userId={}", userId);
        return ResponseEntity.ok(ApiResponse.success(
            userService.updateKyc(userId, request),
            "KYC information saved successfully"));
    }

    // ── GET PREFERENCES ─────────────────────────────────────
    @GetMapping("/me/preferences")
    @Operation(summary = "Get app preferences")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> getPreferences(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
            userService.getPreferences(userId)));
    }

    // ── UPDATE PREFERENCES ──────────────────────────────────
    @PutMapping("/me/preferences")
    @Operation(summary = "Update app preferences — theme, language, display settings")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> updatePreferences(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdatePreferenceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            userService.updatePreferences(userId, request),
            "Preferences updated successfully"));
    }
}
