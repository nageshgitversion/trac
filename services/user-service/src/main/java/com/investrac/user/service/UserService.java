package com.investrac.user.service;

import com.investrac.common.dto.ErrorCodes;
import com.investrac.user.config.AesEncryptionService;
import com.investrac.user.dto.request.*;
import com.investrac.user.dto.response.*;
import com.investrac.user.entity.UserPreference;
import com.investrac.user.entity.UserProfile;
import com.investrac.user.exception.UserException;
import com.investrac.user.mapper.UserMapper;
import com.investrac.user.repository.UserPreferenceRepository;
import com.investrac.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User profile management service.
 *
 * Responsibilities:
 *  - Create profile on first registration (triggered by UserRegisteredEvent)
 *  - Read/update profile and preferences
 *  - KYC submission with AES-256 PAN encryption
 *  - Financial profile (risk, tax regime, income)
 *  - App preferences (theme, language, biometric)
 *
 * Cross-service data (wallet balance, portfolio) is assembled by
 * UserAggregatorService via HTTP calls to other services.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository    profileRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final AesEncryptionService     aesEncryptionService;
    private final UserMapper               mapper;

    // ══════════════════════════════════════════
    // CREATE PROFILE (called by Kafka consumer on registration)
    // ══════════════════════════════════════════
    @Transactional
    public UserProfileResponse createProfile(CreateProfileRequest req) {
        if (profileRepository.existsByUserId(req.getUserId())) {
            log.warn("Profile already exists for userId={} — skipping creation", req.getUserId());
            return mapper.toResponse(profileRepository.findByUserId(req.getUserId()).orElseThrow());
        }

        UserProfile profile = UserProfile.builder()
            .userId(req.getUserId())
            .name(req.getName().trim())
            .email(req.getEmail().toLowerCase().trim())
            .phone(req.getPhone())
            .build();
        profile = profileRepository.save(profile);

        // Create default preferences
        UserPreference prefs = UserPreference.builder()
            .userId(req.getUserId())
            .build();
        preferenceRepository.save(prefs);

        log.info("User profile created for userId={}", req.getUserId());
        return mapper.toResponse(profile);
    }

    // ══════════════════════════════════════════
    // GET PROFILE
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        return mapper.toResponse(findProfile(userId));
    }

    // ══════════════════════════════════════════
    // UPDATE PROFILE
    // ══════════════════════════════════════════
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest req) {
        UserProfile profile = findProfile(userId);

        if (req.getName()           != null) profile.setName(req.getName().trim());
        if (req.getPhone()          != null) profile.setPhone(req.getPhone());
        if (req.getDateOfBirth()    != null) profile.setDateOfBirth(req.getDateOfBirth());
        if (req.getMonthlyIncome()  != null) profile.setMonthlyIncome(req.getMonthlyIncome());
        if (req.getRiskProfile()    != null) profile.setRiskProfile(req.getRiskProfile());
        if (req.getTaxRegime()      != null) profile.setTaxRegime(req.getTaxRegime());
        if (req.getRetirementAge()  != null) profile.setRetirementAge(req.getRetirementAge());
        if (req.getFinancialGoal()  != null) profile.setFinancialGoal(req.getFinancialGoal());
        if (req.getLanguage()       != null) profile.setLanguage(req.getLanguage());
        if (req.getTheme()          != null) profile.setTheme(req.getTheme());
        if (req.getBiometricEnabled() != null) profile.setBiometricEnabled(req.getBiometricEnabled());

        profile = profileRepository.save(profile);
        log.info("Profile updated for userId={}", userId);
        return mapper.toResponse(profile);
    }

    // ══════════════════════════════════════════
    // KYC UPDATE — PAN encrypted, Aadhaar last 4 only
    // ══════════════════════════════════════════
    @Transactional
    public UserProfileResponse updateKyc(Long userId, UpdateKycRequest req) {
        UserProfile profile = findProfile(userId);

        if (req.getPan() != null && !req.getPan().isBlank()) {
            // Encrypt PAN before storing — NEVER log the plaintext
            String encrypted = aesEncryptionService.encrypt(req.getPan().toUpperCase());
            profile.setPanEncrypted(encrypted);
            log.info("PAN encrypted and stored for userId={}", userId);
        }

        if (req.getAadhaarLast4() != null) {
            profile.setAadhaarLast4(req.getAadhaarLast4());
        }

        // Auto-set KYC verified when both PAN and Aadhaar are present
        if (profile.hasPan() && profile.getAadhaarLast4() != null) {
            profile.setKycVerified(true);
            log.info("KYC auto-verified for userId={}", userId);
        }

        profileRepository.save(profile);
        return mapper.toResponse(profile);
    }

    // ══════════════════════════════════════════
    // PREFERENCES — GET + UPDATE
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public UserPreferenceResponse getPreferences(Long userId) {
        UserPreference prefs = preferenceRepository.findByUserId(userId)
            .orElseGet(() -> {
                UserPreference defaultPrefs = UserPreference.builder().userId(userId).build();
                return preferenceRepository.save(defaultPrefs);
            });
        return mapper.toPreferenceResponse(prefs);
    }

    @Transactional
    public UserPreferenceResponse updatePreferences(Long userId, UpdatePreferenceRequest req) {
        UserPreference prefs = preferenceRepository.findByUserId(userId)
            .orElseGet(() -> preferenceRepository.save(
                UserPreference.builder().userId(userId).build()));

        if (req.getShowBalanceOnHome()        != null) prefs.setShowBalanceOnHome(req.getShowBalanceOnHome());
        if (req.getShowPortfolioOnHome()      != null) prefs.setShowPortfolioOnHome(req.getShowPortfolioOnHome());
        if (req.getAiInsightsEnabled()        != null) prefs.setAiInsightsEnabled(req.getAiInsightsEnabled());
        if (req.getAiLanguage()               != null) prefs.setAiLanguage(req.getAiLanguage());
        if (req.getAutoLockMinutes()          != null) prefs.setAutoLockMinutes(req.getAutoLockMinutes());
        if (req.getShowAmountsInLakhs()       != null) prefs.setShowAmountsInLakhs(req.getShowAmountsInLakhs());
        if (req.getWalletBudgetAlertPercent() != null) prefs.setWalletBudgetAlertPercent(req.getWalletBudgetAlertPercent());

        prefs = preferenceRepository.save(prefs);
        log.info("Preferences updated for userId={}", userId);
        return mapper.toPreferenceResponse(prefs);
    }

    // ══════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════
    private UserProfile findProfile(Long userId) {
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> new UserException(
                ErrorCodes.USER_NOT_FOUND,
                "User profile not found for userId=" + userId,
                HttpStatus.NOT_FOUND));
    }
}
