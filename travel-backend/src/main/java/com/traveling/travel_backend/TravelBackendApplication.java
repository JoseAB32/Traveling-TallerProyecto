package com.traveling.travel_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TravelBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelBackendApplication.class, args);
        System.out.println("=== BACKEND INICIADO CORRECTAMENTE ===");
        System.out.println("Base de datos: Clever Cloud");
        System.out.println("Puerto: 8080");
    }
}