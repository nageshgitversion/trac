package com.investrac.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * INVESTRAC Service Registry
 * All microservices register here on startup.
 * API Gateway queries this to discover service locations.
 *
 * Dashboard: http://localhost:8761
 * Secured with Basic Auth (see application.yml)
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
