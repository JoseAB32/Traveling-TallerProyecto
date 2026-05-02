package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.CityResponseDTO;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.repository.CityRepository;
import com.traveling.travel_backend.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CityService {

    private static final Logger logger = LoggerFactory.getLogger(CityService.class);

    private final CityRepository cityRepository;
    private final LogRepository logRepository;

    public CityService(CityRepository cityRepository, LogRepository logRepository) {
        this.cityRepository = cityRepository;
        this.logRepository = logRepository;
    }

    @Transactional
    public List<CityResponseDTO> getAllCities() {
        logger.info("{} [{}] Consultando todas las ciudades", AppConstants.PREFIX_CITY, AppConstants.LOG_CITIES);
        logRepository.save(new LogEntity(AppConstants.LOG_CITIES, AppConstants.LOG_INFO,
                "Peticion recibida: GET /api/cities", null));

        List<City> cities = cityRepository.findAll();

        logger.info("{} [{}] Ciudades encontradas: {} y devueltas correctamente.",
                AppConstants.PREFIX_CITY, AppConstants.LOG_CITIES, cities.size());
        logRepository.save(new LogEntity(AppConstants.LOG_CITIES, AppConstants.LOG_INFO,
                "Ciudades encontradas: " + cities.size() + " y devueltas correctamente.", null));

        return cities.stream()
                .map(CityResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CityResponseDTO getCityById(Long id) {
        logger.info("{} [{}] Solicitando ciudad con ID: {}", AppConstants.PREFIX_CITY, AppConstants.LOG_CITIES, id);
        logRepository.save(new LogEntity(AppConstants.LOG_CITIES, AppConstants.LOG_INFO,
                "Solicitando informacion de la ciudad con ID: " + id + " - GET /api/cities/" + id, null));

        City city = cityRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("{} [{}] Ciudad con ID {} no encontrada", AppConstants.PREFIX_CITY, AppConstants.LOG_CITIES, id);
                    logRepository.save(new LogEntity(AppConstants.LOG_CITIES, AppConstants.LOG_WARN,
                            "Ciudad con ID " + id + " no encontrada - GET /api/cities/" + id, null));
                    return new ResourceNotFoundException("Ciudad no encontrada con ID: " + id);
                });

        logger.debug("{} [{}] Ciudad encontrada: {}", AppConstants.PREFIX_CITY, AppConstants.LOG_CITIES, city.getName());
        return CityResponseDTO.fromEntity(city);
    }
}