package com.traveling.travel_backend.service;

import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.repository.LogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private LogService logService;

    private LogEntity sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = new LogEntity("AUTH", "INFO", "Login exitoso", 1L);
        sampleLog.setTimestamp(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Caso de Uso: Obtener todos los Logs")
    class GetAllLogsTests {

        @Test
        @DisplayName("Debe retornar lista de logs si el acceso admin está habilitado")
        void shouldReturnLogsWhenAdminEnabled() {

            when(logRepository.findAll(any(Sort.class))).thenReturn(List.of(sampleLog));

            List<LogEntity> result = logService.getAllLogs(true);

            assertThat(result).isNotEmpty().hasSize(1);
            verify(logRepository).findAll(any(Sort.class));
        }

        @Test
        @DisplayName("Debe retornar lista vacía y no llamar al repo si el acceso admin está deshabilitado")
        void shouldReturnEmptyListWhenAdminDisabled() {
            // Act
            List<LogEntity> result = logService.getAllLogs(false);

            assertThat(result).isEmpty();
            verifyNoInteractions(logRepository);
        }
    }

    @Nested
    @DisplayName("Caso de Uso: Filtrado de Logs")
    class FilterLogsTests {

        private final String startDate = "2026-05-01T00:00:00";
        private final String endDate = "2026-05-31T23:59:59";

        @Test
        @DisplayName("Debe filtrar logs correctamente con parámetros válidos")
        void shouldFilterLogsWithValidParams() {

            when(logRepository.findByModuleAndLevelAndTimestampBetweenOrderByTimestampDesc(
                    eq("AUTH"), eq("INFO"), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(sampleLog));

            List<LogEntity> result = logService.filterLogs(true, "AUTH", "INFO", startDate, endDate);

            assertThat(result).hasSize(1);
            verify(logRepository).findByModuleAndLevelAndTimestampBetweenOrderByTimestampDesc(
                    eq("AUTH"), eq("INFO"), any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Debe lanzar DateTimeParseException si el formato de fecha es inválido")
        void shouldThrowExceptionWhenDateFormatIsInvalid() {

            assertThatThrownBy(() -> 
                logService.filterLogs(true, "AUTH", "INFO", "invalid-date", endDate)
            ).isInstanceOf(java.time.format.DateTimeParseException.class);
            
            verifyNoInteractions(logRepository);
        }

        @Test
        @DisplayName("Debe retornar lista vacía sin filtrar si admin está deshabilitado")
        void shouldReturnEmptyWhenAdminDisabledForFilter() {

            List<LogEntity> result = logService.filterLogs(false, "AUTH", "INFO", startDate, endDate);

            assertThat(result).isEmpty();
            verifyNoInteractions(logRepository);
        }
    }
}