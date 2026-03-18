package com.investrac.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * INVESTRAC API Gateway
 * Single entry point for all client requests.
 *
 * Responsibilities:
 *  - JWT validation (before request reaches any service)
 *  - Rate limiting (Redis-backed, per user)
 *  - Load balancing (via Eureka + Spring Cloud LB)
 *  - Circuit breaking (Resilience4j per route)
 *  - Request/response logging with traceId
 *  - CORS for Angular frontend
 *
 * Port: 8080
 */

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
