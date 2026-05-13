package com.traveling.travel_backend.service;

import com.traveling.travel_backend.dto.FavoriteResponseDTO;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.exception.UnauthorizedException;
import com.traveling.travel_backend.model.*;
import com.traveling.travel_backend.repository.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private UserRepository userRepository;
    @Mock private PlaceRepository placeRepository;
    @Mock private LogRepository logRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    private Authentication authentication;

    private User sampleUser;
    private Place samplePlace;
    private Favorite sampleFavorite;

    @BeforeEach
    void setUp() {
        authentication = new UsernamePasswordAuthenticationToken("testuser", null, List.of());

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUserName("testuser");

        samplePlace = new Place();
        samplePlace.setId(10L);
        samplePlace.setName("Cristo de la Concordia");

        sampleFavorite = new Favorite(sampleUser, samplePlace);
        sampleFavorite.setId(100L);
        sampleFavorite.setState(true);
    }

    @Nested
    @DisplayName("Seguridad y Resolución de Usuario")
    class SecurityTests {

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si la autenticación es nula")
        void shouldThrowUnauthorizedWhenAuthIsNull() {
            assertThatThrownBy(() -> favoriteService.getUserFavorites(null))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("No autenticado.");
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si el usuario no existe en BD")
        void shouldThrowUnauthorizedWhenUserNotFound() {
            when(userRepository.findByUserName("testuser")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteService.getUserFavorites(authentication))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Usuario no válido.");
        }
    }

    @Nested
    @DisplayName("Caso de Uso: Agregar Favorito")
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
            verify(favoriteRepository).save(any(Favorite.class));
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe reactivar un favorito si ya existía pero estaba en false")
        void shouldReactivateExistingFavorite() {
            sampleFavorite.setState(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(placeRepository.findById(10L)).thenReturn(Optional.of(samplePlace));
            when(favoriteRepository.findByUserIdAndPlaceId(1L, 10L)).thenReturn(Optional.of(sampleFavorite));
            when(favoriteRepository.save(any(Favorite.class))).thenReturn(sampleFavorite);

            favoriteService.addFavorite(authentication, 10L);

            assertThat(sampleFavorite.isState()).isTrue();
            verify(favoriteRepository).save(sampleFavorite);
        }

        @Test
        @DisplayName("Debe fallar si el lugar no existe")
        void shouldFailIfPlaceNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(placeRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteService.addFavorite(authentication, 10L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Caso de Uso: Consultar y Eliminar")
    class QueryAndRemoveTests {

        @BeforeEach
        void mockUserResolution() {
            when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(sampleUser));
        }

        @Test
        @DisplayName("Debe listar favoritos activos")
        void shouldListActiveFavorites() {
            when(favoriteRepository.findByUserIdAndStateTrue(1L)).thenReturn(List.of(sampleFavorite));

            List<FavoriteResponseDTO> result = favoriteService.getUserFavorites(authentication);

            assertThat(result).hasSize(1);
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe realizar Soft Delete (desactivar) un favorito")
        void shouldPerformSoftDelete() {
            when(favoriteRepository.findByUserIdAndPlaceId(1L, 10L)).thenReturn(Optional.of(sampleFavorite));

            favoriteService.removeFavorite(authentication, 10L);

            assertThat(sampleFavorite.isState()).isFalse();
            verify(favoriteRepository).save(sampleFavorite);
            verify(logRepository).save(argThat(log -> log.getMessage().contains("desactivado")));
        }

        @Test
        @DisplayName("Debe fallar al intentar remover un favorito inexistente")
        void shouldFailToRemoveInexistentFavorite() {
            when(favoriteRepository.findByUserIdAndPlaceId(1L, 10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteService.removeFavorite(authentication, 10L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}