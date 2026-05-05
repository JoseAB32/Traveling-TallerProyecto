package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.TripDraftRequest;
import com.traveling.travel_backend.dto.TripDraftResponse;
import com.traveling.travel_backend.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH)
@CrossOrigin(origins = {AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY})
@Tag(name = "Trips", description = "Gestion de itinerarios de viaje")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @Operation(summary = "Recover itinerary draft", description = "Restores the authenticated user's itinerary draft", operationId = "getMyDraft")
    @GetMapping(AppConstants.TRIPS_ENDPOINT + "/draft/me")
    public ResponseEntity<TripDraftResponse> getMyDraft(Authentication authentication) {
        return ResponseEntity.ok(tripService.getMyDraft(authentication));
    }

    @Operation(summary = "Save itinerary draft", description = "Saves the authenticated user's itinerary draft", operationId = "saveDraft")
    @PutMapping(AppConstants.TRIPS_ENDPOINT + "/draft")
    public ResponseEntity<TripDraftResponse> saveDraft(@RequestBody TripDraftRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.ok(tripService.saveDraft(request, authentication));
    }

    @Operation(summary = "Save a confirmed itinerary", description = "Creates and saves a confirmed user itinerary", operationId = "createTrip")
    @PostMapping(AppConstants.TRIPS_ENDPOINT + "/trip")
    public ResponseEntity<TripDraftResponse> createTrip(@RequestBody TripDraftRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.ok(tripService.createTrip(request, authentication));
    }
}