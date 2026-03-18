package com.investrac.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private boolean emailVerified;
    private String riskProfile;
    private String taxRegime;
}
