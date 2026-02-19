package com.restaurant.reportservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Report Service.
 * 
 * This microservice consumes order events from RabbitMQ and maintains
 * reporting projections in an independent database.
 */
@SpringBootApplication
public class ReportServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
    }
}
