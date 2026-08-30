package com.hemoconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point of the HemoConnect backend.
 *
 * Running this class starts an embedded Tomcat server (default port 8080),
 * connects to MySQL using the settings in application.yml, and scans every
 * class under the com.hemoconnect package for Spring components
 * (@Controller, @Service, @Repository, @Configuration, etc.).
 *
 * @EnableScheduling turns on Spring's @Scheduled annotation support, used
 * by ExpiredRequestScheduler (Module 4) to sweep for expired blood
 * requests in the background.
 */
@SpringBootApplication
@EnableScheduling
public class HemoConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HemoConnectApplication.class, args);
    }
}
