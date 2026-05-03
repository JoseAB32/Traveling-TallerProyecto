package com.traveling.travel_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class FeatureService {

    private static final Logger logger = LoggerFactory.getLogger(FeatureService.class);

    private final LogRepository logRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.features.file-path:./config/features.json}")
    private String filePath;

    public FeatureService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    private Map<String, Boolean> defaultFeatures() {
        Map<String, Boolean> defaults = new LinkedHashMap<>();
        defaults.put("pinRedirection", false);
        defaults.put("autoCreateItinerary", true);
        defaults.put("showSearchPlaces", true);
        defaults.put("showFavorites", true);
        return defaults;
    }

    @Transactional
    public Map<String, Boolean> getFeatures() throws IOException {
        logger.info("{} [{}] {}", AppConstants.PREFIX_FEATURE, AppConstants.LOG_FEATURES, AppConstants.FEATURES_FETCH_REQUEST);
        logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_INFO, AppConstants.FEATURES_FETCH_REQUEST, null));

        File file = new File(filePath);
        logger.debug("{} [{}] Buscando features.json en: {}", AppConstants.PREFIX_FEATURE, AppConstants.LOG_FEATURES, file.getAbsolutePath());

        ensureParentDirExists(file);

        if (!file.exists()) {
            logger.warn("{} [{}] {}", AppConstants.PREFIX_FEATURE, AppConstants.LOG_FEATURES, AppConstants.FEATURES_FILE_NOT_FOUND);
            logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_WARN, AppConstants.FEATURES_FILE_NOT_FOUND, null));

            Map<String, Boolean> defaults = defaultFeatures();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, defaults);
            return defaults;
        }

        Map<String, Boolean> stored = objectMapper.readValue(file, new TypeReference<Map<String, Boolean>>() {});
        Map<String, Boolean> merged = defaultFeatures();
        merged.putAll(stored);

        // Persistir si se agregaron nuevas keys
        if (merged.size() != stored.size()) {
            logger.warn("{} [{}] {}", AppConstants.PREFIX_FEATURE, AppConstants.LOG_FEATURES, AppConstants.FEATURES_NEW_KEYS_ADDED);
            logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_WARN,
                    "Se agregaron nuevas keys al archivo features.json. Total keys: " + merged.size(), null));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, merged);
        }

        logger.info("{} [{}] {} {}", AppConstants.PREFIX_FEATURE, AppConstants.LOG_FEATURES, AppConstants.FEATURES_FETCHED_SUCCESS, merged.size());
        logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_INFO,
                "Features obtenidas correctamente. Total: " + merged.size(), null));

        return merged;
    }

    @Transactional
    public Map<String, Boolean> updateFeatures(Map<String, Boolean> incoming) throws IOException {
        logger.info("{} [{}] {}", AppConstants.PREFIX_FEATURE, AppConstants.LOG_FEATURES, AppConstants.FEATURES_UPDATE_REQUEST);
        logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_INFO, AppConstants.FEATURES_UPDATE_REQUEST, null));

        logger.debug("{} [{}] Features recibidas para actualizar: {}", AppConstants.PREFIX_FEATURE, AppConstants.LOG_FEATURES, incoming);

        Map<String, Boolean> merged = defaultFeatures();
        incoming.forEach((key, value) -> {
            if (merged.containsKey(key)) {
                merged.put(key, value);
            }
        });

        File file = new File(filePath);
        ensureParentDirExists(file);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, merged);

        logger.info("{} [{}] {} {}", AppConstants.PREFIX_FEATURE, AppConstants.LOG_FEATURES, AppConstants.FEATURES_UPDATED_SUCCESS, merged.size());
        logRepository.save(new LogEntity(AppConstants.LOG_FEATURES, AppConstants.LOG_INFO,
                "Features actualizadas correctamente. Total keys: " + merged.size(), null));

        return merged;
    }

    private void ensureParentDirExists(File file) {
        File parentDir = file.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
    }
}