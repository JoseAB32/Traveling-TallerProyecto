package com.traveling.travel_backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.ErrorResponse;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.repository.CityRepository;
import com.traveling.travel_backend.repository.LogRepository;

import io.swagger.v3.oas.annotations.Operation;

// IMPORTS DE LOGGING
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH)
@CrossOrigin(origins = {AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY})
public class cityController {

    // LOGGER
    private static final Logger logger = LoggerFactory.getLogger(cityController.class);

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private LogRepository logRepository; 

    @Operation(
        summary = "Get all cities/deparmetents",
        description = "Returns a list of all cities/departments in the database",
        tags = {"Cities"},
        operationId = "getAllCities"
    )
    @GetMapping(AppConstants.CITIES_ENDPOINT)
    public List<City> getAllCities() {

        logger.info(AppConstants.PREFIX_CITY + " [" + AppConstants.LOG_CITIES + "] Petición recibida: GET /api/cities");
        logRepository.save(new LogEntity(AppConstants.LOG_CITIES, AppConstants.LOG_INFO, "Petición recibida: GET /api/cities", null));

        List<City> cities = cityRepository.findAll();

        logger.debug(AppConstants.PREFIX_CITY + " [" + AppConstants.LOG_CITIES + "] Número de ciudades encontradas: {}", cities.size());

        logger.info(AppConstants.PREFIX_CITY + " [" + AppConstants.LOG_CITIES + "] Ciudades encontradas: {} y devueltas correctamente.", cities.size());
        logRepository.save(new LogEntity(AppConstants.LOG_CITIES, AppConstants.LOG_INFO, "Ciudades encontradas: " + cities.size() + " y devueltas correctamente.", null));

        return cities;
    }

    @GetMapping(AppConstants.CITIES_ENDPOINT + "/{id}")   
    public ResponseEntity<?> getCityById(@PathVariable Long id) {
        logger.info(AppConstants.PREFIX_CITY + " [" + AppConstants.LOG_CITIES + "] Solicitando información de la ciudad con ID: {} - GET /api/cities/{}", id, id);
        logRepository.save(new LogEntity(AppConstants.LOG_CITIES, AppConstants.LOG_INFO, "Solicitando información de la ciudad con ID: " + id + " - GET /api/cities/" + id, null));

        Optional<City> city = cityRepository.findById(id);

        if(city.isPresent()) {
            logger.debug(AppConstants.PREFIX_CITY + " [" + AppConstants.LOG_CITIES + "] Información de la ciudad con ID {}: {}", id, city.get());

            return ResponseEntity.ok(city.get());
        } else {
            logger.warn(AppConstants.PREFIX_CITY + " [" + AppConstants.LOG_CITIES + "] Ciudad con ID {} no encontrado", id);
            logRepository.save(new LogEntity(AppConstants.LOG_CITIES, AppConstants.LOG_WARN, "Ciudad con ID " + id + " no encontrado - GET /api/cities/" + id, null));

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Ciudad no encontrada"));
        }
    }   
}