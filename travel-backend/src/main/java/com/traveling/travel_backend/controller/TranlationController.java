package com.traveling.travel_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.TranslationPageResponseDTO;
import com.traveling.travel_backend.dto.TranslationResponseDTO;
import com.traveling.travel_backend.dto.UpdateTranslationRequestDTO;
import com.traveling.travel_backend.service.TranslationsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping(AppConstants.API_BASE_PATH)
@CrossOrigin(origins = {AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY})
@Tag(name = "Traducciones", description = "Manejo de Admin para revisar traducciones")
public class TranlationController {
    private final TranslationsService translationService;

    public TranlationController(TranslationsService translationService) {
        this.translationService = translationService;
    }

    @Operation(
            summary = "Obtener Traducciones",
            description = "Permite revisar y gestionar las traducciones que hizo el sistema de traducción automática",
            operationId = "reviewTranslations"
    )
    @GetMapping(AppConstants.TRANSLATIONS_ENDPOINT)
        public ResponseEntity<TranslationPageResponseDTO> getTranslations(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String fieldName,
            @RequestParam(required = false) Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(translationService.getTranslations(entityType, language, fieldName, entityId, page, size));
    }

    @Operation(
            summary = "Cambiar Traduccion",
            description = "Permite cambiar el resultado de una traducción automática por una traducción manual, para corregir errores o mejorar la calidad de la traducción",
            operationId = "UpdateTranslation"
    )
    @PutMapping(AppConstants.TRANSLATIONS_ENDPOINT + "/{id}")
    public ResponseEntity<TranslationResponseDTO> updateTranslation(
            @PathVariable Long id,
            @RequestBody UpdateTranslationRequestDTO request) {
        return ResponseEntity.ok(translationService.updateTranslation(id, request));
    }
}
