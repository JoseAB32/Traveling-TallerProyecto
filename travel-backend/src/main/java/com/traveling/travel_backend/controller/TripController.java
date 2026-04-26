package com.traveling.travel_backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.TripDraftRequest;
import com.traveling.travel_backend.dto.TripDraftResponse;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.model.Trip;
import com.traveling.travel_backend.model.TripItem;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.PlaceRepository;
import com.traveling.travel_backend.repository.TripItemRepository;
import com.traveling.travel_backend.repository.TripRepository;
import com.traveling.travel_backend.repository.UserRepository;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH)
@CrossOrigin(origins = {AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY})
public class TripController {
    
    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TripItemRepository tripItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    private static final Logger logger = LoggerFactory.getLogger(TripController.class);

    @GetMapping(AppConstants.TRIPS_ENDPOINT + "/draft/me")
    public ResponseEntity<TripDraftResponse> getMyDraft(Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        logger.info(AppConstants.PREFIX_USER + " [{}] {}", AppConstants.LOG_TRIPS, AppConstants.TRIP_DRAFT_LOADED.replace("{}", String.valueOf(user.getId())));

        Trip trip = tripRepository
            .findFirstByUserIdAndStateTrueOrderByIdDesc(user.getId())
            .orElse(null);

        TripDraftResponse response = new TripDraftResponse();
        response.setUserId(user.getId());

        if (trip == null) {
            logger.warn(AppConstants.PREFIX_ERROR + " [{}] {}", AppConstants.LOG_TRIPS, AppConstants.TRIP_DRAFT_NOT_FOUND.replace("{}", String.valueOf(user.getId())));
            response.setName("Mi itinerario");
            response.setPlaces(new ArrayList<>());
            return ResponseEntity.ok(response);
        }

        List<TripItem> items = tripItemRepository.findByTripIdAndStateTrueOrderByVisitOrderAsc(trip.getId());
        logger.info(AppConstants.PREFIX_PLACE + " [{}] {}", AppConstants.LOG_TRIPS, AppConstants.TRIP_PLACES_COUNT.replace("{}", String.valueOf(items.size())));

        response.setTripId(trip.getId());
        response.setUserId(trip.getUser().getId());
        response.setName(trip.getName());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());

        List<Place> places = new ArrayList<>();
        for (TripItem item : items) {
            places.add(item.getPlace());
        }
        response.setPlaces(places);

        return ResponseEntity.ok(response);
    }

    @PutMapping(AppConstants.TRIPS_ENDPOINT + "/draft")
    @Transactional
    public ResponseEntity<TripDraftResponse> saveDraft(@RequestBody TripDraftRequest request, Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        logger.info(AppConstants.PREFIX_USER + " [{}] {}", AppConstants.LOG_TRIPS, AppConstants.TRIP_DRAFT_SAVED.replace("{}", String.valueOf(user.getId())));

        Optional<Trip> existingTrip = tripRepository.findFirstByUserIdAndStateTrueOrderByIdDesc(user.getId());
        Trip trip = existingTrip.orElseGet(Trip::new);

        trip.setUser(user);
        trip.setName((request.getName() == null || request.getName().trim().isEmpty())
            ? "Mi itinerario"
            : request.getName().trim());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setState(true);

        Trip savedTrip = tripRepository.save(trip);

        List<Trip> activeTrips = tripRepository.findByUserIdAndStateTrue(user.getId());
        for (Trip activeTrip : activeTrips) {
            if (activeTrip.getId() != savedTrip.getId()) {
                activeTrip.setState(false);
                tripRepository.save(activeTrip);
            }
        }

        tripItemRepository.deleteByTripId(savedTrip.getId());

        List<Long> placeIds = request.getPlaceIds() == null ? new ArrayList<>() : request.getPlaceIds();

        List<Place> selectedPlaces = new ArrayList<>();
        int order = 1;
        for (Long placeId : placeIds) {
            Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar no encontrado: " + placeId));

            TripItem item = new TripItem();
            item.setTrip(savedTrip);
            item.setPlace(place);
            item.setVisitOrder(order++);
            item.setState(true);
            tripItemRepository.save(item);

            selectedPlaces.add(place);
        }

        TripDraftResponse response = new TripDraftResponse();
        response.setTripId(savedTrip.getId());
        response.setUserId(savedTrip.getUser().getId());
        response.setName(savedTrip.getName());
        response.setStartDate(savedTrip.getStartDate());
        response.setEndDate(savedTrip.getEndDate());
        response.setPlaces(selectedPlaces);

        logger.info(AppConstants.PREFIX_PLACE + " [{}] Borrador id: {} con {} lugares", AppConstants.LOG_TRIPS, savedTrip.getId(), selectedPlaces.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping(AppConstants.TRIPS_ENDPOINT + "/draft/user/{userId}")
    public ResponseEntity<TripDraftResponse> getDraftByUser(@PathVariable Long userId) {
        Trip trip = tripRepository
            .findFirstByUserIdAndStateTrueOrderByIdDesc(userId)
            .orElse(null);

        if (trip == null) {
            return ResponseEntity.noContent().build();
        }

        List<TripItem> items = tripItemRepository.findByTripIdAndStateTrueOrderByVisitOrderAsc(trip.getId());

        TripDraftResponse response = new TripDraftResponse();
        response.setTripId(trip.getId());
        response.setUserId(trip.getUser().getId());
        response.setName(trip.getName());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());

        List<Place> places = new ArrayList<>();
        for (TripItem item : items) {
            places.add(item.getPlace());
        }
        response.setPlaces(places);

        return ResponseEntity.ok(response);
    }

    @PostMapping(AppConstants.TRIPS_ENDPOINT + "/trip")
    @Transactional
    public ResponseEntity<TripDraftResponse> createTrip(@RequestBody TripDraftRequest request, Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        logger.info(AppConstants.PREFIX_USER + " [{}] Iniciando creación de itinerario para usuario: {}", AppConstants.LOG_TRIPS, user.getId());

        // Desactivar borradores anteriores
        List<Trip> activeTrips = tripRepository.findByUserIdAndStateTrue(user.getId());
        for (Trip activeTrip : activeTrips) {
            activeTrip.setState(false);
            tripRepository.save(activeTrip);
        }

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setName((request.getName() == null || request.getName().trim().isEmpty())
            ? "Mi itinerario"
            : request.getName().trim());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setState(true);

        Trip savedTrip = tripRepository.save(trip);

        List<Long> placeIds = request.getPlaceIds() == null ? new ArrayList<>() : request.getPlaceIds();
        List<Place> selectedPlaces = new ArrayList<>();
        int order = 1;
        for (Long placeId : placeIds) {
            Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar no encontrado: " + placeId));

            TripItem item = new TripItem();
            item.setTrip(savedTrip);
            item.setPlace(place);
            item.setVisitOrder(order++);
            item.setState(true);
            tripItemRepository.save(item);
            selectedPlaces.add(place);
        }

        TripDraftResponse response = new TripDraftResponse();
        response.setTripId(savedTrip.getId());
        response.setUserId(savedTrip.getUser().getId());
        response.setName(savedTrip.getName());
        response.setStartDate(savedTrip.getStartDate());
        response.setEndDate(savedTrip.getEndDate());
        response.setPlaces(selectedPlaces);

        logger.info(AppConstants.PREFIX_PLACE + " [{}] {}", AppConstants.LOG_TRIPS, AppConstants.TRIP_CREATED
        .replace("{}", String.valueOf(savedTrip.getId()))
        .replaceFirst("\\{\\}", String.valueOf(user.getId())));

        return ResponseEntity.ok(response);
    }

    private User resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            logger.error(AppConstants.PREFIX_ERROR + " [{}] Intento de acceso sin autenticación", AppConstants.LOG_TRIPS);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado.");
        }

        return userRepository.findByUserName(authentication.getName())
            .orElseThrow(() -> {
                logger.error(AppConstants.PREFIX_ERROR + " [{}] Usuario autenticado no encontrado: {}", AppConstants.LOG_TRIPS, authentication.getName());
                return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario autenticado no válido.");
            });
    }
}
