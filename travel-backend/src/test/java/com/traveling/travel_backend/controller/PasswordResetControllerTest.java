package com.traveling.travel_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.dto.NewPasswordRequest;
import com.traveling.travel_backend.dto.PasswordResetRequest;
import com.traveling.travel_backend.service.PasswordResetService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PasswordResetService passwordResetService;

    @Test
    @DisplayName("Debe retornar 200 cuando se solicita restablecimiento con email válido")
    void forgotPasswordWithValidEmail() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setCorreo("usuario@test.com");

        when(passwordResetService.sendResetLink("usuario@test.com"))
            .thenReturn("Si el correo existe, se enviará un enlace");

        mockMvc.perform(post("/api/password/forgot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Si el correo existe, se enviará un enlace"));
    }

    @Test
    @DisplayName("Debe retornar 200 cuando se valida token válido")
    void validateTokenShouldReturn200WhenTokenIsValid() throws Exception {
        String token = "token-valido-123";
        
        when(passwordResetService.isValidToken(token)).thenReturn(true);

        mockMvc.perform(get("/api/password/validate")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Token válido"));
    }

    @Test
    @DisplayName("Debe retornar 400 cuando se valida token expirado")
    void validateTokenShouldReturn400WhenTokenIsExpired() throws Exception {
        String token = "token-expirado-456";
        
        when(passwordResetService.isValidToken(token)).thenReturn(false);

        mockMvc.perform(get("/api/password/validate")
                .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El enlace ha expirado o es inválido"));
    }

    @Test
    @DisplayName("Debe retornar 200 cuando se restablece contraseña con token válido")
    void resetPasswordWithValidToken() throws Exception {
        NewPasswordRequest request = new NewPasswordRequest();
        request.setToken("token-valido-123");
        request.setPassword("nuevaPass123");

        when(passwordResetService.resetPassword("token-valido-123", "nuevaPass123"))
            .thenReturn("Contraseña actualizada correctamente");

        mockMvc.perform(post("/api/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Contraseña actualizada correctamente"));
    }

    @Test
    @DisplayName("Debe retornar mensaje de error cuando la contraseña tiene menos de 8 caracteres")
    void resetPasswordWithShortPassword() throws Exception {
        NewPasswordRequest request = new NewPasswordRequest();
        request.setToken("token-valido-123");
        request.setPassword("1234567");

        when(passwordResetService.resetPassword("token-valido-123", "1234567"))
            .thenReturn("La contraseña debe tener mínimo 8 caracteres");

        mockMvc.perform(post("/api/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("La contraseña debe tener mínimo 8 caracteres"));
    }

    @Test
    @DisplayName("Debe retornar mensaje de error cuando el token no existe")
    void resetPasswordWithNonExistentToken() throws Exception {
        NewPasswordRequest request = new NewPasswordRequest();
        request.setToken("token-inexistente");
        request.setPassword("nuevaPass123");

        when(passwordResetService.resetPassword("token-inexistente", "nuevaPass123"))
            .thenReturn("Token inválido o expirado");

        mockMvc.perform(post("/api/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Token inválido o expirado"));
    }
}