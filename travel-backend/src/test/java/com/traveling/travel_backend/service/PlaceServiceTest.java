package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.PlaceResponseDTO;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.repository.CityRepository;
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
import com.traveling.travel_backend.dto.CreatePlaceRequestDTO;
import com.traveling.travel_backend.exception.BadRequestException;
import com.traveling.travel_backend.model.PlaceImage;
import com.traveling.travel_backend.repository.PlaceImageRepository;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    private CityRepository cityRepository;

    @Mock
    private LogRepository logRepository;

    @Mock
    private TranslationsService translationsService;

    @Mock
    private PlaceImageRepository placeImageRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private PlaceService placeService;

    private Place samplePlace;
    private City sampleCity;

    @BeforeEach
    void setUp() {
        sampleCity = new City();
        sampleCity.setId(1L);
        sampleCity.setName("Cochabamba");
        sampleCity.setState(true);
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
    @DisplayName("Pruebas de creación de lugares turísticos")
    class CreatePlaceTests {

    @Test
    @DisplayName("Debe crear un lugar turístico con varias imágenes y marcar la primera como principal")
    void shouldCreatePlaceWithMultipleImagesAndMarkFirstAsMain() {
        CreatePlaceRequestDTO request = validCreatePlaceRequest();

        MockMultipartFile image1 = imageFile("madidi-1.jpg", "image/jpeg", 1024);
        MockMultipartFile image2 = imageFile("madidi-2.jpg", "image/jpeg", 2048);

        when(cityRepository.findById(1L)).thenReturn(Optional.of(sampleCity));

        when(placeRepository.existsByNameIgnoreCaseAndCityIdAndStateTrue(
                "Parque Nacional Madidi",
                1L
        )).thenReturn(false);

        when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> {
            Place place = invocation.getArgument(0);
            place.setId(100L);
            return place;
        });

        when(cloudinaryService.uploadPlaceImage(image1, 100L, 0))
                .thenReturn("https://res.cloudinary.com/test/madidi-1.jpg");

        when(cloudinaryService.uploadPlaceImage(image2, 100L, 1))
                .thenReturn("https://res.cloudinary.com/test/madidi-2.jpg");

        when(placeImageRepository.save(any(PlaceImage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlaceResponseDTO result = placeService.createPlace(request, List.of(image1, image2));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("Parque Nacional Madidi");

        ArgumentCaptor<PlaceImage> imageCaptor = ArgumentCaptor.forClass(PlaceImage.class);

        verify(placeImageRepository, times(2)).save(imageCaptor.capture());

        List<PlaceImage> savedImages = imageCaptor.getAllValues();

        assertThat(savedImages.get(0).getImageUrl())
                .isEqualTo("https://res.cloudinary.com/test/madidi-1.jpg");
        assertThat(savedImages.get(0).getDisplayOrder()).isEqualTo(0);
        assertThat(savedImages.get(0).getIsMain()).isTrue();
        assertThat(savedImages.get(0).isState()).isTrue();
        assertThat(savedImages.get(0).getPlace().getId()).isEqualTo(100L);

        assertThat(savedImages.get(1).getImageUrl())
                .isEqualTo("https://res.cloudinary.com/test/madidi-2.jpg");
        assertThat(savedImages.get(1).getDisplayOrder()).isEqualTo(1);
        assertThat(savedImages.get(1).getIsMain()).isFalse();
        assertThat(savedImages.get(1).isState()).isTrue();
        assertThat(savedImages.get(1).getPlace().getId()).isEqualTo(100L);

        verify(cloudinaryService).uploadPlaceImage(image1, 100L, 0);
        verify(cloudinaryService).uploadPlaceImage(image2, 100L, 1);
        verify(logRepository).save(any(LogEntity.class));
    }

    @Test
    @DisplayName("Debe crear un lugar turístico sin imágenes cuando no se envían archivos")
    void shouldCreatePlaceWithoutImagesWhenFilesAreNull() {
        CreatePlaceRequestDTO request = validCreatePlaceRequest();

        when(cityRepository.findById(1L)).thenReturn(Optional.of(sampleCity));

        when(placeRepository.existsByNameIgnoreCaseAndCityIdAndStateTrue(
                "Parque Nacional Madidi",
                1L
        )).thenReturn(false);

        when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> {
            Place place = invocation.getArgument(0);
            place.setId(101L);
            return place;
        });

        PlaceResponseDTO result = placeService.createPlace(request, null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getName()).isEqualTo("Parque Nacional Madidi");

        verify(placeRepository).save(any(Place.class));
        verifyNoInteractions(cloudinaryService);
        verify(placeImageRepository, never()).save(any(PlaceImage.class));
        verify(logRepository).save(any(LogEntity.class));
    }

    @Test
    @DisplayName("Debe rechazar más de 5 imágenes")
    void shouldRejectMoreThanFiveImages() {
        CreatePlaceRequestDTO request = validCreatePlaceRequest();

        List<MultipartFile> images = List.of(
                imageFile("img-1.jpg", "image/jpeg", 100),
                imageFile("img-2.jpg", "image/jpeg", 100),
                imageFile("img-3.jpg", "image/jpeg", 100),
                imageFile("img-4.jpg", "image/jpeg", 100),
                imageFile("img-5.jpg", "image/jpeg", 100),
                imageFile("img-6.jpg", "image/jpeg", 100)
        );

        assertThatThrownBy(() -> placeService.createPlace(request, images))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Solo se permite subir hasta 5 imágenes");

        verify(placeRepository, never()).save(any(Place.class));
        verifyNoInteractions(cloudinaryService);
        verifyNoInteractions(placeImageRepository);
    }

    @Test
    @DisplayName("Debe rechazar archivos que no sean imágenes")
    void shouldRejectNonImageFiles() {
        CreatePlaceRequestDTO request = validCreatePlaceRequest();

        MockMultipartFile pdfFile = new MockMultipartFile(
                "images",
                "documento.pdf",
                "application/pdf",
                new byte[]{1, 2, 3}
        );

        assertThatThrownBy(() -> placeService.createPlace(request, List.of(pdfFile)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Todos los archivos seleccionados deben ser imágenes");

        verify(placeRepository, never()).save(any(Place.class));
        verifyNoInteractions(cloudinaryService);
        verifyNoInteractions(placeImageRepository);
    }

    @Test
    @DisplayName("Debe rechazar imágenes mayores a 5 MB")
    void shouldRejectImagesGreaterThanFiveMb() {
        CreatePlaceRequestDTO request = validCreatePlaceRequest();

        byte[] bigContent = new byte[(5 * 1024 * 1024) + 1];

        MockMultipartFile bigImage = new MockMultipartFile(
                "images",
                "imagen-grande.jpg",
                "image/jpeg",
                bigContent
        );

        assertThatThrownBy(() -> placeService.createPlace(request, List.of(bigImage)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cada imagen no debe superar los 5 MB");

        verify(placeRepository, never()).save(any(Place.class));
        verifyNoInteractions(cloudinaryService);
        verifyNoInteractions(placeImageRepository);
    }

    @Test
    @DisplayName("Debe rechazar lugar duplicado en la misma ciudad")
    void shouldRejectDuplicatedPlaceInSameCity() {
        CreatePlaceRequestDTO request = validCreatePlaceRequest();

        when(cityRepository.findById(1L)).thenReturn(Optional.of(sampleCity));

        when(placeRepository.existsByNameIgnoreCaseAndCityIdAndStateTrue(
                "Parque Nacional Madidi",
                1L
        )).thenReturn(true);

        assertThatThrownBy(() -> placeService.createPlace(request, Collections.emptyList()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ya existe un lugar turístico activo con ese nombre");

        verify(placeRepository, never()).save(any(Place.class));
        verifyNoInteractions(cloudinaryService);
        verifyNoInteractions(placeImageRepository);
    }
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
            when(placeRepository.findByStateTrue()).thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.getSearchCache();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.get(0).getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monumento");

            verify(placeRepository).findByStateTrue();
            verifyNoInteractions(translationsService);
            verify(logRepository, never()).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe retornar cache de búsqueda con lugares activos sin traducir y sin guardar logs")
        void shouldReturnSearchCacheWithActivePlacesWithoutTranslationAndWithoutSavingLogs() {
            when(placeRepository.findByStateTrue()).thenReturn(List.of(samplePlace));

            List<PlaceResponseDTO> result = placeService.getSearchCache();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(10L);
            assertThat(result.get(0).getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.get(0).getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.get(0).getPlaceType()).isEqualTo("Monumento");

            verify(placeRepository).findByStateTrue();
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

        private CreatePlaceRequestDTO validCreatePlaceRequest() {
        CreatePlaceRequestDTO request = new CreatePlaceRequestDTO();
        request.setName("Parque Nacional Madidi");
        request.setDescription("Área protegida de gran biodiversidad ubicada en el norte de La Paz.");
        request.setAddress("Región norte del departamento de La Paz");
        request.setPrice(100.0);
        request.setLatitude(-14.2500);
        request.setLongitude(-68.7500);
        request.setPlaceType("Natural");
        request.setCityId(1L);
        request.setIsEvent(false);
        request.setStartDate(null);
        request.setEndDate(null);
        return request;
    }

    private MockMultipartFile imageFile(String filename, String contentType, int sizeInBytes) {
        return new MockMultipartFile(
                "images",
                filename,
                contentType,
                new byte[sizeInBytes]
        );
    }
}