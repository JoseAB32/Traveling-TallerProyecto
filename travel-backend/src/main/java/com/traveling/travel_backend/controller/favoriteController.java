package com.traveling.travel_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.traveling.travel_backend.model.Favorite;
import com.traveling.travel_backend.repository.FavoriteRepository;

@RestController
@RequestMapping("api/favorites")
@CrossOrigin(origins = "http://localhost:4200")
public class favoriteController {

    @Autowired
    private FavoriteRepository favoriteRepository;
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favorite>> getUserFavorites(@PathVariable Long userId) {
        
        List<Favorite> favorites = favoriteRepository.findByUserIdAndStateTrue(userId);
        
        if(favorites.isEmpty()) {
            return ResponseEntity.noContent().build(); // Devuelve 204 si no tiene favoritos
        }
        
        return ResponseEntity.ok(favorites); // Devuelve 200 y la lista en JSON
    }
}
