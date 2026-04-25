package com.traveling.travel_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.security.JwtService;
import com.traveling.travel_backend.model.LogEntity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeatureController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FeatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LogRepository logRepository;

    @MockBean
    private JwtService jwtService;

    @TempDir
    Path tempDir;


    @Test
    @DisplayName("GET /api/features - Debe crear features.json con defaults si no existe")
    void getFeatures_CreatesFileWithDefaultsWhenNotExists() throws Exception {
        String path = tempDir.resolve("features.json").toString();
        System.setProperty("app.features.file-path", path);

        mockMvc.perform(get("/api/features"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pinRedirection").value(false))
            .andExpect(jsonPath("$.autoCreateItinerary").value(true))
            .andExpect(jsonPath("$.showSearchPlaces").value(true))
            .andExpect(jsonPath("$.showFavorites").value(true));
    }

    @Test
    @DisplayName("GET /api/features - Debe retornar valores guardados si el archivo existe")
    void getFeatures_ReturnsStoredValuesWhenFileExists() throws Exception {
        File file = tempDir.resolve("features.json").toFile();
        Map<String, Boolean> stored = Map.of(
            "pinRedirection", true,
            "autoCreateItinerary", false,
            "showSearchPlaces", true,
            "showFavorites", false
        );
        objectMapper.writeValue(file, stored);

        System.setProperty("app.features.file-path", file.getAbsolutePath());

        mockMvc.perform(get("/api/features"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pinRedirection").value(true))
            .andExpect(jsonPath("$.autoCreateItinerary").value(false))
            .andExpect(jsonPath("$.showSearchPlaces").value(true))
            .andExpect(jsonPath("$.showFavorites").value(false));
    }

    @Test
    @DisplayName("GET /api/features - Debe agregar keys faltantes al archivo existente")
    void getFeatures_MergesNewKeysIntoExistingFile() throws Exception {
        File file = tempDir.resolve("features.json").toFile();
        Map<String, Boolean> partial = Map.of(
            "pinRedirection", true,
            "autoCreateItinerary", false
        );
        objectMapper.writeValue(file, partial);

        System.setProperty("app.features.file-path", file.getAbsolutePath());

        mockMvc.perform(get("/api/features"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pinRedirection").value(true))       
            .andExpect(jsonPath("$.autoCreateItinerary").value(false)) 
            .andExpect(jsonPath("$.showSearchPlaces").value(true))     
            .andExpect(jsonPath("$.showFavorites").value(true));       
    }


    @Test
    @DisplayName("PUT /api/features - Debe actualizar y retornar los nuevos valores")
    void updateFeatures_SavesAndReturnsUpdatedValues() throws Exception {
        File file = tempDir.resolve("features.json").toFile();
        System.setProperty("app.features.file-path", file.getAbsolutePath());

        Map<String, Boolean> updated = Map.of(
            "pinRedirection", true,
            "autoCreateItinerary", false,
            "showSearchPlaces", false,
            "showFavorites", true
        );

        mockMvc.perform(put("/api/features")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pinRedirection").value(true))
            .andExpect(jsonPath("$.autoCreateItinerary").value(false))
            .andExpect(jsonPath("$.showSearchPlaces").value(false))
            .andExpect(jsonPath("$.showFavorites").value(true));
    }

    @Test
    @DisplayName("PUT /api/features - Debe persistir los valores en el archivo JSON")
    void updateFeatures_PersistsValuesToFile() throws Exception {
        File file = tempDir.resolve("features.json").toFile();
        System.setProperty("app.features.file-path", file.getAbsolutePath());

        Map<String, Boolean> updated = Map.of(
            "pinRedirection", true,
            "autoCreateItinerary", true,
            "showSearchPlaces", false,
            "showFavorites", false
        );

        mockMvc.perform(put("/api/features")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
            .andExpect(status().isOk());

        Map<?, ?> savedOnDisk = objectMapper.readValue(file, Map.class);
        assert savedOnDisk.get("pinRedirection").equals(true);
        assert savedOnDisk.get("showSearchPlaces").equals(false);
    }
}