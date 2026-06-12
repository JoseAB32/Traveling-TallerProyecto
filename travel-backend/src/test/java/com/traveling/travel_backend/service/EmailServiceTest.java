package com.traveling.travel_backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("Debe llamar al JavaMailSender para enviar el correo de recuperación correctamente")
    void shouldSendResetEmailSuccessfully() {
        String correoDestino = "pablo@test.com";
        String enlaceRestablecimiento = "http://localhost:4200/reset-password?token=uuid";

        emailService.sendResetEmail(correoDestino, enlaceRestablecimiento);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertThat(message.getTo()).containsExactly(correoDestino);
        assertThat(message.getSubject()).isEqualTo("Recuperación de contraseña");
        assertThat(message.getText()).contains(enlaceRestablecimiento);
        assertThat(message.getText()).contains("Este enlace expira en 15 minutos.");
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException si falla el envío del correo de recuperación")
    void shouldThrowWhenResetEmailFails() {
        doThrow(new MailException("SMTP error") {})
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.sendResetEmail(
                "pablo@test.com",
                "http://localhost:4200/reset-password?token=uuid"
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo enviar el correo de recuperación");
    }

    @Test
    @DisplayName("Debe enviar correo de bienvenida al administrador")
    void shouldSendAdminWelcomeEmailSuccessfully() {
        String correoDestino = "admin@test.com";
        String userName = "admin_user";
        String temporaryPassword = "Adm-X7kP9qL2";

        emailService.sendAdminWelcomeEmail(
                correoDestino,
                userName,
                temporaryPassword
        );

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertThat(message.getTo()).containsExactly(correoDestino);
        assertThat(message.getSubject()).isEqualTo("Cuenta de administrador creada - Traveling");
        assertThat(message.getText()).contains("Se creó una cuenta de administrador");
        assertThat(message.getText()).contains("Usuario: " + userName);
        assertThat(message.getText()).contains("Contraseña temporal: " + temporaryPassword);
        assertThat(message.getText()).contains("Por seguridad, cambia tu contraseña");
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException si falla el correo de bienvenida al administrador")
    void shouldThrowWhenAdminWelcomeEmailFails() {
        doThrow(new MailException("SMTP error") {})
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.sendAdminWelcomeEmail(
                "admin@test.com",
                "admin_user",
                "Adm-X7kP9qL2"
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo enviar el correo de bienvenida al administrador");
    }
}