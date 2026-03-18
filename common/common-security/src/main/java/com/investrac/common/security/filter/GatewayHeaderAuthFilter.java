package com.investrac.common.security.filter;

import com.investrac.common.security.model.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security filter for services running BEHIND the API Gateway.
 *
 * Flow:
 *   Angular → API Gateway (validates JWT, injects headers) → This Filter
 *
 * The API Gateway validates the JWT and injects:
 *   X-User-Id     → authenticated user's ID
 *   X-User-Email  → authenticated user's email
 *   X-User-Roles  → user's roles (comma-separated)
 *
 * This filter reads those headers and sets the Spring Security context
 * so controllers can use @AuthenticationPrincipal AuthenticatedUser.
 *
 * SECURITY:
 *   - Services must ONLY be accessible through the gateway (not directly)
 *   - In Kubernetes: network policies block direct service access
 *   - In Docker: only the gateway container is exposed externally
 *   - If X-User-Id header is missing, request is unauthenticated (401)
 *
 * Services that DON'T use this filter:
 *   - eureka-server (basic auth)
 *   - config-server (basic auth)
 *   - admin-server (basic auth)
 *   - api-gateway (verifies JWT directly, doesn't trust these headers)
 */
@Slf4j
public class GatewayHeaderAuthFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ID    = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLES = "X-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userIdHeader = request.getHeader(HEADER_USER_ID);

        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                Long   userId = Long.parseLong(userIdHeader);
                String email  = request.getHeader(HEADER_USER_EMAIL);
                String roles  = request.getHeader(HEADER_USER_ROLES);

                AuthenticatedUser principal = AuthenticatedUser.fromHeaders(userId, email, roles);

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                    );

                SecurityContextHolder.getContext().setAuthentication(auth);
                log.trace("Security context set for userId={}", userId);

            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header value: {}", userIdHeader);
                // Let the request proceed — Spring Security will block it if auth required
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Skip this filter for health/info endpoints — they don't need authentication.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health")
            || path.startsWith("/actuator/info")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui");
    }
}
