package com.traveling.travel_backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetEmail(String to, String link) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(to);
            message.setSubject("Recuperación de contraseña");

            String body =
                    "Hola,\n\n" +
                    "Recibimos una solicitud para cambiar tu contraseña.\n\n" +
                    "Haz clic en el siguiente enlace:\n" +
                    link + "\n\n" +
                    "Este enlace expira en 15 minutos.\n\n" +
                    "Si no fuiste tú, ignora este mensaje.";

            message.setText(body);

            mailSender.send(message);

        } catch (Exception e) {
            System.out.println("Error al enviar correo: " + e.getMessage());
        }
    }
}