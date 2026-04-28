package com.traveling.travel_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.dto.NewPasswordRequest;
import com.traveling.travel_backend.dto.PasswordResetRequest;
import com.traveling.travel_backend.service.PasswordResetService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        PasswordResetController controller = new PasswordResetController(passwordResetService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void forgotPasswordWithValidEmailShouldReturnGenericMessage() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setCorreo("usuario@gmail.com");

        when(passwordResetService.sendResetLink("usuario@gmail.com"))
                .thenReturn("Si el correo existe, se enviará un enlace");

        mockMvc.perform(post("/api/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Si el correo existe, se enviará un enlace"));

        verify(passwordResetService, times(1)).sendResetLink("usuario@gmail.com");
    }

    @Test
    void resetPasswordWithValidTokenShouldReturnSuccessMessage() throws Exception {
        NewPasswordRequest request = new NewPasswordRequest();
        request.setToken("valid-token");
        request.setPassword("Password123");

        when(passwordResetService.resetPassword("valid-token", "Password123"))
                .thenReturn("Contraseña actualizada correctamente");

        mockMvc.perform(post("/api/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Contraseña actualizada correctamente"));

        verify(passwordResetService, times(1))
                .resetPassword("valid-token", "Password123");
    }

    @Test
    void resetPasswordWithShortPasswordShouldReturnValidationMessage() throws Exception {
        NewPasswordRequest request = new NewPasswordRequest();
        request.setToken("valid-token");
        request.setPassword("123");

        when(passwordResetService.resetPassword("valid-token", "123"))
                .thenReturn("La contraseña debe tener mínimo 8 caracteres");

        mockMvc.perform(post("/api/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("La contraseña debe tener mínimo 8 caracteres"));

        verify(passwordResetService, times(1))
                .resetPassword("valid-token", "123");
    }

    @Test
    void resetPasswordWithInvalidTokenShouldReturnInvalidTokenMessage() throws Exception {
        NewPasswordRequest request = new NewPasswordRequest();
        request.setToken("invalid-token");
        request.setPassword("Password123");

        when(passwordResetService.resetPassword("invalid-token", "Password123"))
                .thenReturn("Token inválido o expirado");

        mockMvc.perform(post("/api/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Token inválido o expirado"));

        verify(passwordResetService, times(1))
                .resetPassword("invalid-token", "Password123");
    }

    @Test
    void validateTokenShouldReturn200WhenTokenIsValid() throws Exception {
        when(passwordResetService.isValidToken("valid-token"))
                .thenReturn(true);

        mockMvc.perform(get("/api/password/validate")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Token válido"));

        verify(passwordResetService, times(1)).isValidToken("valid-token");
    }

    @Test
    void validateTokenShouldReturn400WhenTokenIsInvalidOrExpired() throws Exception {
        when(passwordResetService.isValidToken("expired-token"))
                .thenReturn(false);

        mockMvc.perform(get("/api/password/validate")
                        .param("token", "expired-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El enlace ha expirado o es inválido"));

        verify(passwordResetService, times(1)).isValidToken("expired-token");
    }
}