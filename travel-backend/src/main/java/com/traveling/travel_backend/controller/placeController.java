package com.traveling.travel_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.repository.PlaceRepository;
import com.traveling.travel_backend.security.JwtService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("api/")
@CrossOrigin(origins = "http://localhost:4200")
public class placeController {
    
    @Autowired
    private PlaceRepository placeRepository;

   @GetMapping("/places")
    public List<Place> getAll() {
        return placeRepository.findTop5ByOrderByRatingDesc();
    }
    
}
