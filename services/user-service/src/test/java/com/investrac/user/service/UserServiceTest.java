package com.investrac.user.service;

import com.investrac.user.config.AesEncryptionService;
import com.investrac.user.dto.request.*;
import com.investrac.user.dto.response.*;
import com.investrac.user.entity.UserPreference;
import com.investrac.user.entity.UserProfile;
import com.investrac.user.entity.UserProfile.*;
import com.investrac.user.exception.UserException;
import com.investrac.user.mapper.UserMapper;
import com.investrac.user.repository.UserPreferenceRepository;
import com.investrac.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock UserProfileRepository    profileRepository;
    @Mock UserPreferenceRepository preferenceRepository;
    @Mock AesEncryptionService     aesEncryptionService;
    @Mock UserMapper               mapper;

    @InjectMocks
    UserService userService;

    private static final Long USER_ID = 100L;
    private UserProfile mockProfile;

    @BeforeEach
    void setUp() {
        mockProfile = UserProfile.builder()
            .id(1L).userId(USER_ID).name("Arjun Kumar")
            .email("arjun@investrac.in").phone("9876543210")
            .riskProfile(RiskProfile.MODERATE).taxRegime(TaxRegime.NEW)
            .language(Language.EN).currency("INR").theme("light")
            .build();
    }

    // ── CREATE PROFILE ─────────────────────────────────────────

    @Nested @DisplayName("createProfile()")
    class CreateProfileTests {

        @Test
        @DisplayName("creates profile and default preferences for new user")
        void create_NewUser_CreatesProfileAndPreferences() {
            CreateProfileRequest req = new CreateProfileRequest();
            req.setUserId(USER_ID);
            req.setName("Arjun Kumar");
            req.setEmail("arjun@investrac.in");

            when(profileRepository.existsByUserId(USER_ID)).thenReturn(false);
            when(profileRepository.save(any())).thenReturn(mockProfile);
            when(preferenceRepository.save(any())).thenReturn(UserPreference.builder().userId(USER_ID).build());
            when(mapper.toResponse(mockProfile)).thenReturn(
                UserProfileResponse.builder().userId(USER_ID).name("Arjun Kumar").build());

            UserProfileResponse result = userService.createProfile(req);

            assertThat(result.getName()).isEqualTo("Arjun Kumar");
            verify(profileRepository).save(any(UserProfile.class));
            verify(preferenceRepository).save(any(UserPreference.class));
        }

        @Test
        @DisplayName("returns existing profile idempotently if already created")
        void create_ExistingUser_ReturnsExisting() {
            CreateProfileRequest req = new CreateProfileRequest();
            req.setUserId(USER_ID);
            req.setName("Arjun Kumar");
            req.setEmail("arjun@investrac.in");

            when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);
            when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockProfile));
            when(mapper.toResponse(mockProfile)).thenReturn(
                UserProfileResponse.builder().userId(USER_ID).build());

            userService.createProfile(req);

            // Must NOT create a duplicate
            verify(profileRepository, never()).save(any());
        }
    }

    // ── GET PROFILE ────────────────────────────────────────────

    @Test
    @DisplayName("getProfile: returns profile for existing user")
    void getProfile_ExistingUser_ReturnsProfile() {
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockProfile));
        when(mapper.toResponse(mockProfile)).thenReturn(
            UserProfileResponse.builder().userId(USER_ID).name("Arjun Kumar").build());

        UserProfileResponse result = userService.getProfile(USER_ID);
        assertThat(result.getName()).isEqualTo("Arjun Kumar");
    }

    @Test
    @DisplayName("getProfile: throws NOT_FOUND when profile missing")
    void getProfile_NotFound_ThrowsException() {
        when(profileRepository.findByUserId(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getProfile(999L))
            .isInstanceOf(UserException.class)
            .hasMessageContaining("not found");
    }

    // ── UPDATE PROFILE ─────────────────────────────────────────

    @Test
    @DisplayName("updateProfile: applies partial update — only non-null fields changed")
    void updateProfile_PartialUpdate() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setRiskProfile(RiskProfile.AGGRESSIVE);
        req.setTaxRegime(TaxRegime.OLD);
        req.setRetirementAge(55);

        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockProfile));
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        when(profileRepository.save(captor.capture())).thenReturn(mockProfile);
        when(mapper.toResponse(any())).thenReturn(UserProfileResponse.builder().build());

        userService.updateProfile(USER_ID, req);

        UserProfile saved = captor.getValue();
        assertThat(saved.getRiskProfile()).isEqualTo(RiskProfile.AGGRESSIVE);
        assertThat(saved.getTaxRegime()).isEqualTo(TaxRegime.OLD);
        assertThat(saved.getRetirementAge()).isEqualTo(55);
        // Name not in request — must remain unchanged
        assertThat(saved.getName()).isEqualTo("Arjun Kumar");
    }

    @Test
    @DisplayName("updateProfile: name is trimmed before saving")
    void updateProfile_NameTrimmed() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setName("  Arjun Kumar  ");

        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockProfile));
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        when(profileRepository.save(captor.capture())).thenReturn(mockProfile);
        when(mapper.toResponse(any())).thenReturn(UserProfileResponse.builder().build());

        userService.updateProfile(USER_ID, req);

        assertThat(captor.getValue().getName()).isEqualTo("Arjun Kumar");
    }

    // ── KYC ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateKyc: PAN is encrypted before storing, never stored plaintext")
    void updateKyc_PanEncryptedBeforeStorage() {
        UpdateKycRequest req = new UpdateKycRequest();
        req.setPan("ABCDE1234F");
        req.setAadhaarLast4("5678");

        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockProfile));
        when(aesEncryptionService.encrypt("ABCDE1234F")).thenReturn("ENCRYPTED_PAN");
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        when(profileRepository.save(captor.capture())).thenReturn(mockProfile);
        when(mapper.toResponse(any())).thenReturn(UserProfileResponse.builder().build());

        userService.updateKyc(USER_ID, req);

        UserProfile saved = captor.getValue();
        // Encrypted value stored, NOT plaintext
        assertThat(saved.getPanEncrypted()).isEqualTo("ENCRYPTED_PAN");
        assertThat(saved.getPanEncrypted()).doesNotContain("ABCDE1234F");
        assertThat(saved.getAadhaarLast4()).isEqualTo("5678");
        // KYC auto-verified when both present
        assertThat(saved.isKycVerified()).isTrue();
    }

    @Test
    @DisplayName("updateKyc: KYC not verified when only Aadhaar provided (no PAN)")
    void updateKyc_OnlyAadhaar_NotVerified() {
        UpdateKycRequest req = new UpdateKycRequest();
        req.setAadhaarLast4("1234");
        // No PAN

        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockProfile));
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        when(profileRepository.save(captor.capture())).thenReturn(mockProfile);
        when(mapper.toResponse(any())).thenReturn(UserProfileResponse.builder().build());

        userService.updateKyc(USER_ID, req);

        assertThat(captor.getValue().isKycVerified()).isFalse();
    }

    @Test
    @DisplayName("updateKyc: PAN is uppercased before encryption")
    void updateKyc_PanUppercasedBeforeEncryption() {
        UpdateKycRequest req = new UpdateKycRequest();
        req.setPan("abcde1234f");   // lowercase input

        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockProfile));
        when(aesEncryptionService.encrypt("ABCDE1234F")).thenReturn("ENCRYPTED");
        when(profileRepository.save(any())).thenReturn(mockProfile);
        when(mapper.toResponse(any())).thenReturn(UserProfileResponse.builder().build());

        userService.updateKyc(USER_ID, req);

        // Must uppercase before encrypting
        verify(aesEncryptionService).encrypt("ABCDE1234F");
    }

    // ── PREFERENCES ────────────────────────────────────────────

    @Test
    @DisplayName("updatePreferences: partial update applies only non-null values")
    void updatePreferences_PartialUpdate() {
        UserPreference existing = UserPreference.builder()
            .userId(USER_ID).aiInsightsEnabled(true).autoLockMinutes(5).build();

        UpdatePreferenceRequest req = new UpdatePreferenceRequest();
        req.setAutoLockMinutes(15);
        req.setAiLanguage("hi");
        // aiInsightsEnabled not in request — must remain true

        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
        ArgumentCaptor<UserPreference> captor = ArgumentCaptor.forClass(UserPreference.class);
        when(preferenceRepository.save(captor.capture())).thenReturn(existing);
        when(mapper.toPreferenceResponse(any())).thenReturn(UserPreferenceResponse.builder().build());

        userService.updatePreferences(USER_ID, req);

        UserPreference saved = captor.getValue();
        assertThat(saved.getAutoLockMinutes()).isEqualTo(15);
        assertThat(saved.getAiLanguage()).isEqualTo("hi");
        assertThat(saved.isAiInsightsEnabled()).isTrue();  // unchanged
    }

    @Test
    @DisplayName("getPreferences: creates default preferences if none exist")
    void getPreferences_NoExisting_CreatesDefaults() {
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        UserPreference newPrefs = UserPreference.builder()
            .userId(USER_ID).autoLockMinutes(5).build();
        when(preferenceRepository.save(any())).thenReturn(newPrefs);
        when(mapper.toPreferenceResponse(any())).thenReturn(
            UserPreferenceResponse.builder().autoLockMinutes(5).build());

        UserPreferenceResponse result = userService.getPreferences(USER_ID);

        assertThat(result.getAutoLockMinutes()).isEqualTo(5);
        verify(preferenceRepository).save(argThat(p -> p.getUserId() == USER_ID));
    }
}
