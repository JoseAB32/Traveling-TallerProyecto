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
public class PlaceService {

    private static final Logger logger = LoggerFactory.getLogger(PlaceService.class);

    private final PlaceRepository placeRepository;
    private final LogRepository logRepository;
    private final TranslationsService translationsService;

    public PlaceService(
            PlaceRepository placeRepository,
            LogRepository logRepository,
            TranslationsService translationsService) {
        this.placeRepository = placeRepository;
        this.logRepository = logRepository;
        this.translationsService = translationsService;
    }

    @Transactional
    public List<PlaceResponseDTO> getTopRated(String language) {
        logger.info("{} [{}] Solicitando top 5 de lugares por rating", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando top 5 de lugares ordenados por rating - GET /api/places/top-rated", null));

        List<Place> places = placeRepository.findTop5ByOrderByRatingDesc();
        logger.debug("{} [{}] Top 5 lugares encontrados: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, places.size());

        return toTopRatedDTO(places, language);
    }

    @Transactional
    public List<PlaceResponseDTO> search(String q, String language) {
        logger.info("{} [{}] Busqueda de lugares con criterio: '{}'", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, q);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando busqueda de lugares con criterio: '" + q + "' - GET /api/places/search", null));

        List<Place> places = placeRepository
                .findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(q, q, q);

        logger.debug("{} [{}] Resultados para '{}': {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, q, places.size());

        return toSearchDTO(places, language);
    }

    @Transactional(readOnly = true)
    public List<PlaceResponseDTO> searchWithoutTranslation(String q) {
        logger.debug("{} [{}] Busqueda de lugares sin traducción con criterio: '{}'",
                AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, q);

        List<Place> places = placeRepository
                .findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(q, q, q);

        logger.debug("{} [{}] Resultados sin traducción para '{}': {}",
                AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, q, places.size());

        return toRawDTO(places);
    }

    @Transactional(readOnly = true)
    public List<PlaceResponseDTO> getSearchCache() {
        logger.debug("{} [{}] Cargando cache ligera de lugares activos para buscador",
                AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES);

        return placeRepository.findByStateTrue().stream()
                .map(PlaceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PlaceResponseDTO> getAllPlaces(String language) {
        logger.info("{} [{}] Solicitando lista completa de lugares", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando lista completa de lugares - GET /api/places", null));

        List<Place> places = placeRepository.findAll();
        logger.debug("{} [{}] Total de lugares: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, places.size());

        return toSummaryDTO(places, language);
    }

    @Transactional
    public PlaceResponseDTO getPlaceById(Long id, String language) {
        logger.info("{} [{}] Solicitando lugar con ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, id);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando informacion del lugar con ID: " + id + " - GET /api/places/" + id, null));

        Place place = placeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("{} [{}] Lugar con ID {} no encontrado", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, id);
                    logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_WARN,
                            "Lugar con ID " + id + " no encontrado - GET /api/places/" + id, null));
                    return new ResourceNotFoundException("Lugar no encontrado con ID: " + id);
                });

        logger.debug("{} [{}] Lugar encontrado: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, place.getName());

        return buildDetailedPlaceResponseDTO(place, language);
    }

    @Transactional
    public List<PlaceResponseDTO> getPlacesByDepartment(Long cityId, String language) {
        logger.info("{} [{}] Solicitando lugares para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, cityId);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando lugares para departamento ID: " + cityId, null));

        List<Place> places = placeRepository.findByCityIdAndStateTrue(cityId);

        if (places.isEmpty()) {
            logger.warn("{} [{}] No se encontraron lugares para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, cityId);
        } else {
            logger.debug("{} [{}] {} lugares para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, places.size(), cityId);
        }

        return toSummaryDTO(places, language);
    }

    @Transactional
    public List<PlaceResponseDTO> getTopPlacesByDepartment(Long cityId, String language) {
        logger.info("{} [{}] Solicitando top 3 lugares para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, cityId);
        logRepository.save(new LogEntity(AppConstants.LOG_PLACES, AppConstants.LOG_INFO,
                "Solicitando lugares mejor calificados para departamento ID: " + cityId, null));

        List<Place> places = placeRepository.findTop3ByCityIdAndStateTrueOrderByRatingDesc(cityId);

        if (places.isEmpty()) {
            logger.warn("{} [{}] No se encontraron lugares para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, cityId);
        } else {
            logger.debug("{} [{}] {} lugares para departamento ID: {}", AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, places.size(), cityId);
        }

        return toTopRatedDTO(places, language);
    }

    private List<PlaceResponseDTO> toRawDTO(List<Place> places) {
        return places.stream()
                .map(PlaceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private List<PlaceResponseDTO> toSearchDTO(List<Place> places, String language) {
        return places.stream()
                .map(place -> buildSearchPlaceResponseDTO(place, language))
                .collect(Collectors.toList());
    }

    private List<PlaceResponseDTO> toSummaryDTO(List<Place> places, String language) {
        return places.stream()
                .map(place -> buildSummaryPlaceResponseDTO(place, language))
                .collect(Collectors.toList());
    }

    private List<PlaceResponseDTO> toTopRatedDTO(List<Place> places, String language) {
        return places.stream()
                .map(place -> buildTopRatedPlaceResponseDTO(place, language))
                .collect(Collectors.toList());
    }

    private PlaceResponseDTO buildSearchPlaceResponseDTO(Place place, String language) {
        PlaceResponseDTO placeResponseDTO = PlaceResponseDTO.fromEntity(place);

        if (isSourceLanguage(language)) {
            return placeResponseDTO;
        }

        translateName(place, placeResponseDTO, language);
        translateAddress(place, placeResponseDTO, language);

        return placeResponseDTO;
    }

    private PlaceResponseDTO buildTopRatedPlaceResponseDTO(Place place, String language) {
        PlaceResponseDTO placeResponseDTO = PlaceResponseDTO.fromEntity(place);

        if (isSourceLanguage(language)) {
            return placeResponseDTO;
        }

        translateName(place, placeResponseDTO, language);

        return placeResponseDTO;
    }

    private PlaceResponseDTO buildSummaryPlaceResponseDTO(Place place, String language) {
        PlaceResponseDTO placeResponseDTO = PlaceResponseDTO.fromEntity(place);

        if (isSourceLanguage(language)) {
            return placeResponseDTO;
        }

        translatePlaceType(place, placeResponseDTO, language);

        return placeResponseDTO;
    }

    private PlaceResponseDTO buildDetailedPlaceResponseDTO(Place place, String language) {
        PlaceResponseDTO placeResponseDTO = PlaceResponseDTO.fromEntity(place);

        if (isSourceLanguage(language)) {
            return placeResponseDTO;
        }

        translateName(place, placeResponseDTO, language);
        translateDescription(place, placeResponseDTO, language);
        translateAddress(place, placeResponseDTO, language);
        translatePlaceType(place, placeResponseDTO, language);

        return placeResponseDTO;
    }

    private void translateName(Place place, PlaceResponseDTO placeResponseDTO, String language) {
        if (place.getName() == null || place.getName().trim().isEmpty()) {
            return;
        }

        placeResponseDTO.setName(translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                place.getId(),
                AppConstants.FIELD_NAME,
                language,
                place.getName()
        ));
    }

    private void translateDescription(Place place, PlaceResponseDTO placeResponseDTO, String language) {
        if (place.getDescription() == null || place.getDescription().trim().isEmpty()) {
            return;
        }

        placeResponseDTO.setDescription(translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                place.getId(),
                AppConstants.FIELD_DESCRIPTION,
                language,
                place.getDescription()
        ));
    }

    private void translateAddress(Place place, PlaceResponseDTO placeResponseDTO, String language) {
        if (place.getAddress() == null || place.getAddress().trim().isEmpty()) {
            return;
        }

        placeResponseDTO.setAddress(translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                place.getId(),
                AppConstants.FIELD_ADDRESS,
                language,
                place.getAddress()
        ));
    }

    private void translatePlaceType(Place place, PlaceResponseDTO placeResponseDTO, String language) {
        if (place.getPlaceType() == null || place.getPlaceType().trim().isEmpty()) {
            return;
        }

        placeResponseDTO.setPlaceType(translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_PLACE,
                place.getId(),
                AppConstants.FIELD_PLACE_TYPE,
                language,
                place.getPlaceType()
        ));
    }

    private boolean isSourceLanguage(String language) {
        return language == null || AppConstants.DEFAULT_LANGUAGE.equalsIgnoreCase(language);
    }
}