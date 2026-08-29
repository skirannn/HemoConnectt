package com.hemoconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the HemoConnect backend.
 *
 * Running this class starts an embedded Tomcat server (default port 8080),
 * connects to MySQL using the settings in application.yml, and scans every
 * class under the com.hemoconnect package for Spring components
 * (@Controller, @Service, @Repository, @Configuration, etc.).
 */
@SpringBootApplication
public class HemoConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HemoConnectApplication.class, args);
    }
}
