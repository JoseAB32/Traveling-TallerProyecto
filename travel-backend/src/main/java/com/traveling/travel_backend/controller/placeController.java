package com.traveling.travel_backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.repository.PlaceRepository;
import com.traveling.travel_backend.security.JwtService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = "*")
public class placeController {

    @Autowired
    private PlaceRepository placeRepository;

   @GetMapping("/top-rated")
    public List<Place> getAllOrderList() {
        return placeRepository.findTop5ByOrderByRatingDesc();
    }

    @GetMapping("/search")
    public List<Place> search(@RequestParam String q) {
        return placeRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(q, q, q);
    }


    

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