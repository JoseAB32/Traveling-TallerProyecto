package com.traveling.travel_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.traveling.travel_backend.model.Favorite;
import com.traveling.travel_backend.repository.FavoriteRepository;

import org.slf4j.Logger;           
import org.slf4j.LoggerFactory;    

@RestController
@RequestMapping("api/favorites")
@CrossOrigin(origins = "http://localhost:4200")
public class favoriteController {

    public static final Logger log = LoggerFactory.getLogger(favoriteController.class);

    @Autowired
    private FavoriteRepository favoriteRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favorite>> getUserFavorites(@PathVariable Long userId) {
        
        log.info("📂 [FAVORITOS] Consultando lista del usuario ID: {}", userId);

        List<Favorite> favorites = favoriteRepository.findByUserIdAndStateTrue(userId);
        
        if(favorites.isEmpty()) {
            log.info("📂 [FAVORITOS] El usuario ID: {} no tiene favoritos", userId);
            return ResponseEntity.noContent().build(); // Devuelve 204 si no tiene favoritos
        }
        
        log.info("📂 [FAVORITOS] Devolviendo lista de favoritos para el usuario ID: {}", userId);
        return ResponseEntity.ok(favorites); // Devuelve 200 y la lista en JSON
    }

    @DeleteMapping("/user/{userId}/place/{placeId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long userId, @PathVariable Long placeId) {
        log.info("🗑️ [FAVORITOS] Solicitud de eliminación -> Usuario: {}, Lugar: {}", userId, placeId);
        // favoriteRepository.deleteByUserIdAndPlaceId(userId, placeId);
        // return ResponseEntity.ok().build(); // Devuel 200 OK cuando se elimina

        
        try {
            favoriteRepository.deleteByUserIdAndPlaceId(userId, placeId);
            log.info("🆗 [FAVORITOS] Eliminado correctamente.");
            return ResponseEntity.ok().build(); 
        } catch (Exception e) {
            // Es vital registrar el error completo si algo falla en el borrado
            log.error("❌ [FAVORITOS] Falló la eliminación: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }    
}
