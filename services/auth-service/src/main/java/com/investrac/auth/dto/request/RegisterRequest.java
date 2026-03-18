package com.investrac.auth.dto.request;

import com.investrac.auth.config.validation.StrongPassword;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name can only contain letters, spaces, hyphens and apostrophes")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150)
    private String email;

    @Pattern(
        regexp = "^[6-9]\\d{9}$",
        message = "Invalid Indian mobile number. Must be 10 digits starting with 6-9"
    )
    private String phone;

    @NotBlank(message = "Password is required")
    @StrongPassword
    private String password;
}
