package com.traveling.travel_backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/features")
@CrossOrigin(origins = "*") // Ajusta esto según tus CORS
public class FeatureController {

    // El archivo se creará en la raíz de tu proyecto backend
    private final String FILE_PATH = "features.json"; 
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public Map<String, Boolean> getFeatures() throws IOException {
        File file = new File(FILE_PATH);
        
        // Si es la primera vez y el archivo no existe, lo creamos con valores por defecto
        if (!file.exists()) {
            Map<String, Boolean> defaultFeatures = Map.of(
                "pinRedirection", false,
                "autoCreateItinerary", true
            );
            objectMapper.writeValue(file, defaultFeatures);
            return defaultFeatures;
        }
        
        // Leemos el archivo JSON y lo devolvemos como un Map
        return objectMapper.readValue(file, new TypeReference<Map<String, Boolean>>() {});
    }

    @PutMapping
    public Map<String, Boolean> updateFeatures(@RequestBody Map<String, Boolean> features) throws IOException {
        File file = new File(FILE_PATH);
        // Sobreescribimos el archivo con los nuevos valores que manda Angular
        objectMapper.writeValue(file, features);
        return features;
    }
}
