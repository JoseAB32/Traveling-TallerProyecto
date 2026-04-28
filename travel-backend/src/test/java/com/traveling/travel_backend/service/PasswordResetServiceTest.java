package com.traveling.travel_backend.service;

import com.traveling.travel_backend.model.PasswordResetToken;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.PasswordResetTokenRepository;
import com.traveling.travel_backend.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    @DisplayName("Debe generar token y enviar correo cuando el usuario existe y está activo")
    void sendResetLinkSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setUserName("Pablo");
        user.setCorreo("pablo@test.com");
        user.setState(true);

        when(userRepository.findByCorreo("pablo@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArguments()[0]);
        doNothing().when(emailService).sendResetEmail(anyString(), anyString());

        String result = passwordResetService.sendResetLink("pablo@test.com");

        assertEquals("Si el correo existe, se enviará un enlace", result);
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendResetEmail(eq("pablo@test.com"), anyString());
    }

    @Test
    @DisplayName("Debe retornar mensaje genérico y no hacer nada si el usuario no existe")
    void sendResetLinkWhenUserDoesNotExist() {
        when(userRepository.findByCorreo("fantasma@test.com")).thenReturn(Optional.empty());

        String result = passwordResetService.sendResetLink("fantasma@test.com");

        assertEquals("Si el correo existe, se enviará un enlace", result);
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailService, never()).sendResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe retornar mensaje genérico y no hacer nada si el usuario está inactivo")
    void sendResetLinkWhenUserIsInactive() {
        User user = new User();
        user.setId(1L);
        user.setUserName("Pablo");
        user.setCorreo("pablo@test.com");
        user.setState(false); // Usuario inactivo

        when(userRepository.findByCorreo("pablo@test.com")).thenReturn(Optional.of(user));

        String result = passwordResetService.sendResetLink("pablo@test.com");

        assertEquals("Si el correo existe, se enviará un enlace", result);
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailService, never()).sendResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe retornar true para token válido (no expirado)")
    void isValidTokenShouldReturnTrueForValidToken() {
        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiration(LocalDateTime.now().plusMinutes(10)); // Válido por 10 minutos más

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        boolean result = passwordResetService.isValidToken(token);

        assertTrue(result);
        verify(tokenRepository, times(1)).findByToken(token);
    }

    @Test
    @DisplayName("Debe retornar false para token expirado")
    void isValidTokenShouldReturnFalseForExpiredToken() {
        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiration(LocalDateTime.now().minusMinutes(5)); // Expirado hace 5 minutos

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        boolean result = passwordResetService.isValidToken(token);

        assertFalse(result);
        verify(tokenRepository, times(1)).findByToken(token);
    }

    @Test
    @DisplayName("Debe retornar false para token inexistente")
    void isValidTokenShouldReturnFalseForNonExistentToken() {
        String token = "token-inexistente";
        
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        boolean result = passwordResetService.isValidToken(token);

        assertFalse(result);
        verify(tokenRepository, times(1)).findByToken(token);
    }

    @Test
    @DisplayName("Debe actualizar la contraseña y borrar el token cuando los datos son correctos")
    void resetPasswordSuccessfully() {
        String token = "token-1234";
        String newPassword = "nuevaClave123";
        String encodedPassword = "claveHasheada";
        
        User user = new User();
        user.setId(1L);
        user.setPass("claveVieja");

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiration(LocalDateTime.now().plusMinutes(10)); // Token válido

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(user)).thenReturn(user);

        String result = passwordResetService.resetPassword(token, newPassword);

        assertEquals("Contraseña actualizada correctamente", result);
        assertEquals(encodedPassword, user.getPass());
        verify(userRepository, times(1)).save(user);
        verify(tokenRepository, times(1)).delete(resetToken);
    }

    @Test
    @DisplayName("Debe fallar al resetear si la contraseña tiene menos de 8 caracteres")
    void resetPasswordFailsWhenPasswordIsTooShort() {
        String token = "token-1234";
        String shortPassword = "1234567"; // 7 caracteres

        String result = passwordResetService.resetPassword(token, shortPassword);
        
        assertEquals("La contraseña debe tener mínimo 8 caracteres", result);
        verify(tokenRepository, never()).findByToken(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debe fallar al resetear si el token expiró")
    void resetPasswordFailsWhenTokenIsExpired() {
        String token = "token-vencido";
        String newPassword = "nuevaClave123";
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiration(LocalDateTime.now().minusMinutes(5)); // Token expirado

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        String result = passwordResetService.resetPassword(token, newPassword);

        assertEquals("Token inválido o expirado", result);
        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository, never()).delete(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("Debe fallar al resetear si el token no existe")
    void resetPasswordFailsWhenTokenDoesNotExist() {
        String token = "token-inexistente";
        String newPassword = "nuevaClave123";

        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        String result = passwordResetService.resetPassword(token, newPassword);

        assertEquals("Token inválido o expirado", result);
        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository, never()).delete(any(PasswordResetToken.class));
    }
}