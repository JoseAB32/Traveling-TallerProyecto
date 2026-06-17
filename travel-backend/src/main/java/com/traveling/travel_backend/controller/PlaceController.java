package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.CreatePlaceRequestDTO;
import com.traveling.travel_backend.dto.PlaceResponseDTO;
import com.traveling.travel_backend.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + AppConstants.PLACES_ENDPOINT)
@CrossOrigin(origins = AppConstants.CORS_ALL)
@Tag(name = "Places", description = "Gestión de lugares turísticos")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @Operation(
            summary = "Create a tourist place",
            description = "Creates a new tourist place. Requires ADMIN or SUPERADMIN role.",
            operationId = "createPlace"
    )
    @PostMapping
    public ResponseEntity<PlaceResponseDTO> createPlace(@RequestBody CreatePlaceRequestDTO request) {
        PlaceResponseDTO createdPlace = placeService.createPlace(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPlace.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdPlace);
    }

    @Operation(
            summary = "Top 5 rated places",
            description = "Returns the top 5 rated places in descending order",
            operationId = "getAllOrderList"
    )
    @GetMapping(AppConstants.TOP_RATED)
    public ResponseEntity<List<PlaceResponseDTO>> getTopRated(
            @RequestHeader(value = AppConstants.HEADER_LANGUAGE, defaultValue = AppConstants.DEFAULT_LANGUAGE) String language) {
        return ResponseEntity.ok(placeService.getTopRated(language));
    }

    @Operation(
            summary = "Search a place",
            description = "Returns places matching name, address or city with lightweight translation",
            operationId = "search"
    )
    @GetMapping(AppConstants.SEARCH)
    public ResponseEntity<List<PlaceResponseDTO>> search(
            @RequestParam String q,
            @RequestHeader(value = AppConstants.HEADER_LANGUAGE, defaultValue = AppConstants.DEFAULT_LANGUAGE) String language) {
        return ResponseEntity.ok(placeService.search(q, language));
    }

    @Operation(
            summary = "Get places search cache",
            description = "Returns a lightweight raw list of active places for frontend autocomplete",
            operationId = "getSearchCache"
    )
    @GetMapping(AppConstants.SEARCH + "/cache")
    public ResponseEntity<List<PlaceResponseDTO>> getSearchCache() {
        return ResponseEntity.ok(placeService.getSearchCache());
    }

    @Operation(
            summary = "Get all places",
            description = "Returns a complete list of places stored in the database",
            operationId = "getAllPlaces"
    )
    @GetMapping
    public ResponseEntity<List<PlaceResponseDTO>> getAllPlaces(
            @RequestHeader(value = AppConstants.HEADER_LANGUAGE, defaultValue = AppConstants.DEFAULT_LANGUAGE) String language) {
        return ResponseEntity.ok(placeService.getAllPlaces(language));
    }

    @Operation(
            summary = "Get place by Id",
            description = "Returns a place by its ID",
            operationId = "getPlaceById"
    )
    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponseDTO> getPlaceById(
            @PathVariable Long id,
            @RequestHeader(value = AppConstants.HEADER_LANGUAGE, defaultValue = AppConstants.DEFAULT_LANGUAGE) String language) {
        return ResponseEntity.ok(placeService.getPlaceById(id, language));
    }

    @Operation(
            summary = "Get all places by department",
            description = "Returns all active places for a given department",
            operationId = "getPlacesByDepartment"
    )
    @GetMapping(AppConstants.PLACES_DEPARTMENT)
    public ResponseEntity<List<PlaceResponseDTO>> getPlacesByDepartment(
            @PathVariable Long cityId,
            @RequestHeader(value = AppConstants.HEADER_LANGUAGE, defaultValue = AppConstants.DEFAULT_LANGUAGE) String language) {
        return ResponseEntity.ok(placeService.getPlacesByDepartment(cityId, language));
    }

    @Operation(
            summary = "Get top rated places by department",
            description = "Returns top 3 rated places for a given department",
            operationId = "getPlacesTopByDepartment"
    )
    @GetMapping(AppConstants.PLACES_DEPARTMENT_TOP)
    public ResponseEntity<List<PlaceResponseDTO>> getTopPlacesByDepartment(
            @PathVariable Long cityId,
            @RequestHeader(value = AppConstants.HEADER_LANGUAGE, defaultValue = AppConstants.DEFAULT_LANGUAGE) String language) {
        return ResponseEntity.ok(placeService.getTopPlacesByDepartment(cityId, language));
    }
}