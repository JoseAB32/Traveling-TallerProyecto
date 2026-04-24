package com.traveling.travel_backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.repository.LogRepository;

import io.swagger.v3.oas.annotations.Operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/features")
@CrossOrigin(origins = "*")
public class FeatureController {

    // LOGGER
    private static final Logger logger = LoggerFactory.getLogger(FeatureController.class);

    @Autowired
    private LogRepository logRepository;

    @Value("${app.features.file-path:./config/features.json}")
    private String filePath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Boolean> defaultFeatures() {
        Map<String, Boolean> defaults = new LinkedHashMap<>();
        defaults.put("pinRedirection", false);
        defaults.put("autoCreateItinerary", true);
        defaults.put("showSearchPlaces", true);
        defaults.put("showFavorites", true);
        return defaults;
    }

    @Operation(
        summary = "Get features JSON configuration",
        description = "Returns a map of all feature flags/toggles for app features",
        tags = {"Feature"},
        operationId = "getFeatures"
    )
    @GetMapping
    public Map<String, Boolean> getFeatures() throws IOException {

        logger.info(AppConstants.PREFIX_FEATURE + " [" + AppConstants.LOG_FEATURES + "] " + AppConstants.FEATURES_FETCH_REQUEST);
        logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_INFO, AppConstants.FEATURES_FETCH_REQUEST, null));

        File file = new File(filePath);
        logger.debug(AppConstants.PREFIX_FEATURE + " [" + AppConstants.LOG_FEATURES + "] Buscando features.json en: {}", file.getAbsolutePath());

        // Crear directorios padre si no existen
        File parentDir = file.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }

        if (!file.exists()) {
            logger.warn(AppConstants.PREFIX_FEATURE + " [" + AppConstants.LOG_FEATURES + "] " + AppConstants.FEATURES_FILE_NOT_FOUND);
            logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_WARN, AppConstants.FEATURES_FILE_NOT_FOUND, null));

            Map<String, Boolean> defaults = defaultFeatures();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, defaults);
            return defaults;
        }

        Map<String, Boolean> stored = objectMapper.readValue(
            file, new TypeReference<Map<String, Boolean>>() {}
        );

        // Si hay nuevas keys en defaults que no están en el archivo, las agrega
        Map<String, Boolean> merged = defaultFeatures();
        merged.putAll(stored);

        // Si se agregaron keys nuevas, persistir
        if (merged.size() != stored.size()) {
            logger.warn(AppConstants.PREFIX_FEATURE + " [" + AppConstants.LOG_FEATURES + "] " + AppConstants.FEATURES_NEW_KEYS_ADDED, merged.size());
            logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_WARN,
                "Se agregaron nuevas keys al archivo features.json. Total keys: " + merged.size(), null));

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, merged);
        }

        logger.info(AppConstants.PREFIX_FEATURE + " [" + AppConstants.LOG_FEATURES + "] " + AppConstants.FEATURES_FETCHED_SUCCESS, merged.size());
        logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_INFO,
            "Features obtenidas correctamente. Total: " + merged.size(), null));

        return merged;
    }

    @Operation(
        summary = "Update features JSON configuration",
        description = "Update values of flags/toggles for app features",
        tags = {"Feature"},
        operationId = "updateFeatures"
    )
    @PutMapping
    public Map<String, Boolean> updateFeatures(@RequestBody Map<String, Boolean> features) throws IOException {

        logger.info(AppConstants.PREFIX_FEATURE + " [" + AppConstants.LOG_FEATURES + "] " + AppConstants.FEATURES_UPDATE_REQUEST);
        logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_INFO, AppConstants.FEATURES_UPDATE_REQUEST, null));

        logger.debug(AppConstants.PREFIX_FEATURE + " [" + AppConstants.LOG_FEATURES + "] Features recibidas para actualizar: {}", features);

        File file = new File(filePath);
        file.getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, features);

        logger.info(AppConstants.PREFIX_FEATURE + " [" + AppConstants.LOG_FEATURES + "] " + AppConstants.FEATURES_UPDATED_SUCCESS, features.size());
        logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_INFO,
            "Features actualizadas correctamente. Total keys: " + features.size(), null));

        return features;
    }
}