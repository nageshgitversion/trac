package com.investrac.common.security.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security principal representing an authenticated INVESTRAC user.
 *
 * Populated from the headers injected by API Gateway after JWT validation:
 *   X-User-Id     → userId
 *   X-User-Email  → email
 *   X-User-Roles  → roles (comma-separated)
 *
 * Services BEHIND the gateway trust these headers completely.
 * The gateway already verified the JWT — services do not re-verify.
 *
 * Usage in controllers:
 *   @GetMapping("/me")
 *   public ResponseEntity<?> getProfile(
 *       @AuthenticationPrincipal AuthenticatedUser user) {
 *       return service.getProfile(user.getUserId());
 *   }
 */
@Getter
@Builder
public class AuthenticatedUser implements UserDetails {

    private final Long   userId;
    private final String email;
    private final String roles;  // "ROLE_USER,ROLE_ADMIN"

    public static AuthenticatedUser fromHeaders(Long userId, String email, String roles) {
        return AuthenticatedUser.builder()
            .userId(userId)
            .email(email)
            .roles(roles != null ? roles : "ROLE_USER")
            .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isBlank()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return Arrays.stream(roles.split(","))
            .map(String::trim)
            .filter(r -> !r.isBlank())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    @Override
    public String getPassword()  { return null; }  // Not used — JWT-based auth

    @Override
    public String getUsername()  { return email; }

    @Override
    public boolean isAccountNonExpired()   { return true; }

    @Override
    public boolean isAccountNonLocked()    { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled()             { return true; }

    /**
     * Check if user has a specific role.
     * @param role Role to check (e.g., "ROLE_ADMIN", "ROLE_USER")
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        if (role == null || role.isBlank() || roles == null || roles.isBlank()) {
            return false;
        }
        return Arrays.stream(roles.split(","))
            .map(String::trim)
            .anyMatch(r -> r.equalsIgnoreCase(role));
    }
}
