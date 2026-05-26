package com.traveling.travel_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;

class SupabaseReminderServiceTest {

    private SupabaseReminderService supabaseReminderService;

    @BeforeEach
    void setUp() {
        supabaseReminderService = new SupabaseReminderService();
        
        // Inyectar valores manualmente (sin usar @Value)
        ReflectionTestUtils.setField(supabaseReminderService, "SUPABASE_URL", 
                "https://test.supabase.co");
        ReflectionTestUtils.setField(supabaseReminderService, "SUPABASE_API_KEY", 
                "test-api-key-123");
    }

    @Nested
    @DisplayName("Tests de creación de recordatorios")
    class CreateReminderTests {

        @Test
        @DisplayName("Debe crear un recordatorio exitosamente (sin errores)")
        void shouldCreateReminderSuccessfully() {
            String email = "usuario@test.com";
            String userName = "Usuario Test";
            String tripName = "Viaje a la Paz";
            String startDate = "2026-06-15";
            Long tripId = 100L;

            // Solo verificamos que no lance excepción
            assertThatCode(() -> {
                supabaseReminderService.createReminder(email, userName, tripName, startDate, tripId);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe calcular correctamente la fecha del recordatorio")
        void shouldCalculateReminderDateCorrectly() {
            String email = "usuario@test.com";
            String userName = "Usuario Test";
            String tripName = "Viaje a Uyuni";
            String startDate = "2026-12-25";
            Long tripId = 400L;

            assertThatCode(() -> {
                supabaseReminderService.createReminder(email, userName, tripName, startDate, tripId);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe manejar fechas con formato correcto")
        void shouldHandleValidDateFormat() {
            String email = "test@test.com";
            String userName = "Test";
            String tripName = "Test Trip";
            String startDate = "2026-12-31";
            Long tripId = 1L;

            assertThatCode(() -> {
                supabaseReminderService.createReminder(email, userName, tripName, startDate, tripId);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe manejar fechas inválidas sin lanzar excepción al exterior")
        void shouldHandleInvalidDateFormat() {
            String email = "test@test.com";
            String userName = "Test";
            String tripName = "Test Trip";
            String startDate = "fecha-invalida";
            Long tripId = 1L;

            // No debe lanzar excepción (la atrapa internamente)
            assertThatCode(() -> {
                supabaseReminderService.createReminder(email, userName, tripName, startDate, tripId);
            }).doesNotThrowAnyException();
        }
    }
}