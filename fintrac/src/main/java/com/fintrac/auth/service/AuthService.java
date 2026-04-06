package com.fintrac.auth.service;

import com.fintrac.auth.dto.request.*;
import com.fintrac.auth.dto.response.AuthResponse;
import com.fintrac.auth.entity.User;
import com.fintrac.auth.repository.UserRepository;
import com.fintrac.common.exception.FinTracException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new FinTracException("FT-1001", "Email already registered", HttpStatus.CONFLICT);
        }
        User user = User.builder()
            .name(request.getName().trim())
            .email(request.getEmail().toLowerCase().trim())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .build();
        user = userRepository.save(user);
        log.info("Registered userId={}", user.getId());
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndActiveTrue(request.getEmail().toLowerCase())
            .orElseThrow(() -> new FinTracException("FT-1002", "Invalid email or password", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new FinTracException("FT-1002", "Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
        log.info("Login successful userId={}", user.getId());
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        return AuthResponse.builder()
            .accessToken(jwtService.generateToken(user.getId(), user.getEmail()))
            .userId(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .build();
    }
}
