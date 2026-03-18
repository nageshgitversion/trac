package com.investrac.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    @Builder.Default
    private long expiresIn = 900;       // 15 minutes in seconds

    private UserSummaryDto user;
}
