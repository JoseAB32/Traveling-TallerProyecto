package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.TripDraftRequest;
import com.traveling.travel_backend.dto.TripDraftResponse;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.exception.UnauthorizedException;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.model.Trip;
import com.traveling.travel_backend.model.TripItem;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.PlaceRepository;
import com.traveling.travel_backend.repository.TripItemRepository;
import com.traveling.travel_backend.repository.TripRepository;
import com.traveling.travel_backend.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TripService {

    private static final Logger logger =
            LoggerFactory.getLogger(TripService.class);

    private final TripRepository tripRepository;
    private final TripItemRepository tripItemRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    // NUEVO
    private final SupabaseReminderService supabaseReminderService;

    public TripService(
            TripRepository tripRepository,
            TripItemRepository tripItemRepository,
            UserRepository userRepository,
            PlaceRepository placeRepository,
            SupabaseReminderService supabaseReminderService) {

        this.tripRepository = tripRepository;
        this.tripItemRepository = tripItemRepository;
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;

        // NUEVO
        this.supabaseReminderService =
                supabaseReminderService;
    }

    @Transactional(readOnly = true)
    public TripDraftResponse getMyDraft(Authentication authentication) {

        User user = resolveAuthenticatedUser(authentication);

        logger.info("{} [{}] Cargando borrador para usuario ID: {}",
                AppConstants.PREFIX_USER,
                AppConstants.LOG_TRIPS,
                user.getId());

        Optional<Trip> tripOpt =
                tripRepository
                        .findFirstByUserIdAndStateTrueOrderByIdDesc(
                                user.getId());

        TripDraftResponse response = new TripDraftResponse();
        response.setUserId(user.getId());

        if (tripOpt.isEmpty()) {

            logger.warn("{} [{}] No se encontro borrador para usuario ID: {}",
                    AppConstants.PREFIX_ERROR,
                    AppConstants.LOG_TRIPS,
                    user.getId());

            response.setName("Mi itinerario");
            response.setPlaces(new ArrayList<>());

            return response;
        }

        Trip trip = tripOpt.get();

        List<TripItem> items =
                tripItemRepository
                        .findByTripIdAndStateTrueOrderByVisitOrderAsc(
                                trip.getId());

        return buildResponse(trip, items);
    }

    @Transactional(readOnly = true)
    public List<TripDraftResponse> getMyTrips(
            Authentication authentication) {

        User user = resolveAuthenticatedUser(authentication);

        logger.info("{} [{}] Cargando itinerarios del usuario ID: {}",
                AppConstants.PREFIX_USER,
                AppConstants.LOG_TRIPS,
                user.getId());

        List<Trip> trips =
                tripRepository
                        .findByUserIdAndStateTrueOrderByStartDateAsc(
                                user.getId());

        List<TripDraftResponse> response = new ArrayList<>();

        for (Trip trip : trips) {

            List<TripItem> items =
                    tripItemRepository
                            .findByTripIdAndStateTrueOrderByVisitOrderAsc(
                                    trip.getId());

            response.add(buildResponse(trip, items));
        }

        return response;
    }

    @Transactional
    public TripDraftResponse saveDraft(
            TripDraftRequest request,
            Authentication authentication) {

        User user = resolveAuthenticatedUser(authentication);

        logger.info("{} [{}] Guardando borrador para usuario ID: {}",
                AppConstants.PREFIX_USER,
                AppConstants.LOG_TRIPS,
                user.getId());

        Trip trip = tripRepository
                .findFirstByUserIdAndStateTrueOrderByIdDesc(user.getId())
                .orElseGet(Trip::new);

        populateTrip(trip, user, request);

        Trip savedTrip = tripRepository.save(trip);

        tripItemRepository.deleteByTripId(savedTrip.getId());

        List<Place> selectedPlaces =
                buildAndSaveTripItems(
                        savedTrip,
                        request.getPlaceIds());

        return buildResponseFromPlaces(savedTrip, selectedPlaces);
    }

    @Transactional
    public TripDraftResponse createTrip(
            TripDraftRequest request,
            Authentication authentication) {

        User user = resolveAuthenticatedUser(authentication);

        logger.info("{} [{}] Creando itinerario para usuario ID: {}",
                AppConstants.PREFIX_USER,
                AppConstants.LOG_TRIPS,
                user.getId());

        Trip trip = new Trip();

        populateTrip(trip, user, request);

        Trip savedTrip = tripRepository.save(trip);

        // NUEVO → CREA RECORDATORIO EN SUPABASE
        supabaseReminderService.createReminder(
                savedTrip.getUser().getCorreo(),
                savedTrip.getUser().getUserName(),
                savedTrip.getName(),
                savedTrip.getStartDate(),
                savedTrip.getId()
        );

        List<Place> selectedPlaces =
                buildAndSaveTripItems(
                        savedTrip,
                        request.getPlaceIds());

        return buildResponseFromPlaces(savedTrip, selectedPlaces);
    }

    private User resolveAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            throw new UnauthorizedException("No autenticado.");
        }

        return userRepository
                .findByUserName(authentication.getName())
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Usuario autenticado no valido."));
    }

    private void populateTrip(
            Trip trip,
            User user,
            TripDraftRequest request) {

        trip.setUser(user);

        trip.setName(
                request.getName() == null
                        || request.getName().trim().isEmpty()
                        ? "Mi itinerario"
                        : request.getName().trim());

        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setState(true);
    }

    private List<Place> buildAndSaveTripItems(
            Trip savedTrip,
            List<Long> placeIds) {

        List<Long> ids =
                placeIds == null
                        ? new ArrayList<>()
                        : placeIds;

        List<Place> selectedPlaces = new ArrayList<>();

        int order = 1;

        for (Long placeId : ids) {

            Place place = placeRepository.findById(placeId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Lugar no encontrado con ID: "
                                            + placeId));

            TripItem item =
                    new TripItem(savedTrip, place, order++);

            item.setState(true);

            tripItemRepository.save(item);

            selectedPlaces.add(place);
        }

        return selectedPlaces;
    }

    private TripDraftResponse buildResponse(
            Trip trip,
            List<TripItem> items) {

        List<Place> places = new ArrayList<>();

        for (TripItem item : items) {
            places.add(item.getPlace());
        }

        return buildResponseFromPlaces(trip, places);
    }

    private TripDraftResponse buildResponseFromPlaces(
            Trip trip,
            List<Place> places) {

        TripDraftResponse response =
                new TripDraftResponse();

        response.setTripId(trip.getId());
        response.setUserId(trip.getUser().getId());
        response.setName(trip.getName());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());
        response.setPlaces(places);

        return response;
    }

    @Transactional(readOnly = true)
    public TripDraftResponse getTripById(
            Long tripId,
            Authentication authentication) {

        User user = resolveAuthenticatedUser(authentication);

        logger.info("{} [{}] Buscando itinerario ID: {} para usuario ID: {}",
                AppConstants.PREFIX_USER,
                AppConstants.LOG_TRIPS,
                tripId,
                user.getId());

        Trip trip = tripRepository
                .findByIdAndUserIdAndStateTrue(
                        tripId,
                        user.getId())
                .orElseThrow(() -> {

                    logger.warn("{} [{}] Itinerario ID: {} no encontrado o inactivo para usuario ID: {}",
                            AppConstants.PREFIX_ERROR,
                            AppConstants.LOG_TRIPS,
                            tripId,
                            user.getId());

                    return new ResourceNotFoundException(
                            "Itinerario no encontrado con ID: "
                                    + tripId);
                });

        List<TripItem> items =
                tripItemRepository
                        .findByTripIdAndStateTrueOrderByVisitOrderAsc(
                                trip.getId());

        logger.info("{} [{}] Itinerario ID: {} encontrado con {} lugares",
                AppConstants.PREFIX_PLACE,
                AppConstants.LOG_TRIPS,
                trip.getId(),
                items.size());

        return buildResponse(trip, items);
    }

    @Transactional
    public TripDraftResponse updateTripById(
            Long tripId,
            TripDraftRequest request,
            Authentication authentication) {

        User user = resolveAuthenticatedUser(authentication);

        logger.info("{} [{}] Actualizando itinerario ID: {} para usuario ID: {}",
                AppConstants.PREFIX_USER,
                AppConstants.LOG_TRIPS,
                tripId,
                user.getId());

        Trip trip = tripRepository
                .findByIdAndUserIdAndStateTrue(
                        tripId,
                        user.getId())
                .orElseThrow(() -> {

                    logger.warn("{} [{}] Itinerario ID: {} no encontrado o inactivo para usuario ID: {}",
                            AppConstants.PREFIX_ERROR,
                            AppConstants.LOG_TRIPS,
                            tripId,
                            user.getId());

                    return new ResourceNotFoundException(
                            "Itinerario no encontrado con ID: "
                                    + tripId);
                });

        populateTrip(trip, user, request);

        Trip savedTrip = tripRepository.save(trip);

        tripItemRepository.deleteByTripId(savedTrip.getId());

        List<Place> selectedPlaces =
                buildAndSaveTripItems(
                        savedTrip,
                        request.getPlaceIds());

        logger.info("{} [{}] Itinerario ID: {} actualizado con {} lugares",
                AppConstants.PREFIX_PLACE,
                AppConstants.LOG_TRIPS,
                savedTrip.getId(),
                selectedPlaces.size());

        return buildResponseFromPlaces(savedTrip, selectedPlaces);
    }
}