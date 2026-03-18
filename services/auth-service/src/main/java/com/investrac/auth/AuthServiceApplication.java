package com.investrac.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * INVESTRAC Authentication Service
 * Handles: Registration, Login, JWT tokens, OTP, Password reset
 * Port: 8081
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
