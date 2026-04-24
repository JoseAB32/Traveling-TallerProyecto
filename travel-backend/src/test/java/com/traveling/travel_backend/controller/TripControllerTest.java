package com.traveling.travel_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.model.Trip;
import com.traveling.travel_backend.model.TripItem;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.PlaceRepository;
import com.traveling.travel_backend.repository.TripItemRepository;
import com.traveling.travel_backend.repository.TripRepository;
import com.traveling.travel_backend.repository.UserRepository;
import com.traveling.travel_backend.security.JwtService;

@WebMvcTest(TripController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripRepository tripRepository;

    @MockBean
    private TripItemRepository tripItemRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PlaceRepository placeRepository;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/trips/draft/me retorna borrador vacío cuando no existe uno activo")
    void getMyDraftReturnsEmptyWhenNoDraftExists() throws Exception {
        User user = new User();
        user.setId(10L);
        user.setUserName("alice");

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
        when(tripRepository.findFirstByUserIdAndStateTrueOrderByIdDesc(10L)).thenReturn(Optional.empty());

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("alice", null, List.of());

        mockMvc.perform(get("/api/trips/draft/me").principal(auth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(10))
            .andExpect(jsonPath("$.name").value("Mi itinerario"))
            .andExpect(jsonPath("$.places").isArray())
            .andExpect(jsonPath("$.places.length()").value(0));
    }

    @Test
    @DisplayName("PUT /api/trips/draft guarda selección y retorna lugares")
    void saveDraftStoresSelectedPlaces() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUserName("alice");

        Trip existingTrip = new Trip();
        existingTrip.setId(20L);
        existingTrip.setUser(user);
        existingTrip.setState(true);

        Place first = new Place();
        first.setId(2L);
        first.setName("Cristo");

        Place second = new Place();
        second.setId(3L);
        second.setName("Laguna");

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
        when(tripRepository.findFirstByUserIdAndStateTrueOrderByIdDesc(1L)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.findByUserIdAndStateTrue(1L)).thenReturn(List.of(existingTrip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(placeRepository.findById(2L)).thenReturn(Optional.of(first));
        when(placeRepository.findById(3L)).thenReturn(Optional.of(second));
        doNothing().when(tripItemRepository).deleteByTripId(20L);
        when(tripItemRepository.save(any(TripItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String payload = objectMapper.writeValueAsString(
            java.util.Map.of(
                "name", "Mi itinerario",
                "startDate", "2026-05-01",
                "endDate", "2026-05-02",
                "placeIds", List.of(2, 3)
            )
        );

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("alice", null, List.of());

        mockMvc.perform(put("/api/trips/draft")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.name").value("Mi itinerario"))
            .andExpect(jsonPath("$.places.length()").value(2))
            .andExpect(jsonPath("$.places[0].id").value(2))
            .andExpect(jsonPath("$.places[1].id").value(3));
    }
}
