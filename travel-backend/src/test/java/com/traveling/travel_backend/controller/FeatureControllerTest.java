package com.traveling.travel_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.repository.LogRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class FeatureControllerTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private FeatureController featureController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        String path = tempDir.resolve("features.json").toString();
        ReflectionTestUtils.setField(featureController, "filePath", path);
    }

    @Test
    @DisplayName("GET - Debe crear features.json con defaults si no existe")
    void getFeatures_CreatesFileWithDefaultsWhenNotExists() throws IOException {
        Map<String, Boolean> result = featureController.getFeatures();

        assertThat(result).containsEntry("pinRedirection", false);
        assertThat(result).containsEntry("autoCreateItinerary", true);
        assertThat(result).containsEntry("showSearchPlaces", true);
        assertThat(result).containsEntry("showFavorites", true);

        File file = tempDir.resolve("features.json").toFile();
        assertThat(file).exists();
    }

    @Test
    @DisplayName("GET - Debe retornar valores guardados si el archivo existe")
    void getFeatures_ReturnsStoredValuesWhenFileExists() throws IOException {
        File file = tempDir.resolve("features.json").toFile();
        objectMapper.writeValue(file, Map.of(
            "pinRedirection", true,
            "autoCreateItinerary", false,
            "showSearchPlaces", true,
            "showFavorites", false
        ));

        Map<String, Boolean> result = featureController.getFeatures();

        assertThat(result).containsEntry("pinRedirection", true);
        assertThat(result).containsEntry("autoCreateItinerary", false);
        assertThat(result).containsEntry("showFavorites", false);
    }

    @Test
    @DisplayName("GET - Debe agregar keys faltantes al archivo existente")
    void getFeatures_MergesNewKeysIntoExistingFile() throws IOException {
        File file = tempDir.resolve("features.json").toFile();
        objectMapper.writeValue(file, Map.of(
            "pinRedirection", true,
            "autoCreateItinerary", false
        ));

        Map<String, Boolean> result = featureController.getFeatures();

        assertThat(result).containsEntry("pinRedirection", true);
        assertThat(result).containsEntry("autoCreateItinerary", false);
        assertThat(result).containsEntry("showSearchPlaces", true);
        assertThat(result).containsEntry("showFavorites", true);
        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("PUT - Debe actualizar y retornar los nuevos valores")
    void updateFeatures_SavesAndReturnsUpdatedValues() throws IOException {
        Map<String, Boolean> updated = Map.of(
            "pinRedirection", true,
            "autoCreateItinerary", false,
            "showSearchPlaces", false,
            "showFavorites", true
        );

        Map<String, Boolean> result = featureController.updateFeatures(updated);

        assertThat(result).containsEntry("pinRedirection", true);
        assertThat(result).containsEntry("autoCreateItinerary", false);
        assertThat(result).containsEntry("showSearchPlaces", false);
        assertThat(result).containsEntry("showFavorites", true);
    }

    @Test
    @DisplayName("PUT - Debe persistir los valores en el archivo JSON")
    void updateFeatures_PersistsValuesToFile() throws IOException {
        Map<String, Boolean> updated = Map.of(
            "pinRedirection", true,
            "autoCreateItinerary", true,
            "showSearchPlaces", false,
            "showFavorites", false
        );

        featureController.updateFeatures(updated);

        File file = tempDir.resolve("features.json").toFile();
        assertThat(file).exists();

        Map<?, ?> savedOnDisk = objectMapper.readValue(file, Map.class);
        assertThat(savedOnDisk.get("pinRedirection")).isEqualTo(true);
        assertThat(savedOnDisk.get("showSearchPlaces")).isEqualTo(false);
    }

    @Test
    @DisplayName("PUT - Debe ignorar keys desconocidas y no persistirlas")
    void updateFeatures_IgnoresUnknownKeys() throws IOException {
        Map<String, Boolean> withUnknownKeys = new HashMap<>();
        withUnknownKeys.put("pinRedirection", true);
        withUnknownKeys.put("autoCreateItinerary", false);
        withUnknownKeys.put("showSearchPlaces", true);
        withUnknownKeys.put("showFavorites", false);
        withUnknownKeys.put("keyInventada", true); // key desconocida

        Map<String, Boolean> result = featureController.updateFeatures(withUnknownKeys);

        assertThat(result).doesNotContainKey("keyInventada");
        assertThat(result).hasSize(4);
        assertThat(result).containsEntry("pinRedirection", true);
        assertThat(result).containsEntry("showFavorites", false);
    }
}