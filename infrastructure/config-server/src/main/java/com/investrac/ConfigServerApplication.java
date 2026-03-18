package com.investrac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Spring Cloud Config Server
 *
 * Centralized configuration management for all INVESTRAC services.
 * Port: 8888
 *
 * Serves configuration from:
 *   - Git repository (production)
 *   - Classpath resources (development)
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
