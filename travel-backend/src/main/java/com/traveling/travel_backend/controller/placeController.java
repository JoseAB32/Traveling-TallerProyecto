package com.traveling.travel_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.repository.PlaceRepository;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = "http://localhost:4200")
public class placeController {

    @Autowired
    private PlaceRepository placeRepository;

    @GetMapping
    public List<Place> getAllPlaces() {
        return placeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Place> getPlaceById(@PathVariable Long id) {
        return placeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}