
package com.example.contentgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the AI Content Generator.
 *
 * @SpringBootApplication enables auto-configuration, component scanning, and application setup.
 * @EnableScheduling enables Spring's scheduled task execution capabilities.
 */
@SpringBootApplication
@EnableScheduling
public class ContentGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentGeneratorApplication.class, args);
    }
}
