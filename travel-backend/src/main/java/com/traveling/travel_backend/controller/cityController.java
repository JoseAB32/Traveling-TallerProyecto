package com.traveling.travel_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.repository.CityRepository;

@RestController
@RequestMapping("api/")
@CrossOrigin(origins = "http://localhost:4200")
public class cityController {
    @Autowired
    private CityRepository cityRepository;

    @GetMapping("/cities")
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
}
