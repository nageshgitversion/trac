package com.investrac.common.security.config;

import com.investrac.common.security.filter.GatewayHeaderAuthFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Base security configuration for all INVESTRAC microservices behind the gateway.
 *
 * Usage — extend this in each service's SecurityConfig:
 *
 *   @Configuration
 *   @EnableWebSecurity
 *   public class SecurityConfig extends BaseSecurityConfig {
 *
 *       @Bean
 *       public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
 *           return applyCommonConfig(http)
 *               .authorizeHttpRequests(auth -> auth
 *                   .anyRequest().authenticated()
 *               )
 *               .build();
 *       }
 *   }
 *
 * This approach:
 *   1. Eliminates copy-paste of the same security boilerplate across 8 services
 *   2. Centralises the GatewayHeaderAuthFilter registration
 *   3. Makes it easy to add company-wide security rules in one place
 */
public abstract class BaseSecurityConfig {

    /**
     * Apply common security settings shared across all services:
     *  - Disable CSRF (stateless REST API)
     *  - Stateless session (no HttpSession)
     *  - Permit actuator health/info and Swagger
     *  - Register GatewayHeaderAuthFilter
     *
     * @param http HttpSecurity to configure
     * @return Configured HttpSecurity (chain further in the subclass)
     */
    protected HttpSecurity applyCommonConfig(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(
                new GatewayHeaderAuthFilter(),
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/actuator/prometheus",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
            );
        return http;
    }
}
