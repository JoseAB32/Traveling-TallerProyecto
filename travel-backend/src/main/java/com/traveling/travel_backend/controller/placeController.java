package com.traveling.travel_backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.PlaceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.traveling.travel_backend.constants.AppConstants;


@RestController
@RequestMapping(AppConstants.API_BASE_PATH + AppConstants.PLACES_ENDPOINT)
@CrossOrigin(origins = AppConstants.CORS_ALL)
public class placeController {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired 
    private LogRepository logRepository;

    private static final Logger logger = LoggerFactory.getLogger(placeController.class);

   @GetMapping(AppConstants.TOP_RATED)
    public List<Place> getAllOrderList() {
        logger.info(AppConstants.PREFIX_PLACE + " [" + AppConstants.LOG_PLACES + "] Solicitando top 5 de lugares ordenados por rating - GET /api/places/top-rated");
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO, "Solicitando top 5 de lugares ordenados por rating - GET /api/places/top-rated", null));

        List<Place> topPlaces = placeRepository.findTop5ByOrderByRatingDesc();

        logger.debug(AppConstants.PREFIX_PLACE + " [" + AppConstants.LOG_PLACES + "] Top 5 de lugares ordenados: {}", topPlaces);

        return topPlaces;
    }   

    @GetMapping(AppConstants.SEARCH)
    public List<Place> search(@RequestParam String q) {
        logger.info(AppConstants.PREFIX_PLACE + " [" + AppConstants.LOG_PLACES + "] Solicitando búsqueda de lugares con criterio: '{}' - GET /api/places/search", q);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO, "Solicitando búsqueda de lugares con criterio: '" + q + "' - GET /api/places/search", null));

        List<Place> resultPlaces = placeRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(q, q, q);

        logger.debug(AppConstants.PREFIX_PLACE + " [" + AppConstants.LOG_PLACES + "] Resultados de búsqueda para criterio '{}': {}", q, resultPlaces);

        return resultPlaces;
    }

    @GetMapping
    public List<Place> getAllPlaces() {
        logger.info(AppConstants.PREFIX_PLACE + " [" + AppConstants.LOG_PLACES + "] Solicitando lista completa de lugares - GET /api/places");
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO, "Solicitando lista completa de lugares - GET /api/places", null));

        List<Place> allPlaces = placeRepository.findAll();

        logger.debug(AppConstants.PREFIX_PLACE + " [" + AppConstants.LOG_PLACES + "] Lista completa de lugares: {}", allPlaces);

        return allPlaces;
    }

    @GetMapping("/{id}")
    public Place getPlaceById(@PathVariable Long id) {
        logger.info(AppConstants.PREFIX_PLACE + " [" + AppConstants.LOG_PLACES + "] Solicitando información del lugar con ID: {} - GET /api/places/{}", id, id);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO, "Solicitando información del lugar con ID: " + id + " - GET /api/places/" + id, null));

        Optional<Place> place = placeRepository.findById(id);

        if(place.isPresent()) {
            logger.debug(AppConstants.PREFIX_PLACE + " [" + AppConstants.LOG_PLACES + "] Información del lugar con ID {}: {}", id, place.get());
        } else {
            logger.warn(AppConstants.PREFIX_PLACE + " [" + AppConstants.LOG_PLACES + "] Lugar con ID {} no encontrado", id);
            logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_WARN, "Lugar con ID " + id + " no encontrado - GET /api/places/" + id, null));
        }

        return place.orElse(null);
    }

    @GetMapping("/department/{cityId}")
    public List<Place> getPlacesByDepartment(@PathVariable Long cityId) {
        logger.info("📍 [PLACES] Solicitando lugares para el departamento (ID): {} - GET /api/places/department/{}", cityId, cityId);
        logRepository.save(new LogEntity("PLACES", "INFO", "Solicitando lugares para departamento ID: " + cityId, null));

        List<Place> places = placeRepository.findByCityIdAndStateTrue(cityId);

        if (places.isEmpty()) {
            logger.warn("📍 [PLACES] No se encontraron lugares para el departamento ID: {}", cityId);
        } else {
            logger.debug("📍 [PLACES] Se encontraron {} lugares para el departamento ID: {}", places.size(), cityId);
        }

        return places;
    }
}