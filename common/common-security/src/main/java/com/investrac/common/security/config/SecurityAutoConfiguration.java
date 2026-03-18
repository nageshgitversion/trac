package com.investrac.common.security.config;

import com.investrac.common.security.jwt.JwtPublicKeyProvider;
import com.investrac.common.security.jwt.JwtTokenVerifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for common-security.
 *
 * Registered via META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 *
 * What gets auto-wired when any service adds common-security dependency:
 *   ✅ JwtPublicKeyProvider  — loads RSA public key once at startup
 *   ✅ JwtTokenVerifier      — verifies JWT tokens using public key
 *   ✅ RateLimitingService   — Redis-backed rate limiter (if Redis is on classpath)
 *
 * What each service still wires itself:
 *   ✅ SecurityFilterChain   — each service extends BaseSecurityConfig with its own rules
 *   ✅ GatewayHeaderAuthFilter — registered in each service's SecurityConfig
 *
 * Beans are conditional:
 *   @ConditionalOnMissingBean — services can override by defining their own bean
 *   @ConditionalOnProperty    — can disable via security.jwt.auto-configure=false
 */
@AutoConfiguration
@ConditionalOnProperty(name = "security.jwt.public-key")
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtPublicKeyProvider jwtPublicKeyProvider() {
        return new JwtPublicKeyProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenVerifier jwtTokenVerifier(JwtPublicKeyProvider provider) {
        return new JwtTokenVerifier(provider);
    }
}
