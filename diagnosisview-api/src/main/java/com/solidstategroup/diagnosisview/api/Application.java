package com.solidstategroup.diagnosisview.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application.
 */
@SpringBootApplication
public class Application {

    /**
     * Spring Boot application entry point.
     *
     * @param args String array application arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
