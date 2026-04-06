package com.fintrac.auth.dto.response;

import lombok.*;

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private Long userId;
    private String name;
    private String email;
}
