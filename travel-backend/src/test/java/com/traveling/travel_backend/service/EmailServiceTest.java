package com.traveling.travel_backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("Debe llamar al JavaMailSender para enviar el correo correctamente")
    void shouldSendResetEmailSuccessfully() {
        String correoDestino = "pablo@test.com";
        String enlaceRestablecimiento = "http://localhost:4200/reset-password?token=uuid";

        emailService.sendResetEmail(correoDestino, enlaceRestablecimiento);

        // Verificamos que el componente de Spring Boot encargado de enviar emails fue ejecutado
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}