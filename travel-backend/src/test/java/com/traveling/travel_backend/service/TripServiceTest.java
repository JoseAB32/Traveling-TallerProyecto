package com.traveling.travel_backend.service;

import com.traveling.travel_backend.dto.TripDraftRequest;
import com.traveling.travel_backend.dto.TripDraftResponse;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock private TripRepository tripRepository;
    @Mock private TripItemRepository tripItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private PlaceRepository placeRepository;
    @Mock private SupabaseReminderService supabaseReminderService;  // ← AGREGAR ESTE MOCK

    private Authentication authentication;

    @InjectMocks
    private TripService tripService;

    private User sampleUser;
    private Place samplePlace;
    private Trip sampleTrip;
    private TripItem sampleItem;

    @BeforeEach
    void setUp() {
        authentication = new UsernamePasswordAuthenticationToken("traveler_joe", null, List.of());

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUserName("traveler_joe");
        sampleUser.setCorreo("traveler_joe@example.com");  // ← AGREGAR EMAIL

        samplePlace = new Place();
        samplePlace.setId(50L);
        samplePlace.setName("Salar de Uyuni");

        sampleTrip = new Trip();
        sampleTrip.setId(100L);
        sampleTrip.setUser(sampleUser);
        sampleTrip.setName("Mi viaje");
        sampleTrip.setStartDate("2026-06-01");  // ← AGREGAR FECHA
        sampleTrip.setState(true);

        sampleItem = new TripItem(sampleTrip, samplePlace, 1);
    }

    @Nested
    @DisplayName("Tests de Obtención de Borrador (getMyDraft)")
    class GetDraftTests {

        @Test
        @DisplayName("Debe retornar un borrador vacío si el usuario no tiene viajes activos")
        void shouldReturnEmptyDraftWhenNoTripExists() {
            when(userRepository.findByUserName("traveler_joe")).thenReturn(Optional.of(sampleUser));
            when(tripRepository.findFirstByUserIdAndStateTrueOrderByIdDesc(1L)).thenReturn(Optional.empty());

            TripDraftResponse response = tripService.getMyDraft(authentication);

            assertThat(response.getUserId()).isEqualTo(1L);
            assertThat(response.getPlaces()).isEmpty();
            assertThat(response.getName()).isEqualTo("Mi itinerario");
        }

        @Test
        @DisplayName("Debe retornar el borrador con sus lugares si existe")
        void shouldReturnDraftWithPlaces() {
            when(userRepository.findByUserName("traveler_joe")).thenReturn(Optional.of(sampleUser));
            when(tripRepository.findFirstByUserIdAndStateTrueOrderByIdDesc(1L)).thenReturn(Optional.of(sampleTrip));
            when(tripItemRepository.findByTripIdAndStateTrueOrderByVisitOrderAsc(100L)).thenReturn(List.of(sampleItem));

            TripDraftResponse response = tripService.getMyDraft(authentication);

            assertThat(response.getTripId()).isEqualTo(100L);
            assertThat(response.getPlaces()).hasSize(1);
            assertThat(response.getPlaces().get(0).getName()).isEqualTo("Salar de Uyuni");
        }
    }

    @Nested
    @DisplayName("Tests de Guardado y Creación (saveDraft / createTrip)")
    class SaveAndCreateTests {

        @Test
        @DisplayName("Debe guardar un borrador existente y reemplazar sus items")
        void shouldUpdateExistingDraft() {
            TripDraftRequest request = new TripDraftRequest();
            request.setName("Ruta actualizada");
            request.setPlaceIds(List.of(50L));

            when(userRepository.findByUserName("traveler_joe")).thenReturn(Optional.of(sampleUser));
            when(tripRepository.findFirstByUserIdAndStateTrueOrderByIdDesc(1L)).thenReturn(Optional.of(sampleTrip));
            when(tripRepository.save(any(Trip.class))).thenReturn(sampleTrip);
            when(placeRepository.findById(50L)).thenReturn(Optional.of(samplePlace));

            TripDraftResponse response = tripService.saveDraft(request, authentication);

            assertThat(response.getName()).isEqualTo("Ruta actualizada");
            verify(tripItemRepository).deleteByTripId(100L);
            verify(tripItemRepository).save(any(TripItem.class));
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si un lugar de la lista no existe")
        void shouldThrowExceptionWhenPlaceNotFound() {
            TripDraftRequest request = new TripDraftRequest();
            request.setPlaceIds(List.of(999L));

            when(userRepository.findByUserName("traveler_joe")).thenReturn(Optional.of(sampleUser));
            when(tripRepository.save(any(Trip.class))).thenReturn(sampleTrip);
            when(placeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tripService.saveDraft(request, authentication))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Lugar no encontrado con ID: 999");
        }

        @Test
        @DisplayName("Debe crear un nuevo viaje y llamar a SupabaseReminderService")
        void shouldCreateTripAndCallReminderService() {
            TripDraftRequest request = new TripDraftRequest();
            request.setName("Nuevo viaje");
            request.setStartDate("2026-06-15");
            request.setEndDate("2026-06-20");
            request.setPlaceIds(List.of(50L));

            Trip newTrip = new Trip();
            newTrip.setId(200L);
            newTrip.setUser(sampleUser);
            newTrip.setName("Nuevo viaje");
            newTrip.setStartDate("2026-06-15");
            newTrip.setEndDate("2026-06-20");
            newTrip.setState(true);

            when(userRepository.findByUserName("traveler_joe")).thenReturn(Optional.of(sampleUser));
            when(tripRepository.save(any(Trip.class))).thenReturn(newTrip);
            when(placeRepository.findById(50L)).thenReturn(Optional.of(samplePlace));

            TripDraftResponse response = tripService.createTrip(request, authentication);

            // Verificar que se llamó a SupabaseReminderService
            verify(supabaseReminderService).createReminder(
                eq(sampleUser.getCorreo()),
                eq(sampleUser.getUserName()),
                eq("Nuevo viaje"),
                eq("2026-06-15"),
                eq(200L)
            );

            assertThat(response.getName()).isEqualTo("Nuevo viaje");
            assertThat(response.getTripId()).isEqualTo(200L);
        }
    }

    @Nested
    @DisplayName("Tests de Seguridad")
    class SecurityTests {

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si no hay sesión")
        void shouldThrowUnauthorizedWhenNoSession() {
            assertThatThrownBy(() -> tripService.getMyDraft(null))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si el usuario no existe en BD")
        void shouldThrowUnauthorizedWhenUserNotFound() {
            when(userRepository.findByUserName("traveler_joe")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tripService.getMyDraft(authentication))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }
}