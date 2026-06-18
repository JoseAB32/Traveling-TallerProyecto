package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.CreatePlaceRequestDTO;
import com.traveling.travel_backend.dto.PlaceResponseDTO;
import com.traveling.travel_backend.exception.BadRequestException;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.model.PlaceImage;
import com.traveling.travel_backend.repository.CityRepository;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.PlaceImageRepository;
import com.traveling.travel_backend.repository.PlaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceService {

    private static final Logger logger = LoggerFactory.getLogger(PlaceService.class);

    private final PlaceRepository placeRepository;
    private final CityRepository cityRepository;
    private final PlaceImageRepository placeImageRepository;
    private final LogRepository logRepository;
    private final TranslationsService translationsService;
    private final CloudinaryService cloudinaryService;

    public PlaceService(
            PlaceRepository placeRepository,
            CityRepository cityRepository,
            PlaceImageRepository placeImageRepository,
            LogRepository logRepository,
            TranslationsService translationsService,
            CloudinaryService cloudinaryService
    ) {
        this.placeRepository = placeRepository;
        this.cityRepository = cityRepository;
        this.placeImageRepository = placeImageRepository;
        this.logRepository = logRepository;
        this.translationsService = translationsService;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public PlaceResponseDTO createPlace(CreatePlaceRequestDTO request, List<MultipartFile> imageFiles) {
        validateCreatePlaceRequest(request);
        validateImageFiles(imageFiles);

        String normalizedName = normalizeRequired(request.getName());
        String normalizedDescription = normalizeRequired(request.getDescription());
        String normalizedAddress = normalizeRequired(request.getAddress());
        String normalizedPlaceType = normalizeRequired(request.getPlaceType());

        City city = cityRepository.findById(request.getCityId())
                .filter(City::isState)
                .orElseThrow(() -> new BadRequestException("La ciudad seleccionada no existe o no está activa."));

        if (placeRepository.existsByNameIgnoreCaseAndCityIdAndStateTrue(normalizedName, city.getId())) {
            throw new BadRequestException("Ya existe un lugar turístico activo con ese nombre en la ciudad seleccionada.");
        }

        Place place = new Place();
        place.setName(normalizedName);
        place.setDescription(normalizedDescription);
        place.setAddress(normalizedAddress);
        place.setPrice(request.getPrice());
        place.setLatitude(request.getLatitude());
        place.setLongitude(request.getLongitude());
        place.setPlaceType(normalizedPlaceType);
        place.setCity(city);
        place.setRating(AppConstants.MAX_RATING);
        place.setState(true);

        boolean isEvent = Boolean.TRUE.equals(request.getIsEvent());
        place.setEvent(isEvent);
        place.setStartDate(isEvent ? request.getStartDate() : null);
        place.setEndDate(isEvent ? request.getEndDate() : null);

        Place savedPlace = placeRepository.save(place);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            int displayOrder = 0;

            for (MultipartFile imageFile : imageFiles) {
                if (imageFile == null || imageFile.isEmpty()) {
                    continue;
                }

                String cloudinaryUrl = cloudinaryService.uploadPlaceImage(
                        imageFile,
                        savedPlace.getId(),
                        displayOrder
                );

                PlaceImage image = new PlaceImage();
                image.setImageUrl(cloudinaryUrl);
                image.setAltText(normalizedName);
                image.setDisplayOrder(displayOrder);
                image.setIsMain(displayOrder == 0);
                image.setState(true);
                image.setPlace(savedPlace);

                PlaceImage savedImage = placeImageRepository.save(image);

                if (savedPlace.getImages() != null) {
                    savedPlace.getImages().add(savedImage);
                }

                displayOrder++;
            }
        }

        logger.info("{} [{}] Lugar turístico creado con ID: {}",
                AppConstants.PREFIX_PLACE, AppConstants.LOG_PLACES, savedPlace.getId());

        logRepository.save(new LogEntity(
                AppConstants.LOG_PLACES,
                AppConstants.LOG_INFO,
                "Lugar turístico creado: " + savedPlace.getName() + " - POST /api/places",
                null
        ));

        return PlaceResponseDTO.fromEntity(savedPlace);
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

    private void validateCreatePlaceRequest(CreatePlaceRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("Los datos del lugar turístico son obligatorios.");
        }

        validateText(request.getName(), "El nombre", 3, 100);
        validateText(request.getDescription(), "La descripción", 10, 2000);
        validateText(request.getAddress(), "La dirección", 5, 255);
        validateText(request.getPlaceType(), "El tipo de lugar", 3, 50);

        if (request.getCityId() == null || request.getCityId() <= 0) {
            throw new BadRequestException("Debe seleccionar una ciudad válida.");
        }

        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new BadRequestException("El precio debe ser mayor o igual a 0.");
        }

        if (request.getLatitude() == null || request.getLatitude() < -90 || request.getLatitude() > 90) {
            throw new BadRequestException("La latitud debe estar entre -90 y 90.");
        }

        if (request.getLongitude() == null || request.getLongitude() < -180 || request.getLongitude() > 180) {
            throw new BadRequestException("La longitud debe estar entre -180 y 180.");
        }

        if (Boolean.TRUE.equals(request.getIsEvent())) {
            if (request.getStartDate() == null || request.getEndDate() == null) {
                throw new BadRequestException("Los eventos deben tener fecha de inicio y fecha de fin.");
            }

            if (!request.getEndDate().isAfter(request.getStartDate())) {
                throw new BadRequestException("La fecha de fin debe ser posterior a la fecha de inicio.");
            }
        }
    }

    private void validateImageFiles(List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return;
        }

        int validImages = 0;
        long maxSize = 5 * 1024 * 1024;

        for (MultipartFile imageFile : imageFiles) {
            if (imageFile == null || imageFile.isEmpty()) {
                continue;
            }

            validImages++;

            String contentType = imageFile.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("Todos los archivos seleccionados deben ser imágenes.");
            }

            if (imageFile.getSize() > maxSize) {
                throw new BadRequestException("Cada imagen no debe superar los 5 MB.");
            }
        }

        if (validImages > 5) {
            throw new BadRequestException("Solo se permite subir hasta 5 imágenes por lugar turístico.");
        }
    }

    private void validateText(String value, String fieldName, int minLength, int maxLength) {
        String normalized = normalizeOptional(value);

        if (normalized == null) {
            throw new BadRequestException(fieldName + " es obligatorio.");
        }

        if (normalized.length() < minLength) {
            throw new BadRequestException(fieldName + " debe tener al menos " + minLength + " caracteres.");
        }

        if (normalized.length() > maxLength) {
            throw new BadRequestException(fieldName + " no debe superar " + maxLength + " caracteres.");
        }
    }

    private String normalizeRequired(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return "";
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? null : normalized;
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