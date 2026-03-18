package com.investrac;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Spring Boot Admin Server
 *
 * Centralized UI dashboard for monitoring all INVESTRAC microservices.
 * Integrates with Eureka for service discovery.
 * Port: 9090
 *
 * URL: http://localhost:9090
 */
@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
public class AdminServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminServerApplication.class, args);
    }
}
