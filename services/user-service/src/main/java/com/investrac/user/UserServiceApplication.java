package com.investrac.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * INVESTRAC User Service
 *
 * Manages user profiles, KYC, financial settings, and app preferences.
 * Consumes investrac.user.registered from Kafka to auto-create profiles.
 *
 * Key security features:
 *  - PAN encrypted with AES-256-GCM before storing
 *  - Only last 4 Aadhaar digits accepted
 *  - PAN/KYC fields never returned in API responses
 *  - PAN/KYC fields never logged
 *
 * Port: 8082
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
