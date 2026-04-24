package com.traveling.travel_backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;

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

    @Value("${app.features.file-path:./config/features.json}")
    private String filePath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Valores por defecto para toggles
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
        File file = new File(filePath);
        System.out.println(">>> Buscando features.json en: " + file.getAbsolutePath());

        // Crear directorios padre si no existen
        File parentDir = file.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
 
        if (!file.exists()) {
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
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, merged);
        }

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
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, features);
        return features;
    }
}
