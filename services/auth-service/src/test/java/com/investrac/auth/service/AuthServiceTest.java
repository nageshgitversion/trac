package com.investrac.auth.service;

import com.investrac.auth.dto.request.LoginRequest;
import com.investrac.auth.dto.request.RegisterRequest;
import com.investrac.auth.dto.response.AuthResponse;
import com.investrac.auth.entity.User;
import com.investrac.auth.exception.AccountLockedException;
import com.investrac.auth.exception.AuthException;
import com.investrac.auth.outbox.OutboxService;
import com.investrac.auth.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock UserRepository            userRepository;
    @Mock RefreshTokenRepository    refreshTokenRepository;
    @Mock OtpVerificationRepository otpRepository;
    @Mock AuditLogRepository        auditLogRepository;
    @Mock JwtService                jwtService;
    @Mock PasswordEncoder           passwordEncoder;
    @Mock OutboxService             outboxService;

    @InjectMocks
    AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
            .id(1L)
            .name("Arjun Kumar")
            .email("arjun@investrac.in")
            .phone("9876543210")
            .passwordHash("$2a$12$hashedPassword")
            .active(true)
            .emailVerified(false)
            .loginAttempts(0)
            .build();
    }

    // ── REGISTER ─────────────────────────────────────────────

    @Test
    @DisplayName("register: success — new unique email + phone")
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Arjun Kumar");
        request.setEmail("arjun@investrac.in");
        request.setPhone("9876543210");
        request.setPassword("Secure@123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashed");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("mock.access.token");
        when(jwtService.generateRefreshToken()).thenReturn("mock-refresh-token");
        when(refreshTokenRepository.save(any())).thenReturn(null);
        when(otpRepository.save(any())).thenReturn(null);
        when(auditLogRepository.save(any())).thenReturn(null);
        doNothing().when(outboxService).publish(any(), any());

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock.access.token");
        assertThat(response.getUser().getEmail()).isEqualTo("arjun@investrac.in");
        verify(userRepository).save(any(User.class));
        verify(outboxService).publish(any(), any());
    }

    @Test
    @DisplayName("register: fail — email already exists")
    void register_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@investrac.in");
        request.setPassword("Secure@123");

        when(userRepository.existsByEmail("existing@investrac.in")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(AuthException.class)
            .hasMessageContaining("email already exists");
    }

    // ── LOGIN ─────────────────────────────────────────────────

    @Test
    @DisplayName("login: success — correct credentials")
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("arjun@investrac.in");
        request.setPassword("Secure@123");

        when(userRepository.findByEmailAndActiveTrue("arjun@investrac.in"))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("Secure@123", mockUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access.token");
        when(jwtService.generateRefreshToken()).thenReturn("refresh-token");
        when(refreshTokenRepository.save(any())).thenReturn(null);
        when(auditLogRepository.save(any())).thenReturn(null);
        doNothing().when(userRepository).resetLoginAttempts(any());

        AuthResponse response = authService.login(request, "192.168.1.1", "Mozilla/5.0");

        assertThat(response.getAccessToken()).isEqualTo("access.token");
        verify(userRepository).resetLoginAttempts(1L);
    }

    @Test
    @DisplayName("login: fail — wrong password increments attempts")
    void login_WrongPassword_IncrementsAttempts() {
        LoginRequest request = new LoginRequest();
        request.setEmail("arjun@investrac.in");
        request.setPassword("WrongPassword");

        mockUser.setLoginAttempts(2);
        when(userRepository.findByEmailAndActiveTrue(any())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        doNothing().when(userRepository).incrementLoginAttempts(any());
        when(auditLogRepository.save(any())).thenReturn(null);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1", "agent"))
            .isInstanceOf(AuthException.class)
            .hasMessageContaining("Invalid email or password");

        verify(userRepository).incrementLoginAttempts(1L);
    }

    @Test
    @DisplayName("login: fail — 5th wrong attempt locks account")
    void login_FifthFailedAttempt_LocksAccount() {
        LoginRequest request = new LoginRequest();
        request.setEmail("arjun@investrac.in");
        request.setPassword("WrongPassword");

        mockUser.setLoginAttempts(4);  // 5th attempt will lock
        when(userRepository.findByEmailAndActiveTrue(any())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        doNothing().when(userRepository).incrementLoginAttempts(any());
        doNothing().when(userRepository).lockUserUntil(any(), any());
        when(auditLogRepository.save(any())).thenReturn(null);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1", "agent"))
            .isInstanceOf(AccountLockedException.class);

        verify(userRepository).lockUserUntil(eq(1L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("login: fail — account is locked")
    void login_AccountLocked() {
        LoginRequest request = new LoginRequest();
        request.setEmail("arjun@investrac.in");
        request.setPassword("any");

        mockUser.setLockedUntil(LocalDateTime.now().plusMinutes(25));
        when(userRepository.findByEmailAndActiveTrue(any())).thenReturn(Optional.of(mockUser));
        when(auditLogRepository.save(any())).thenReturn(null);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1", "agent"))
            .isInstanceOf(AccountLockedException.class);
    }

    @Test
    @DisplayName("login: fail — user not found")
    void login_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notexist@investrac.in");
        request.setPassword("any");

        when(userRepository.findByEmailAndActiveTrue(any())).thenReturn(Optional.empty());
        when(auditLogRepository.save(any())).thenReturn(null);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1", "agent"))
            .isInstanceOf(AuthException.class);
    }
}
