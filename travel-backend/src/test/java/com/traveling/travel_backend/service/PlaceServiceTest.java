package com.traveling.travel_backend.service;

import com.traveling.travel_backend.dto.PlaceResponseDTO;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.PlaceRepository;
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
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private PlaceService placeService;

    private Place samplePlace;
    private City sampleCity;

    @BeforeEach
    void setUp() {
        sampleCity = new City();
        sampleCity.setId(1L);
        sampleCity.setName("Cochabamba");

        samplePlace = new Place();
        samplePlace.setId(10L);
        samplePlace.setName("Cristo de la Concordia");
        samplePlace.setAddress("Cerro San Pedro");
        samplePlace.setRating(5.0);
        samplePlace.setCity(sampleCity);
        samplePlace.setState(true);
    }

    @Nested
    @DisplayName("Pruebas de Consultas Generales y Top Rated")
    class GeneralQueriesTests {

        @Test
        @DisplayName("Debe retornar el top 5 de lugares ordenados por rating")
        void shouldReturnTop5RatedPlaces() {
            when(placeRepository.findTop5ByOrderByRatingDesc()).thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.getTopRated();

            assertThat(result).hasSize(1);
            verify(logRepository).save(any(LogEntity.class));
            verify(placeRepository).findTop5ByOrderByRatingDesc();
        }

        @Test
        @DisplayName("Debe buscar lugares por criterio de texto (nombre, dirección o ciudad)")
        void shouldSearchPlacesByQuery() {
            String query = "Cristo";
            when(placeRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(query, query, query))
                    .thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.search(query);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains(query);
            verify(logRepository).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe retornar todos los lugares registrados")
        void shouldReturnAllPlaces() {
            when(placeRepository.findAll()).thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.getAllPlaces();

            assertThat(result).isNotEmpty();
            verify(placeRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Pruebas de Búsqueda por ID")
    class GetByIdTests {

        @Test
        @DisplayName("Debe retornar un lugar cuando el ID existe")
        void shouldReturnPlaceWhenIdExists() {
            when(placeRepository.findById(10L)).thenReturn(Optional.of(samplePlace));

            PlaceResponseDTO result = placeService.getPlaceById(10L);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Cristo de la Concordia");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando el ID no existe")
        void shouldThrowExceptionWhenIdNotFound() {
            when(placeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> placeService.getPlaceById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Lugar no encontrado con ID: 99");

            verify(logRepository, times(2)).save(any(LogEntity.class));
        }
    }

    @Nested
    @DisplayName("Pruebas por Departamento (Ciudad)")
    class DepartmentQueriesTests {

        @Test
        @DisplayName("Debe retornar lugares filtrados por ciudad y activos")
        void shouldReturnPlacesByCityId() {
            when(placeRepository.findByCityIdAndStateTrue(1L)).thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.getPlacesByDepartment(1L);

            assertThat(result).hasSize(1);
            verify(placeRepository).findByCityIdAndStateTrue(1L);
        }

        @Test
        @DisplayName("Debe retornar el top 3 de lugares de un departamento específico")
        void shouldReturnTop3PlacesByDepartment() {
            when(placeRepository.findTop3ByCityIdAndStateTrueOrderByRatingDesc(1L)).thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.getTopPlacesByDepartment(1L);

            assertThat(result).hasSize(1);
            verify(placeRepository).findTop3ByCityIdAndStateTrueOrderByRatingDesc(1L);
        }

        @Test
        @DisplayName("Debe manejar correctamente cuando un departamento no tiene lugares")
        void shouldReturnEmptyListWhenDepartmentHasNoPlaces() {
            when(placeRepository.findByCityIdAndStateTrue(2L)).thenReturn(Collections.emptyList());

            List<PlaceResponseDTO> result = placeService.getPlacesByDepartment(2L);

            assertThat(result).isEmpty();
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
        }
    }
}