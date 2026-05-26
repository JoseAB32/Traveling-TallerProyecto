package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
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

    @Mock
    private TranslationsService translationsService;

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
        samplePlace.setDescription("Monumento turístico ubicado en Cochabamba");
        samplePlace.setAddress("Cerro San Pedro");
        samplePlace.setPlaceType("Monumento");
        samplePlace.setRating(5.0);
        samplePlace.setCity(sampleCity);
        samplePlace.setState(true);
    }

    @Nested
    @DisplayName("Pruebas de Consultas Generales y Top Rated")
    class GeneralQueriesTests {

        @Test
        @DisplayName("Debe retornar el top 5 de lugares sin traducir cuando el idioma es español")
        void shouldReturnTop5RatedPlacesWithoutTranslationWhenLanguageIsSpanish() {
            when(placeRepository.findTop5ByOrderByRatingDesc()).thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.getTopRated(AppConstants.DEFAULT_LANGUAGE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.get(0).getDescription()).isEqualTo("Monumento turístico ubicado en Cochabamba");
            assertThat(result.get(0).getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monumento");

            verify(logRepository).save(any(LogEntity.class));
            verify(placeRepository).findTop5ByOrderByRatingDesc();
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe retornar el top 5 traduciendo solo el nombre cuando el idioma no es español")
        void shouldReturnTop5RatedPlacesTranslatingOnlyNameWhenLanguageIsNotSpanish() {
            when(placeRepository.findTop5ByOrderByRatingDesc()).thenReturn(List.of(samplePlace));

            when(translationsService.getTranslation(
                    AppConstants.ENTITY_TYPE_PLACE,
                    10L,
                    AppConstants.FIELD_NAME,
                    "en",
                    "Cristo de la Concordia"
            )).thenReturn("Christ of Concord");

            List<PlaceResponseDTO> result = placeService.getTopRated("en");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Christ of Concord");
            assertThat(result.get(0).getDescription()).isEqualTo("Monumento turístico ubicado en Cochabamba");
            assertThat(result.get(0).getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monumento");

            verify(logRepository).save(any(LogEntity.class));
            verify(placeRepository).findTop5ByOrderByRatingDesc();

            verify(translationsService).getTranslation(
                    AppConstants.ENTITY_TYPE_PLACE,
                    10L,
                    AppConstants.FIELD_NAME,
                    "en",
                    "Cristo de la Concordia"
            );

            verify(translationsService, never()).getTranslation(
                    AppConstants.ENTITY_TYPE_PLACE,
                    10L,
                    AppConstants.FIELD_DESCRIPTION,
                    "en",
                    "Monumento turístico ubicado en Cochabamba"
            );

            verify(translationsService, never()).getTranslation(
                    AppConstants.ENTITY_TYPE_PLACE,
                    10L,
                    AppConstants.FIELD_ADDRESS,
                    "en",
                    "Cerro San Pedro"
            );

            verify(translationsService, never()).getTranslation(
                    AppConstants.ENTITY_TYPE_PLACE,
                    10L,
                    AppConstants.FIELD_PLACE_TYPE,
                    "en",
                    "Monumento"
            );
        }

        @Test
        @DisplayName("Debe buscar lugares por criterio de texto sin traducir cuando el idioma es español")
        void shouldSearchPlacesByQueryWithoutTranslationWhenLanguageIsSpanish() {
            String query = "Cristo";

            when(placeRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(query, query, query))
                    .thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.search(query, AppConstants.DEFAULT_LANGUAGE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.get(0).getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.get(0).getDescription()).isEqualTo("Monumento turístico ubicado en Cochabamba");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monumento");

            verify(logRepository).save(any(LogEntity.class));
            verify(placeRepository).findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(query, query, query);
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe buscar lugares por criterio de texto y traducir solo nombre y dirección cuando el idioma no es español")
        void shouldSearchPlacesByQueryTranslatingOnlyNameAndAddressWhenLanguageIsNotSpanish() {
            String query = "Cristo";

            when(placeRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(query, query, query))
                    .thenReturn(List.of(samplePlace));
            mockNameTranslation("en");
            mockAddressTranslation("en");

            List<PlaceResponseDTO> result = placeService.search(query, "en");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Christ of Concord");
            assertThat(result.get(0).getAddress()).isEqualTo("San Pedro Hill");
            assertThat(result.get(0).getDescription()).isEqualTo("Monumento turístico ubicado en Cochabamba");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monumento");

            verifyNameTranslationCall("en");
            verifyAddressTranslationCall("en");
            verifyNoDescriptionTranslationCall("en");
            verifyNoPlaceTypeTranslationCall("en");
            verify(logRepository).save(any(LogEntity.class));
            verify(placeRepository).findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(query, query, query);
        }

        @Test
        @DisplayName("Debe retornar todos los lugares registrados sin traducir cuando el idioma es español")
        void shouldReturnAllPlacesWithoutTranslationWhenLanguageIsSpanish() {
            when(placeRepository.findAll()).thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.getAllPlaces(AppConstants.DEFAULT_LANGUAGE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.get(0).getDescription()).isEqualTo("Monumento turístico ubicado en Cochabamba");
            assertThat(result.get(0).getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monumento");

            verify(placeRepository).findAll();
            verify(logRepository).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe retornar todos los lugares traduciendo solo tipo de lugar cuando el idioma no es español")
        void shouldReturnAllPlacesTranslatingOnlyPlaceTypeWhenLanguageIsNotSpanish() {
            when(placeRepository.findAll()).thenReturn(List.of(samplePlace));
            mockPlaceTypeTranslation("en");

            List<PlaceResponseDTO> result = placeService.getAllPlaces("en");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.get(0).getDescription()).isEqualTo("Monumento turístico ubicado en Cochabamba");
            assertThat(result.get(0).getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monument");

            verifyPlaceTypeTranslationCall("en");
            verifyNoNameTranslationCall("en");
            verifyNoDescriptionTranslationCall("en");
            verifyNoAddressTranslationCall("en");
            verify(placeRepository).findAll();
            verify(logRepository).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe retornar cache de búsqueda sin traducir y sin guardar logs")
        void shouldReturnSearchCacheWithoutTranslationAndWithoutSavingLogs() {
            when(placeRepository.findAll()).thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.getSearchCache();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.get(0).getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monumento");

            verify(placeRepository).findAll();
            verifyNoInteractions(translationsService);
            verify(logRepository, never()).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe retornar solo lugares activos en cache de búsqueda")
        void shouldReturnOnlyActivePlacesInSearchCache() {
            Place inactivePlace = new Place();
            inactivePlace.setId(20L);
            inactivePlace.setName("Lugar inactivo");
            inactivePlace.setDescription("Descripción inactiva");
            inactivePlace.setAddress("Dirección inactiva");
            inactivePlace.setPlaceType("Museo");
            inactivePlace.setRating(3.0);
            inactivePlace.setCity(sampleCity);
            inactivePlace.setState(false);

            when(placeRepository.findAll()).thenReturn(List.of(samplePlace, inactivePlace));

            List<PlaceResponseDTO> result = placeService.getSearchCache();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(10L);
            assertThat(result.get(0).getName()).isEqualTo("Cristo de la Concordia");

            verify(placeRepository).findAll();
            verifyNoInteractions(translationsService);
            verify(logRepository, never()).save(any(LogEntity.class));
        }
    }

    @Nested
    @DisplayName("Pruebas de Búsqueda por ID")
    class GetByIdTests {

        @Test
        @DisplayName("Debe retornar un lugar sin traducir cuando el ID existe y el idioma es español")
        void shouldReturnPlaceWithoutTranslationWhenIdExistsAndLanguageIsSpanish() {
            when(placeRepository.findById(10L)).thenReturn(Optional.of(samplePlace));

            PlaceResponseDTO result = placeService.getPlaceById(10L, AppConstants.DEFAULT_LANGUAGE);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.getDescription()).isEqualTo("Monumento turístico ubicado en Cochabamba");
            assertThat(result.getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.getPlaceType()).isEqualTo("Monumento");

            verify(placeRepository).findById(10L);
            verify(logRepository).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe retornar un lugar traducido completo cuando el ID existe y el idioma no es español")
        void shouldReturnPlaceWithFullTranslationWhenIdExistsAndLanguageIsNotSpanish() {
            when(placeRepository.findById(10L)).thenReturn(Optional.of(samplePlace));
            mockAllPlaceTranslations("en");

            PlaceResponseDTO result = placeService.getPlaceById(10L, "en");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Christ of Concord");
            assertThat(result.getDescription()).isEqualTo("Tourist monument located in Cochabamba");
            assertThat(result.getAddress()).isEqualTo("San Pedro Hill");
            assertThat(result.getPlaceType()).isEqualTo("Monument");

            verifyAllPlaceTranslationCalls("en");
            verify(placeRepository).findById(10L);
            verify(logRepository).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando el ID no existe")
        void shouldThrowExceptionWhenIdNotFound() {
            when(placeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> placeService.getPlaceById(99L, AppConstants.DEFAULT_LANGUAGE))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Lugar no encontrado con ID: 99");

            verify(placeRepository).findById(99L);
            verify(logRepository, times(2)).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }
    }

    @Nested
    @DisplayName("Pruebas por Departamento")
    class DepartmentQueriesTests {

        @Test
        @DisplayName("Debe retornar lugares filtrados por ciudad y activos sin traducir cuando el idioma es español")
        void shouldReturnPlacesByCityIdWithoutTranslationWhenLanguageIsSpanish() {
            when(placeRepository.findByCityIdAndStateTrue(1L)).thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.getPlacesByDepartment(1L, AppConstants.DEFAULT_LANGUAGE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monumento");

            verify(placeRepository).findByCityIdAndStateTrue(1L);
            verify(logRepository).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe retornar lugares por ciudad traduciendo solo tipo de lugar cuando el idioma no es español")
        void shouldReturnPlacesByCityIdTranslatingOnlyPlaceTypeWhenLanguageIsNotSpanish() {
            when(placeRepository.findByCityIdAndStateTrue(1L)).thenReturn(List.of(samplePlace));
            mockPlaceTypeTranslation("en");

            List<PlaceResponseDTO> result = placeService.getPlacesByDepartment(1L, "en");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.get(0).getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.get(0).getDescription()).isEqualTo("Monumento turístico ubicado en Cochabamba");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monument");

            verifyPlaceTypeTranslationCall("en");
            verifyNoNameTranslationCall("en");
            verifyNoDescriptionTranslationCall("en");
            verifyNoAddressTranslationCall("en");
            verify(placeRepository).findByCityIdAndStateTrue(1L);
            verify(logRepository).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe retornar el top 3 de lugares de un departamento sin traducir cuando el idioma es español")
        void shouldReturnTop3PlacesByDepartmentWithoutTranslationWhenLanguageIsSpanish() {
            when(placeRepository.findTop3ByCityIdAndStateTrueOrderByRatingDesc(1L)).thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.getTopPlacesByDepartment(1L, AppConstants.DEFAULT_LANGUAGE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monumento");

            verify(placeRepository).findTop3ByCityIdAndStateTrueOrderByRatingDesc(1L);
            verify(logRepository).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe retornar el top 3 de lugares de un departamento traduciendo solo el nombre cuando el idioma no es español")
        void shouldReturnTop3PlacesByDepartmentTranslatingOnlyNameWhenLanguageIsNotSpanish() {
            when(placeRepository.findTop3ByCityIdAndStateTrueOrderByRatingDesc(1L))
                    .thenReturn(List.of(samplePlace));

            when(translationsService.getTranslation(
                    AppConstants.ENTITY_TYPE_PLACE,
                    10L,
                    AppConstants.FIELD_NAME,
                    "en",
                    "Cristo de la Concordia"
            )).thenReturn("Christ of Concord");

            List<PlaceResponseDTO> result = placeService.getTopPlacesByDepartment(1L, "en");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Christ of Concord");
            assertThat(result.get(0).getDescription()).isEqualTo("Monumento turístico ubicado en Cochabamba");
            assertThat(result.get(0).getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monumento");

            verify(placeRepository).findTop3ByCityIdAndStateTrueOrderByRatingDesc(1L);
            verify(logRepository).save(any(LogEntity.class));

            verify(translationsService).getTranslation(
                    AppConstants.ENTITY_TYPE_PLACE,
                    10L,
                    AppConstants.FIELD_NAME,
                    "en",
                    "Cristo de la Concordia"
            );

            verify(translationsService, never()).getTranslation(
                    AppConstants.ENTITY_TYPE_PLACE,
                    10L,
                    AppConstants.FIELD_DESCRIPTION,
                    "en",
                    "Monumento turístico ubicado en Cochabamba"
            );

            verify(translationsService, never()).getTranslation(
                    AppConstants.ENTITY_TYPE_PLACE,
                    10L,
                    AppConstants.FIELD_ADDRESS,
                    "en",
                    "Cerro San Pedro"
            );

            verify(translationsService, never()).getTranslation(
                    AppConstants.ENTITY_TYPE_PLACE,
                    10L,
                    AppConstants.FIELD_PLACE_TYPE,
                    "en",
                    "Monumento"
            );
        }

        @Test
        @DisplayName("Debe manejar correctamente cuando un departamento no tiene lugares")
        void shouldReturnEmptyListWhenDepartmentHasNoPlaces() {
            when(placeRepository.findByCityIdAndStateTrue(2L)).thenReturn(Collections.emptyList());

            List<PlaceResponseDTO> result = placeService.getPlacesByDepartment(2L, AppConstants.DEFAULT_LANGUAGE);

            assertThat(result).isEmpty();

            verify(placeRepository).findByCityIdAndStateTrue(2L);
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe manejar correctamente cuando un departamento no tiene lugares top")
        void shouldReturnEmptyListWhenDepartmentHasNoTopPlaces() {
            when(placeRepository.findTop3ByCityIdAndStateTrueOrderByRatingDesc(2L)).thenReturn(Collections.emptyList());

            List<PlaceResponseDTO> result = placeService.getTopPlacesByDepartment(2L, AppConstants.DEFAULT_LANGUAGE);

            assertThat(result).isEmpty();

            verify(placeRepository).findTop3ByCityIdAndStateTrueOrderByRatingDesc(2L);
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }
    }

    @Nested
    @DisplayName("Pruebas de campos vacíos o nulos")
    class EmptyFieldsTests {

        @Test
        @DisplayName("No debe traducir campos nulos o vacíos en detalle")
        void shouldNotTranslateNullOrBlankFieldsInDetail() {
            samplePlace.setName(null);
            samplePlace.setDescription("   ");
            samplePlace.setAddress(null);
            samplePlace.setPlaceType("");

            when(placeRepository.findById(10L)).thenReturn(Optional.of(samplePlace));

            PlaceResponseDTO result = placeService.getPlaceById(10L, "en");

            assertThat(result).isNotNull();

            verify(placeRepository).findById(10L);
            verify(logRepository).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("No debe traducir campos nulos o vacíos en búsqueda")
        void shouldNotTranslateNullOrBlankFieldsInSearch() {
            String query = "Cristo";

            samplePlace.setName(null);
            samplePlace.setAddress("   ");

            when(placeRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(query, query, query))
                    .thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.search(query, "en");

            assertThat(result).hasSize(1);

            verify(placeRepository).findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(query, query, query);
            verify(logRepository).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }
    }

    private void mockAllPlaceTranslations(String language) {
        mockNameTranslation(language);
        mockDescriptionTranslation(language);
        mockAddressTranslation(language);
        mockPlaceTypeTranslation(language);
    }

    private void mockNameTranslation(String language) {
        when(translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_NAME,
                language,
                "Cristo de la Concordia"
        )).thenReturn("Christ of Concord");
    }

    private void mockDescriptionTranslation(String language) {
        when(translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_DESCRIPTION,
                language,
                "Monumento turístico ubicado en Cochabamba"
        )).thenReturn("Tourist monument located in Cochabamba");
    }

    private void mockAddressTranslation(String language) {
        when(translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_ADDRESS,
                language,
                "Cerro San Pedro"
        )).thenReturn("San Pedro Hill");
    }

    private void mockPlaceTypeTranslation(String language) {
        when(translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_PLACE_TYPE,
                language,
                "Monumento"
        )).thenReturn("Monument");
    }

    private void verifyAllPlaceTranslationCalls(String language) {
        verifyNameTranslationCall(language);
        verifyDescriptionTranslationCall(language);
        verifyAddressTranslationCall(language);
        verifyPlaceTypeTranslationCall(language);
    }

    private void verifyNameTranslationCall(String language) {
        verify(translationsService).getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_NAME,
                language,
                "Cristo de la Concordia"
        );
    }

    private void verifyDescriptionTranslationCall(String language) {
        verify(translationsService).getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_DESCRIPTION,
                language,
                "Monumento turístico ubicado en Cochabamba"
        );
    }

    private void verifyAddressTranslationCall(String language) {
        verify(translationsService).getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_ADDRESS,
                language,
                "Cerro San Pedro"
        );
    }

    private void verifyPlaceTypeTranslationCall(String language) {
        verify(translationsService).getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_PLACE_TYPE,
                language,
                "Monumento"
        );
    }

    private void verifyNoNameTranslationCall(String language) {
        verify(translationsService, never()).getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_NAME,
                language,
                "Cristo de la Concordia"
        );
    }

    private void verifyNoDescriptionTranslationCall(String language) {
        verify(translationsService, never()).getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_DESCRIPTION,
                language,
                "Monumento turístico ubicado en Cochabamba"
        );
    }

    private void verifyNoAddressTranslationCall(String language) {
        verify(translationsService, never()).getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_ADDRESS,
                language,
                "Cerro San Pedro"
        );
    }

    private void verifyNoPlaceTypeTranslationCall(String language) {
        verify(translationsService, never()).getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_PLACE_TYPE,
                language,
                "Monumento"
        );
    }
}