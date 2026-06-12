package com.traveling.travel_backend.service;

import org.springframework.mail.MailException;
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

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("No se pudo enviar el correo de recuperación", e);
        }
    }

    public void sendAdminWelcomeEmail(String to, String userName, String temporaryPassword) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Cuenta de administrador creada - Traveling");

        String body =
                "Hola,\n\n" +
                "Se creó una cuenta de administrador para ti en Traveling.\n\n" +
                "Tus credenciales temporales son:\n\n" +
                "Usuario: " + userName + "\n" +
                "Contraseña temporal: " + temporaryPassword + "\n\n" +
                "Por seguridad, cambia tu contraseña después de iniciar sesión.\n\n" +
                "Si no esperabas este correo, comunícate con el administrador del sistema.";

        message.setText(body);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("No se pudo enviar el correo de bienvenida al administrador", e);
        }
    }
}