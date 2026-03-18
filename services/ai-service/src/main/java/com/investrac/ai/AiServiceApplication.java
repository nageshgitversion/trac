package com.investrac.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * INVESTRAC AI Service
 *
 * Claude-powered personal financial advisor.
 * Features:
 *  - Real-time chat with full financial context (wallet + portfolio + transactions)
 *  - Nightly insight generation at 11:30 PM for all active users
 *  - Graceful fallback when Claude API unavailable
 *  - Privacy: message content never logged
 *  - Exponential backoff retry for rate limiting (429)
 *
 * Port: 8087
 * External dependency: Anthropic Claude API (claude-sonnet-4-20250514)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class AiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
