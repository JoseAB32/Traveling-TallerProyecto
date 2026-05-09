package com.traveling.travel_backend.service;

import com.traveling.travel_backend.dto.CityResponseDTO;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.repository.CityRepository;
import com.traveling.travel_backend.repository.LogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private CityService cityService;

    private City sampleCity;

    @BeforeEach
    void setUp() {
        sampleCity = new City();
        sampleCity.setId(1L);
        sampleCity.setName("Cochabamba");
    }

    @Nested
    @DisplayName("Tests para getAllCities")
    class GetAllCitiesTests {

        @Test
        @DisplayName("Debe retornar una lista de ciudades y guardar los logs de auditoría")
        void shouldReturnListOfCitiesAndSaveLogs() {
            when(cityRepository.findAll()).thenReturn(List.of(sampleCity));

            List<CityResponseDTO> result = cityService.getAllCities();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Cochabamba");

            verify(logRepository, times(2)).save(any(LogEntity.class));
            verify(cityRepository).findAll();
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay ciudades")
        void shouldReturnEmptyListWhenNoCitiesExist() {

            when(cityRepository.findAll()).thenReturn(Collections.emptyList());

            List<CityResponseDTO> result = cityService.getAllCities();

            assertThat(result).isEmpty();
            verify(logRepository, times(2)).save(any(LogEntity.class));
        }
    }

    @Nested
    @DisplayName("Tests para getCityById")
    class GetCityByIdTests {

        @Test
        @DisplayName("Debe retornar la ciudad cuando el ID existe")
        void shouldReturnCityWhenIdExists() {

            when(cityRepository.findById(1L)).thenReturn(Optional.of(sampleCity));

            CityResponseDTO result = cityService.getCityById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Cochabamba");
            
            verify(logRepository, times(1)).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException y guardar log de advertencia cuando el ID no existe")
        void shouldThrowExceptionAndLogWarnWhenIdDoesNotExist() {

            Long nonExistentId = 99L;
            when(cityRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cityService.getCityById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Ciudad no encontrada con ID: " + nonExistentId);

            verify(logRepository, times(2)).save(any(LogEntity.class));
        }
    }
}