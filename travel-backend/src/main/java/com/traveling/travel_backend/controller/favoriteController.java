package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.FavoriteResponseDTO;
import com.traveling.travel_backend.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + AppConstants.FAVORITES_ENDPOINT)
@CrossOrigin(origins = {AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY})
@Tag(name = "Favorite", description = "Gestion de favoritos por usuario")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Operation(summary = "Add a place to user's favorite list", description = "Creates or reactivates a favorite entry for a specific user and place", operationId = "addFavorite")
    @PostMapping("/user/{userId}/place/{placeId}")
    public ResponseEntity<FavoriteResponseDTO> addFavorite(@PathVariable Long userId, @PathVariable Long placeId, Authentication authentication) {
        return ResponseEntity.ok(favoriteService.addFavorite(authentication, placeId));
    }

    @Operation(summary = "Get a user's favorite list", description = "Returns all active favorite places for a specific user", operationId = "getUserFavorites")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoriteResponseDTO>> getUserFavorites(@PathVariable Long userId, Authentication authentication) {
        List<FavoriteResponseDTO> favorites = favoriteService.getUserFavorites(authentication);
        if (favorites.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(favorites);
    }

    @Operation(summary = "Delete a place from user's favorite list", description = "Deletes a favorite entry for a specific user and place", operationId = "removeFavorite")
    @DeleteMapping("/user/{userId}/place/{placeId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long userId, @PathVariable Long placeId, Authentication authentication) {
        favoriteService.removeFavorite(authentication, placeId);
        return ResponseEntity.ok().build();
    }
}