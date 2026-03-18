package com.investrac.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * INVESTRAC Wallet Service
 * Handles: Monthly wallet, envelopes, SAGA debit/credit
 * Port: 8083
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class WalletServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
    }
}
