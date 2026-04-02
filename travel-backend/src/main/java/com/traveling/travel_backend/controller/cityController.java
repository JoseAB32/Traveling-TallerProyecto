package com.traveling.travel_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.repository.CityRepository;

// IMPORTS DE LOGGING
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("api/")
@CrossOrigin(origins = "http://localhost:4200")
public class cityController {

    // LOGGER
    private static final Logger logger = LoggerFactory.getLogger(cityController.class);

    @Autowired
    private CityRepository cityRepository;

    @GetMapping("/cities")
    public List<City> getAllCities() {

        logger.info("🏙️ [CIUDADES] Petición recibida: GET /api/cities");

        List<City> cities = cityRepository.findAll();

        logger.debug("🏙️ [CIUDADES] Número de ciudades encontradas: {}", cities.size());

        logger.info("🏙️ [CIUDADES] Ciudades encontradas: {} y devueltas correctamente.", cities.size());

        return cities;
    }
}