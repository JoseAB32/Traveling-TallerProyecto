package com.traveling.travel_backend.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendEmail(String to, String subject, String content) {

        // Simulación por consola (para pruebas)
        System.out.println("------------ EMAIL ------------");
        System.out.println("TO: " + to);
        System.out.println("SUBJECT: " + subject);
        System.out.println("CONTENT: " + content);
        System.out.println("--------------------------------");
    }
}