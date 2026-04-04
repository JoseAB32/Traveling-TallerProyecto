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


@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = "*")
public class placeController {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired 
    private LogRepository logRepository;

    private static final Logger logger = LoggerFactory.getLogger(placeController.class);

   @GetMapping("/top-rated")
    public List<Place> getAllOrderList() {
        logger.info("📍 [PLACES] Solicitando top 5 de lugares ordenados por rating - GET /api/places/top-rated");
        logRepository.save(new LogEntity("PLACES", "INFO", "Solicitando top 5 de lugares ordenados por rating - GET /api/places/top-rated", null));

        List<Place> topPlaces = placeRepository.findTop5ByOrderByRatingDesc();

        logger.debug("📍 [PLACES] Top 5 de lugares ordenados: {}", topPlaces);

        return topPlaces;
    }   

    @GetMapping("/search")
    public List<Place> search(@RequestParam String q) {
        logger.info("📍 [PLACES] Solicitando búsqueda de lugares con criterio: '{}' - GET /api/places/search", q);
        logRepository.save(new LogEntity("PLACES", "INFO", "Solicitando búsqueda de lugares con criterio: '" + q + "' - GET /api/places/search", null));

        List<Place> resultPlaces = placeRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(q, q, q);

        logger.debug("📍 [PLACES] Resultados de búsqueda para criterio '{}': {}", q, resultPlaces);

        return resultPlaces;
    }

    @GetMapping
    public List<Place> getAllPlaces() {
        logger.info("📍 [PLACES] Solicitando lista completa de lugares - GET /api/places");
        logRepository.save(new LogEntity("PLACES", "INFO", "Solicitando lista completa de lugares - GET /api/places", null));

        List<Place> allPlaces = placeRepository.findAll();

        logger.debug("📍 [PLACES] Lista completa de lugares: {}", allPlaces);

        return allPlaces;
    }

    @GetMapping("/{id}")
    public Place getPlaceById(@PathVariable Long id) {
        logger.info("📍 [PLACES] Solicitando información del lugar con ID: {} - GET /api/places/{}", id, id);
        logRepository.save(new LogEntity("PLACES", "INFO", "Solicitando información del lugar con ID: " + id + " - GET /api/places/" + id, null));

        Optional<Place> place = placeRepository.findById(id);

        if(place.isPresent()) {
            logger.debug("📍 [PLACES] Información del lugar con ID {}: {}", id, place.get());
        } else {
            logger.warn("📍 [PLACES] Lugar con ID {} no encontrado", id);
            logRepository.save(new LogEntity("PLACES", "WARN", "Lugar con ID " + id + " no encontrado - GET /api/places/" + id, null));
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