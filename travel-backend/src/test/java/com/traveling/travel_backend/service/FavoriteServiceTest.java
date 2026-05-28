package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.FavoriteResponseDTO;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.exception.UnauthorizedException;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.Favorite;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.FavoriteRepository;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.PlaceRepository;
import com.traveling.travel_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private LogRepository logRepository;

    @Mock
    private TranslationsService translationsService;

    @InjectMocks
    private FavoriteService favoriteService;

    private Authentication authentication;
    private User sampleUser;
    private City sampleCity;
    private Place samplePlace;
    private Favorite sampleFavorite;

    @BeforeEach
    void setUp() {
        authentication = new UsernamePasswordAuthenticationToken("testuser", null, List.of());

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUserName("testuser");

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

        sampleFavorite = new Favorite(sampleUser, samplePlace);
        sampleFavorite.setId(100L);
        sampleFavorite.setState(true);
    }

    @Nested
    @DisplayName("Seguridad y resolución de usuario")
    class SecurityTests {

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si la autenticación es nula")
        void shouldThrowUnauthorizedWhenAuthIsNull() {
            assertThatThrownBy(() -> favoriteService.getUserFavorites(null, AppConstants.DEFAULT_LANGUAGE))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("No autenticado.");

            verifyNoInteractions(userRepository);
            verifyNoInteractions(favoriteRepository);
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si el usuario no existe en BD")
        void shouldThrowUnauthorizedWhenUserNotFound() {
            when(userRepository.findByUserName("testuser")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteService.getUserFavorites(authentication, AppConstants.DEFAULT_LANGUAGE))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Usuario no válido.");

            verify(userRepository).findByUserName("testuser");
            verifyNoInteractions(favoriteRepository);
            verifyNoInteractions(translationsService);
        }
    }

    @Nested
    @DisplayName("Caso de uso: agregar favorito")
    class AddFavoriteTests {

        @BeforeEach
        void mockUserResolution() {
            when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(sampleUser));
        }

        @Test
        @DisplayName("Debe crear un nuevo favorito si no existía")
        void shouldCreateNewFavorite() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(placeRepository.findById(10L)).thenReturn(Optional.of(samplePlace));
            when(favoriteRepository.findByUserIdAndPlaceId(1L, 10L)).thenReturn(Optional.empty());
            when(favoriteRepository.save(any(Favorite.class))).thenReturn(sampleFavorite);

            FavoriteResponseDTO result = favoriteService.addFavorite(authentication, 10L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getPlace()).isNotNull();
            assertThat(result.getPlace().getName()).isEqualTo("Cristo de la Concordia");

            verify(userRepository).findByUserName("testuser");
            verify(userRepository).findById(1L);
            verify(placeRepository).findById(10L);
            verify(favoriteRepository).findByUserIdAndPlaceId(1L, 10L);
            verify(favoriteRepository).save(any(Favorite.class));
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe reactivar un favorito si ya existía pero estaba desactivado")
        void shouldReactivateExistingFavorite() {
            sampleFavorite.setState(false);

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(placeRepository.findById(10L)).thenReturn(Optional.of(samplePlace));
            when(favoriteRepository.findByUserIdAndPlaceId(1L, 10L)).thenReturn(Optional.of(sampleFavorite));
            when(favoriteRepository.save(sampleFavorite)).thenReturn(sampleFavorite);

            FavoriteResponseDTO result = favoriteService.addFavorite(authentication, 10L);

            assertThat(result).isNotNull();
            assertThat(sampleFavorite.isState()).isTrue();

            verify(userRepository).findByUserName("testuser");
            verify(userRepository).findById(1L);
            verify(placeRepository).findById(10L);
            verify(favoriteRepository).findByUserIdAndPlaceId(1L, 10L);
            verify(favoriteRepository).save(sampleFavorite);
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe devolver el favorito existente si ya estaba activo")
        void shouldReturnExistingFavoriteWhenAlreadyActive() {
            sampleFavorite.setState(true);

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(placeRepository.findById(10L)).thenReturn(Optional.of(samplePlace));
            when(favoriteRepository.findByUserIdAndPlaceId(1L, 10L)).thenReturn(Optional.of(sampleFavorite));

            FavoriteResponseDTO result = favoriteService.addFavorite(authentication, 10L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(sampleFavorite.isState()).isTrue();

            verify(userRepository).findByUserName("testuser");
            verify(userRepository).findById(1L);
            verify(placeRepository).findById(10L);
            verify(favoriteRepository).findByUserIdAndPlaceId(1L, 10L);
            verify(favoriteRepository, never()).save(any(Favorite.class));
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe fallar si el usuario no existe por ID al agregar favorito")
        void shouldFailIfUserByIdNotFoundWhenAddingFavorite() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteService.addFavorite(authentication, 10L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository).findByUserName("testuser");
            verify(userRepository).findById(1L);
            verifyNoInteractions(placeRepository);
            verifyNoInteractions(favoriteRepository);
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe fallar si el lugar no existe al agregar favorito")
        void shouldFailIfPlaceNotFoundWhenAddingFavorite() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(placeRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteService.addFavorite(authentication, 10L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository).findByUserName("testuser");
            verify(userRepository).findById(1L);
            verify(placeRepository).findById(10L);
            verifyNoInteractions(favoriteRepository);
            verifyNoInteractions(translationsService);
        }
    }

    @Nested
    @DisplayName("Caso de uso: consultar favoritos")
    class QueryFavoritesTests {

        @BeforeEach
        void mockUserResolution() {
            when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(sampleUser));
        }

        @Test
        @DisplayName("Debe listar favoritos activos sin traducir cuando el idioma es español")
        void shouldListActiveFavoritesWithoutTranslationWhenLanguageIsSpanish() {
            when(favoriteRepository.findByUserIdAndStateTrue(1L)).thenReturn(List.of(sampleFavorite));

            List<FavoriteResponseDTO> result = favoriteService.getUserFavorites(authentication, AppConstants.DEFAULT_LANGUAGE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(100L);
            assertThat(result.get(0).getPlace()).isNotNull();
            assertThat(result.get(0).getPlace().getName()).isEqualTo("Cristo de la Concordia");
            assertThat(result.get(0).getPlace().getAddress()).isEqualTo("Cerro San Pedro");
            assertThat(result.get(0).getPlace().getDescription()).isEqualTo("Monumento turístico ubicado en Cochabamba");

            verify(userRepository).findByUserName("testuser");
            verify(favoriteRepository).findByUserIdAndStateTrue(1L);
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe listar favoritos traduciendo nombre, dirección y descripción cuando el idioma no es español")
        void shouldListActiveFavoritesWithTranslatedPlaceFieldsWhenLanguageIsNotSpanish() {
            when(favoriteRepository.findByUserIdAndStateTrue(1L)).thenReturn(List.of(sampleFavorite));
            mockFavoritePlaceTranslations("en");

            List<FavoriteResponseDTO> result = favoriteService.getUserFavorites(authentication, "en");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPlace()).isNotNull();
            assertThat(result.get(0).getPlace().getName()).isEqualTo("Christ of Concord");
            assertThat(result.get(0).getPlace().getAddress()).isEqualTo("San Pedro Hill");
            assertThat(result.get(0).getPlace().getDescription()).isEqualTo("Tourist monument located in Cochabamba");
            assertThat(result.get(0).getPlace().getPlaceType()).isEqualTo("Monumento");

            verify(userRepository).findByUserName("testuser");
            verify(favoriteRepository).findByUserIdAndStateTrue(1L);
            verifyFavoritePlaceTranslationCalls("en");
            verifyNoPlaceTypeTranslationCall("en");
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando el usuario no tiene favoritos")
        void shouldReturnEmptyListWhenUserHasNoFavorites() {
            when(favoriteRepository.findByUserIdAndStateTrue(1L)).thenReturn(List.of());

            List<FavoriteResponseDTO> result = favoriteService.getUserFavorites(authentication, AppConstants.DEFAULT_LANGUAGE);

            assertThat(result).isEmpty();

            verify(userRepository).findByUserName("testuser");
            verify(favoriteRepository).findByUserIdAndStateTrue(1L);
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("No debe traducir campos nulos o vacíos del lugar favorito")
        void shouldNotTranslateNullOrBlankFavoritePlaceFields() {
            samplePlace.setName(null);
            samplePlace.setAddress("   ");
            samplePlace.setDescription("");

            when(favoriteRepository.findByUserIdAndStateTrue(1L)).thenReturn(List.of(sampleFavorite));

            List<FavoriteResponseDTO> result = favoriteService.getUserFavorites(authentication, "en");

            assertThat(result).hasSize(1);

            verify(userRepository).findByUserName("testuser");
            verify(favoriteRepository).findByUserIdAndStateTrue(1L);
            verifyNoInteractions(translationsService);
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
        }
    }

    @Nested
    @DisplayName("Caso de uso: eliminar favorito")
    class RemoveFavoriteTests {

        @BeforeEach
        void mockUserResolution() {
            when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(sampleUser));
        }

        @Test
        @DisplayName("Debe realizar soft delete desactivando un favorito")
        void shouldPerformSoftDelete() {
            when(favoriteRepository.findByUserIdAndPlaceId(1L, 10L)).thenReturn(Optional.of(sampleFavorite));

            favoriteService.removeFavorite(authentication, 10L);

            assertThat(sampleFavorite.isState()).isFalse();

            verify(userRepository).findByUserName("testuser");
            verify(favoriteRepository).findByUserIdAndPlaceId(1L, 10L);
            verify(favoriteRepository).save(sampleFavorite);
            verify(logRepository).save(argThat(log -> log.getMessage().contains("desactivado")));
            verifyNoInteractions(translationsService);
        }

        @Test
        @DisplayName("Debe fallar al intentar remover un favorito inexistente")
        void shouldFailToRemoveInexistentFavorite() {
            when(favoriteRepository.findByUserIdAndPlaceId(1L, 10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteService.removeFavorite(authentication, 10L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository).findByUserName("testuser");
            verify(favoriteRepository).findByUserIdAndPlaceId(1L, 10L);
            verify(favoriteRepository, never()).save(any(Favorite.class));
            verifyNoInteractions(translationsService);
        }
    }

    private void mockFavoritePlaceTranslations(String language) {
        when(translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_NAME,
                language,
                "Cristo de la Concordia"
        )).thenReturn("Christ of Concord");

        when(translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_ADDRESS,
                language,
                "Cerro San Pedro"
        )).thenReturn("San Pedro Hill");

        when(translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_DESCRIPTION,
                language,
                "Monumento turístico ubicado en Cochabamba"
        )).thenReturn("Tourist monument located in Cochabamba");
    }

    private void verifyFavoritePlaceTranslationCalls(String language) {
        verify(translationsService).getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_NAME,
                language,
                "Cristo de la Concordia"
        );

        verify(translationsService).getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_ADDRESS,
                language,
                "Cerro San Pedro"
        );

        verify(translationsService).getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                10L,
                AppConstants.FIELD_DESCRIPTION,
                language,
                "Monumento turístico ubicado en Cochabamba"
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