package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.PlaceResponseDTO;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.PlaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PlaceService {

    private static final Logger logger = LoggerFactory.getLogger(PlaceService.class);

    private final PlaceRepository placeRepository;
    private final LogRepository logRepository;

    public PlaceService(PlaceRepository placeRepository, LogRepository logRepository) {
        this.placeRepository = placeRepository;
        this.logRepository = logRepository;
    }

    public List<PlaceResponseDTO> getTopRated() {
        logger.info("{} [{}] Solicitando top 5 de lugares por rating", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando top 5 de lugares ordenados por rating - GET /api/places/top-rated", null));

        List<Place> places = placeRepository.findTop5ByOrderByRatingDesc();

        logger.debug("{} [{}] Top 5 lugares: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, places);
        return toDTO(places);
    }

    public List<PlaceResponseDTO> search(String q) {
        logger.info("{} [{}] Búsqueda de lugares con criterio: '{}'", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, q);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando búsqueda de lugares con criterio: '" + q + "' - GET /api/places/search", null));

        List<Place> places = placeRepository
                .findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(q, q, q);

        logger.debug("{} [{}] Resultados para '{}': {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, q, places);
        return toDTO(places);
    }

    public List<PlaceResponseDTO> getAllPlaces() {
        logger.info("{} [{}] Solicitando lista completa de lugares", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando lista completa de lugares - GET /api/places", null));

        List<Place> places = placeRepository.findAll();

        logger.debug("{} [{}] Total de lugares: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, places.size());
        return toDTO(places);
    }

    public PlaceResponseDTO getPlaceById(Long id) {
        logger.info("{} [{}] Solicitando lugar con ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, id);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando información del lugar con ID: " + id + " - GET /api/places/" + id, null));

        Place place = placeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("{} [{}] Lugar con ID {} no encontrado", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, id);
                    logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_WARN,
                            "Lugar con ID " + id + " no encontrado - GET /api/places/" + id, null));
                    return new ResourceNotFoundException("Lugar no encontrado con ID: " + id);
                });

        logger.debug("{} [{}] Lugar encontrado: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, place);
        return PlaceResponseDTO.fromEntity(place);
    }

    public List<PlaceResponseDTO> getPlacesByDepartment(Long cityId) {
        logger.info("{} [{}] Solicitando lugares para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, cityId);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando lugares para departamento ID: " + cityId, null));

        List<Place> places = placeRepository.findByCityIdAndStateTrue(cityId);

        if (places.isEmpty()) {
            logger.warn("{} [{}] No se encontraron lugares para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, cityId);
        } else {
            logger.debug("{} [{}] {} lugares encontrados para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, places.size(), cityId);
        }
        return toDTO(places);
    }

    public List<PlaceResponseDTO> getTopPlacesByDepartment(Long cityId) {
        logger.info("{} [{}] Solicitando top 3 lugares para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, cityId);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando lugares mejor calificados para departamento ID: " + cityId, null));

        List<Place> places = placeRepository.findTop3ByCityIdAndStateTrueOrderByRatingDesc(cityId);

        if (places.isEmpty()) {
            logger.warn("{} [{}] No se encontraron lugares para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, cityId);
        } else {
            logger.debug("{} [{}] {} lugares encontrados para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, places.size(), cityId);
        }
        return toDTO(places);
    }


    private List<PlaceResponseDTO> toDTO(List<Place> places) {
        return places.stream()
                .map(PlaceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}