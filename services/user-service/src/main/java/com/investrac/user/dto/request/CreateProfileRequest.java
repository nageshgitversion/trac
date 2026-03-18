package com.investrac.user.dto.request;

import com.investrac.user.entity.UserProfile;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Called internally by the UserRegisteredEvent consumer
 * when a new user registers in auth-service.
 */
@Data
public class CreateProfileRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String phone;
}
