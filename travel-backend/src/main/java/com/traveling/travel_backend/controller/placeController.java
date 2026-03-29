package com.traveling.travel_backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.repository.PlaceRepository;

@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = "*")
public class placeController {

    @Autowired
    private PlaceRepository placeRepository;

    // 🔥 LISTAR TODOS
    @GetMapping
    public List<Place> getAllPlaces() {
        return placeRepository.findAll();
    }

    // 🔥 OBTENER POR ID
    @GetMapping("/{id}")
    public Place getPlaceById(@PathVariable Long id) {
        Optional<Place> place = placeRepository.findById(id);
        return place.orElse(null);
    }
}