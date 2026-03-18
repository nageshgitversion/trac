package com.investrac.common.security.model;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Static utilities for accessing the current security context.
 *
 * Use sparingly — prefer @CurrentUser in controllers.
 * Useful in service-layer code where request scope is not directly available.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Get the currently authenticated user, if any.
     */
    public static Optional<AuthenticatedUser> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * Get userId from security context — throws if not authenticated.
     */
    public static Long requireUserId() {
        return getCurrentUser()
            .map(AuthenticatedUser::getUserId)
            .orElseThrow(() -> new IllegalStateException(
                "No authenticated user in security context — is the request going through the gateway?"));
    }

    /**
     * Quick check: is the current request authenticated?
     */
    public static boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }

    /**
     * Check if current user has a specific role.
     */
    public static boolean hasRole(String role) {
        return getCurrentUser()
            .map(u -> u.hasRole(role))
            .orElse(false);
    }
}
