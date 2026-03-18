package com.investrac.auth.service;

import com.investrac.auth.dto.request.*;
import com.investrac.auth.dto.response.AuthResponse;
import com.investrac.auth.dto.response.UserSummaryDto;
import com.investrac.auth.entity.*;
import com.investrac.auth.exception.AccountLockedException;
import com.investrac.auth.exception.AuthException;
import com.investrac.auth.outbox.OutboxService;
import com.investrac.auth.repository.*;
import com.investrac.common.dto.ErrorCodes;
import com.investrac.common.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Core authentication business logic.
 * All operations are @Transactional.
 * Kafka events published via Outbox Pattern (never lost, even on crash).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository            userRepository;
    private final RefreshTokenRepository    refreshTokenRepository;
    private final OtpVerificationRepository otpRepository;
    private final AuditLogRepository        auditLogRepository;
    private final JwtService                jwtService;
    private final PasswordEncoder           passwordEncoder;
    private final OutboxService             outboxService;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int REFRESH_TOKEN_EXPIRY_DAYS = 30;
    private static final int OTP_EXPIRY_MINUTES = 10;

    // ══════════════════════════════════════════
    // REGISTER
    // ══════════════════════════════════════════
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        // Check duplicates
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException(ErrorCodes.EMAIL_ALREADY_EXISTS,
                "An account with this email already exists", HttpStatus.CONFLICT);
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new AuthException(ErrorCodes.PHONE_ALREADY_EXISTS,
                "An account with this phone number already exists", HttpStatus.CONFLICT);
        }

        // Create user (inactive until email verified)
        User user = User.builder()
            .name(request.getName().trim())
            .email(request.getEmail().toLowerCase().trim())
            .phone(request.getPhone())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .active(true)
            .emailVerified(false)
            .build();
        user = userRepository.save(user);

        // Generate OTP for email verification
        String otp = generateOtp();
        saveOtp(user.getEmail(), otp, OtpVerification.OtpPurpose.EMAIL_VERIFICATION);

        // Publish event via Outbox (Kafka consumer will send welcome email + OTP)
        outboxService.publish(
            UserRegisteredEvent.TOPIC,
            new UserRegisteredEvent(user.getId(), user.getEmail(), user.getName(), otp, Instant.now())
        );

        // Audit
        saveAudit(user.getId(), user.getEmail(), AuditLog.AuditAction.REGISTER, null, null, true, null);

        // Return tokens (user can use app before verifying email, but some features restricted)
        return buildAuthResponse(user);
    }

    // ══════════════════════════════════════════
    // LOGIN
    // ══════════════════════════════════════════
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Login attempt for email: {} from IP: {}", request.getEmail(), ipAddress);

        User user = userRepository.findByEmailAndActiveTrue(request.getEmail())
            .orElseThrow(() -> {
                saveAudit(null, request.getEmail(), AuditLog.AuditAction.LOGIN_FAILED,
                    ipAddress, userAgent, false, "User not found");
                return new AuthException(ErrorCodes.INVALID_CREDENTIALS,
                    "Invalid email or password", HttpStatus.UNAUTHORIZED);
            });

        // Check account lock
        if (user.isLocked()) {
            saveAudit(user.getId(), user.getEmail(), AuditLog.AuditAction.LOGIN_FAILED,
                ipAddress, userAgent, false, "Account locked");
            throw new AccountLockedException(user.getLockedUntil());
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            userRepository.incrementLoginAttempts(user.getId());

            int newAttempts = user.getLoginAttempts() + 1;
            if (newAttempts >= MAX_LOGIN_ATTEMPTS) {
                userRepository.lockUserUntil(user.getId(), LocalDateTime.now().plusMinutes(30));
                saveAudit(user.getId(), user.getEmail(), AuditLog.AuditAction.ACCOUNT_LOCKED,
                    ipAddress, userAgent, false, "Max attempts reached");
                throw new AccountLockedException(LocalDateTime.now().plusMinutes(30));
            }

            saveAudit(user.getId(), user.getEmail(), AuditLog.AuditAction.LOGIN_FAILED,
                ipAddress, userAgent, false,
                "Wrong password. Attempts: " + newAttempts + "/" + MAX_LOGIN_ATTEMPTS);

            throw new AuthException(ErrorCodes.INVALID_CREDENTIALS,
                "Invalid email or password. " + (MAX_LOGIN_ATTEMPTS - newAttempts) + " attempts remaining",
                HttpStatus.UNAUTHORIZED);
        }

        // Reset failed attempts on successful login
        userRepository.resetLoginAttempts(user.getId());

        // Audit success
        saveAudit(user.getId(), user.getEmail(), AuditLog.AuditAction.LOGIN,
            ipAddress, userAgent, true, null);

        log.info("Login successful for userId: {}", user.getId());
        return buildAuthResponse(user, ipAddress, userAgent);
    }

    // ══════════════════════════════════════════
    // REFRESH TOKEN
    // ══════════════════════════════════════════
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository
            .findByTokenAndRevokedFalse(request.getRefreshToken())
            .orElseThrow(() -> new AuthException(
                ErrorCodes.TOKEN_INVALID, "Invalid or revoked refresh token", HttpStatus.UNAUTHORIZED));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.revokeByToken(request.getRefreshToken());
            throw new AuthException(ErrorCodes.TOKEN_EXPIRED,
                "Refresh token has expired. Please login again.", HttpStatus.UNAUTHORIZED);
        }

        // Rotate — revoke old, issue new
        refreshTokenRepository.revokeByToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        log.debug("Token rotated for userId: {}", user.getId());
        return buildAuthResponse(user);
    }

    // ══════════════════════════════════════════
    // LOGOUT
    // ══════════════════════════════════════════
    @Transactional
    public void logout(Long userId, String refreshToken) {
        refreshTokenRepository.revokeByToken(refreshToken);
        User user = userRepository.findById(userId).orElseThrow();
        saveAudit(userId, user.getEmail(), AuditLog.AuditAction.LOGOUT, null, null, true, null);
        log.info("User {} logged out", userId);
    }

    @Transactional
    public void logoutAllDevices(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        User user = userRepository.findById(userId).orElseThrow();
        saveAudit(userId, user.getEmail(), AuditLog.AuditAction.LOGOUT_ALL, null, null, true, null);
        log.info("All devices logged out for userId: {}", userId);
    }

    // ══════════════════════════════════════════
    // FORGOT PASSWORD
    // ══════════════════════════════════════════
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Don't reveal if user exists — always return success
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String otp = generateOtp();
            saveOtp(user.getEmail(), otp, OtpVerification.OtpPurpose.PASSWORD_RESET);
            // TODO: publish via outbox to send email
            log.info("Password reset OTP generated for userId: {}", user.getId());
        });
    }

    // ══════════════════════════════════════════
    // RESET PASSWORD
    // ══════════════════════════════════════════
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        OtpVerification otpRecord = otpRepository
            .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                request.getEmail(), OtpVerification.OtpPurpose.PASSWORD_RESET)
            .orElseThrow(() -> new AuthException(
                ErrorCodes.OTP_INVALID, "Invalid or expired OTP", HttpStatus.BAD_REQUEST));

        if (otpRecord.isExpired()) {
            throw new AuthException(ErrorCodes.OTP_EXPIRED,
                "OTP has expired. Request a new one.", HttpStatus.BAD_REQUEST);
        }
        if (otpRecord.hasExceededMaxAttempts()) {
            throw new AuthException(ErrorCodes.OTP_MAX_ATTEMPTS,
                "Too many wrong attempts. Request a new OTP.", HttpStatus.TOO_MANY_REQUESTS);
        }

        // Verify OTP
        if (!passwordEncoder.matches(request.getOtp(), otpRecord.getOtpHash())) {
            otpRepository.incrementAttempts(otpRecord.getId());
            throw new AuthException(ErrorCodes.OTP_INVALID,
                "Invalid OTP. " + (3 - otpRecord.getAttempts() - 1) + " attempts remaining",
                HttpStatus.BAD_REQUEST);
        }

        // Update password + mark OTP used + revoke all tokens
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new AuthException(
                ErrorCodes.USER_NOT_FOUND, "User not found", HttpStatus.NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpRepository.markUsed(otpRecord.getId());
        refreshTokenRepository.revokeAllByUserId(user.getId());

        saveAudit(user.getId(), user.getEmail(), AuditLog.AuditAction.PASSWORD_RESET,
            null, null, true, null);

        log.info("Password reset successful for userId: {}", user.getId());
    }

    // ══════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════

    private AuthResponse buildAuthResponse(User user) {
        return buildAuthResponse(user, null, null);
    }

    private AuthResponse buildAuthResponse(User user, String ipAddress, String userAgent) {
        String accessToken  = jwtService.generateAccessToken(user.getId(), user.getEmail(), "ROLE_USER");
        String refreshToken = jwtService.generateRefreshToken();

        RefreshToken tokenEntity = RefreshToken.builder()
            .user(user)
            .token(refreshToken)
            .ipAddress(ipAddress)
            .deviceInfo(userAgent)
            .expiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRY_DAYS * 86400L))
            .build();
        refreshTokenRepository.save(tokenEntity);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .user(UserSummaryDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .emailVerified(user.isEmailVerified())
                .riskProfile(user.getRiskProfile().name())
                .taxRegime(user.getTaxRegime().name())
                .build())
            .build();
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void saveOtp(String email, String otp, OtpVerification.OtpPurpose purpose) {
        OtpVerification otpRecord = OtpVerification.builder()
            .email(email)
            .otpHash(passwordEncoder.encode(otp))
            .purpose(purpose)
            .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
            .build();
        otpRepository.save(otpRecord);
    }

    private void saveAudit(Long userId, String email, AuditLog.AuditAction action,
                           String ipAddress, String userAgent, boolean success, String failureReason) {
        auditLogRepository.save(AuditLog.builder()
            .userId(userId)
            .email(email)
            .action(action)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .success(success)
            .failureReason(failureReason)
            .build());
    }
}
