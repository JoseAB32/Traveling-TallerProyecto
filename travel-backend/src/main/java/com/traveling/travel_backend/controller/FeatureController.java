package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.service.FeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/features")
@CrossOrigin(origins = "*")
@Tag(name = "Feature", description = "Gestion de feature flags")
public class FeatureController {

    private final FeatureService featureService;

    public FeatureController(FeatureService featureService) {
        this.featureService = featureService;
    }

    @Operation(summary = "Get features JSON configuration", description = "Returns a map of all feature flags/toggles", operationId = "getFeatures")
    @GetMapping
    public ResponseEntity<Map<String, Boolean>> getFeatures() throws IOException {
        return ResponseEntity.ok(featureService.getFeatures());
    }

    @Operation(summary = "Update features JSON configuration", description = "Update values of feature flags/toggles", operationId = "updateFeatures")
    @PutMapping
    public ResponseEntity<Map<String, Boolean>> updateFeatures(@RequestBody Map<String, Boolean> features) throws IOException {
        return ResponseEntity.ok(featureService.updateFeatures(features));
    }
}