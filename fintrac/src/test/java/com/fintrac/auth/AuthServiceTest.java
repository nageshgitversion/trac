package com.fintrac.auth;

import com.fintrac.auth.dto.request.LoginRequest;
import com.fintrac.auth.dto.request.RegisterRequest;
import com.fintrac.auth.dto.response.AuthResponse;
import com.fintrac.auth.entity.User;
import com.fintrac.auth.repository.UserRepository;
import com.fintrac.auth.service.AuthService;
import com.fintrac.auth.service.JwtService;
import com.fintrac.common.exception.FinTracException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .name("Test User")
            .email("test@example.com")
            .passwordHash("$2a$12$hashedpassword")
            .active(true)
            .build();
    }

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("test@example.com");
        req.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn("token");

        AuthResponse response = authService.register(req);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getAccessToken()).isEqualTo("token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("test@example.com");
        req.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
            .isInstanceOf(FinTracException.class)
            .satisfies(ex -> {
                FinTracException fte = (FinTracException) ex;
                assertThat(fte.getErrorCode()).isEqualTo("FT-1001");
                assertThat(fte.getStatus()).isEqualTo(HttpStatus.CONFLICT);
            });
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        when(userRepository.findByEmailAndActiveTrue("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(1L, "test@example.com")).thenReturn("token");

        AuthResponse response = authService.login(req);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getAccessToken()).isEqualTo("token");
    }

    @Test
    void login_wrongPassword_throwsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrongpassword");

        when(userRepository.findByEmailAndActiveTrue("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(FinTracException.class)
            .satisfies(ex -> {
                FinTracException fte = (FinTracException) ex;
                assertThat(fte.getErrorCode()).isEqualTo("FT-1002");
                assertThat(fte.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            });
    }
}
