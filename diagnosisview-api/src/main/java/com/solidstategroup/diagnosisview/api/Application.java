package com.solidstategroup.diagnosisview.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application.
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    /**
     * Spring Boot application entry point.
     * @param args String array application arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
