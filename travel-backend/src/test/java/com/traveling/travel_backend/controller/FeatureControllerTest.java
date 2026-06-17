package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.service.FeatureService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeatureControllerTest {

    @Mock
    private FeatureService featureService;

    @InjectMocks
    private FeatureController featureController;


    @Test
    @DisplayName("GET - Debe retornar HTTP 200 con el mapa de features")
    void getFeatures_Returns200WithFeatureMap() throws IOException {
        Map<String, Boolean> mockFeatures = Map.of(
                "autoCreateItinerary", true,
                "showSearchPlaces", true,
                "showFavorites", true
        );
        when(featureService.getFeatures()).thenReturn(mockFeatures);

        ResponseEntity<Map<String, Boolean>> response = featureController.getFeatures();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(mockFeatures);
        verify(featureService, times(1)).getFeatures();
    }

    @Test
    @DisplayName("GET - Debe delegar la llamada al servicio exactamente una vez")
    void getFeatures_DelegatesToServiceOnce() throws IOException {
        when(featureService.getFeatures()).thenReturn(Map.of());

        featureController.getFeatures();

        verify(featureService, times(1)).getFeatures();
        verifyNoMoreInteractions(featureService);
    }

    @Test
    @DisplayName("GET - Debe retornar el mapa exacto que devuelve el servicio")
    void getFeatures_ReturnsExactlyWhatServiceReturns() throws IOException {
        Map<String, Boolean> mockFeatures = new HashMap<>();
        mockFeatures.put("autoCreateItinerary", false);
        when(featureService.getFeatures()).thenReturn(mockFeatures);

        ResponseEntity<Map<String, Boolean>> response = featureController.getFeatures();

        assertThat(response.getBody()).containsEntry("autoCreateItinerary", false);
    }

    @Test
    @DisplayName("PUT - Debe retornar HTTP 200 con el mapa actualizado")
    void updateFeatures_Returns200WithUpdatedMap() throws IOException {
        Map<String, Boolean> incoming = Map.of(
                "autoCreateItinerary", false,
                "showSearchPlaces", false,
                "showFavorites", true
        );
        Map<String, Boolean> serviceResponse = Map.of(
                "autoCreateItinerary", false,
                "showSearchPlaces", false,
                "showFavorites", true
        );
        when(featureService.updateFeatures(incoming)).thenReturn(serviceResponse);

        ResponseEntity<Map<String, Boolean>> response = featureController.updateFeatures(incoming);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
        verify(featureService, times(1)).updateFeatures(incoming);
    }

    @Test
    @DisplayName("PUT - Debe pasar el body recibido al servicio sin modificarlo")
    void updateFeatures_PassesBodyToServiceUnmodified() throws IOException {
        Map<String, Boolean> incoming = new HashMap<>();
        incoming.put("autoCreateItinerary", true);
        incoming.put("keyInventada", true);
        when(featureService.updateFeatures(incoming)).thenReturn(Map.of("autoCreateItinerary", true));

        featureController.updateFeatures(incoming);

        verify(featureService, times(1)).updateFeatures(incoming);
    }

    @Test
    @DisplayName("PUT - Debe delegar al servicio exactamente una vez")
    void updateFeatures_DelegatesToServiceOnce() throws IOException {
        Map<String, Boolean> incoming = Map.of("autoCreateItinerary", false);
        when(featureService.updateFeatures(incoming)).thenReturn(incoming);

        featureController.updateFeatures(incoming);

        verify(featureService, times(1)).updateFeatures(incoming);
        verifyNoMoreInteractions(featureService);
    }
}