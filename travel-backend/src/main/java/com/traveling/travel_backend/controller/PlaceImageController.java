package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.PlaceImageResponseDTO;
import com.traveling.travel_backend.service.PlaceImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/places")
@CrossOrigin(origins = AppConstants.CORS_ALL)
@Tag(name = "Place Images", description = "Gestión de imágenes de lugares turísticos")
public class PlaceImageController {

    private final PlaceImageService placeImageService;

    public PlaceImageController(PlaceImageService placeImageService) {
        this.placeImageService = placeImageService;
    }

    @Operation(
            summary = "Get images by place",
            description = "Returns all active images for a given place"
    )
    @GetMapping("/{placeId}/images")
    public ResponseEntity<List<PlaceImageResponseDTO>> getImagesByPlaceId(
            @PathVariable Long placeId
    ) {
        return ResponseEntity.ok(placeImageService.getImagesByPlaceId(placeId));
    }

    @Operation(
            summary = "Delete place image",
            description = "Soft deletes a place image"
    )
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long imageId
    ) {
        placeImageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}