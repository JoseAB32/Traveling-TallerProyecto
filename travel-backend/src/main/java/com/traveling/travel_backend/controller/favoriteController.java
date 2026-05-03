package com.traveling.travel_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.model.Favorite;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.FavoriteRepository;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.PlaceRepository;
import com.traveling.travel_backend.repository.UserRepository;

import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;

import org.slf4j.Logger;           
import org.slf4j.LoggerFactory;    

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + AppConstants.FAVORITES_ENDPOINT)
@CrossOrigin(origins = {AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY})
public class favoriteController {

    public static final Logger log = LoggerFactory.getLogger(favoriteController.class);

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Operation(
        summary = "Add a place to user's favorite list",
        description = "Creates or reactivates a favorite entry for a specific user and place",
        tags = {"Favorite"},
        operationId = "addFavorite"
    )
    @PostMapping("/user/{userId}/place/{placeId}")
    public ResponseEntity<Favorite> addFavorite(@PathVariable Long userId, @PathVariable Long placeId) {
        log.info("➕ [FAVORITOS] Solicitud de agregado -> Usuario: {}, Lugar: {}", userId, placeId);
        logRepository.save(new LogEntity("FAVORITOS", "INFO", "Solicitud de agregado -> Usuario: " + userId + ", Lugar: " + placeId + " - POST /api/favorites/user/" + userId + "/place/" + placeId, userId));

        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Place> placeOpt = placeRepository.findById(placeId);

        if (userOpt.isEmpty() || placeOpt.isEmpty()) {
            log.warn("⚠️ [FAVORITOS] Usuario o lugar no encontrado. Usuario: {}, Lugar: {}", userId, placeId);
            return ResponseEntity.notFound().build();
        }

        Optional<Favorite> existingFavorite = favoriteRepository.findByUserIdAndPlaceId(userId, placeId);

        if (existingFavorite.isPresent()) {
            Favorite favorite = existingFavorite.get();
            favorite.setState(true);
            Favorite savedFavorite = favoriteRepository.save(favorite);
            return ResponseEntity.ok(savedFavorite);
        }

        Favorite favorite = new Favorite(userOpt.get(), placeOpt.get());
        favorite.setState(true);
        Favorite savedFavorite = favoriteRepository.save(favorite);
        return ResponseEntity.ok(savedFavorite);
    }

    @Operation(
        summary = "Get a user's favorite list",
        description = "Returns a list of all favorite places from a specific user",
        tags = {"Favorite"},
        operationId = "getUserFavorites"
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favorite>> getUserFavorites(@PathVariable Long userId) {
        
        log.info("📂 [FAVORITOS] Consultando lista del usuario ID: {}", userId);
        logRepository.save(new LogEntity("FAVORITOS", "INFO", "Consultando lista del usuario ID: " + userId + " - GET /api/favorites/user/" + userId, userId));

        List<Favorite> favorites = favoriteRepository.findByUserIdAndStateTrue(userId);
        
        if(favorites.isEmpty()) {
            log.info("📂 [FAVORITOS] El usuario ID: {} no tiene favoritos", userId);
            logRepository.save(new LogEntity("FAVORITOS", "INFO", "El usuario ID: " + userId + " no tiene favoritos - GET /api/favorites/user/" + userId, userId));
            return ResponseEntity.noContent().build(); // Devuelve 204 si no tiene favoritos
        }
        
        log.info("📂 [FAVORITOS] Devolviendo lista de favoritos para el usuario ID: {}", userId);
        logRepository.save(new LogEntity("FAVORITOS", "INFO", "Devolviendo lista de favoritos para el usuario ID: " + userId + " - GET /api/favorites/user/" + userId, userId));
        return ResponseEntity.ok(favorites); // Devuelve 200 y la lista en JSON
    }

    @Operation(
        summary = "Delete a place user's favorite list",
        description = "Delete a place favorites table for a specific user",
        tags = {"Favorite"},
        operationId = "removeFavorite"
    )
    @DeleteMapping("/user/{userId}/place/{placeId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long userId, @PathVariable Long placeId) {
        log.info("🗑️ [FAVORITOS] Solicitud de eliminación -> Usuario: {}, Lugar: {}", userId, placeId);
        logRepository.save(new LogEntity("FAVORITOS", "INFO", "Solicitud de eliminación -> Usuario: " + userId + ", Lugar: " + placeId + " - DELETE /api/favorites/user/" + userId + "/place/" + placeId, userId));

        try {
            favoriteRepository.deleteByUserIdAndPlaceId(userId, placeId);
            log.info("🆗 [FAVORITOS] Eliminado correctamente.");
            logRepository.save(new LogEntity("FAVORITOS", "INFO", "Eliminado correctamente -> Usuario: " + userId + ", Lugar: " + placeId + " - DELETE /api/favorites/user/" + userId + "/place/" + placeId, userId));
            return ResponseEntity.ok().build(); 
        } catch (Exception e) {
            // Es vital registrar el error completo si algo falla en el borrado
            log.error("❌ [FAVORITOS] Falló la eliminación: {}", e.getMessage());
            logRepository.save(new LogEntity("FAVORITOS", "ERROR", "Falló la eliminación -> Usuario: " + userId + ", Lugar: " + placeId + " - DELETE /api/favorites/user/" + userId + "/place/" + placeId, userId));
            return ResponseEntity.internalServerError().build();
        }
    }    
}
