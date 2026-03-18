package com.investrac.portfolio.config;

import com.investrac.common.security.config.BaseSecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends BaseSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return applyCommonConfig(http)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .build();
    }
}
