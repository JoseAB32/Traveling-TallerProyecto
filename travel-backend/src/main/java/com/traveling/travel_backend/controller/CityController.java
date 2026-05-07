package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.CityResponseDTO;
import com.traveling.travel_backend.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH)
@CrossOrigin(origins = {AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY})
@Tag(name = "Cities", description = "Gestión de ciudades y departamentos")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @Operation(
            summary = "Obtener todas las ciudades/departamentos",
            description = "Retorna la lista completa de ciudades/departamentos registrados en la base de datos",
            operationId = "getAllCities"
    )
    @GetMapping(AppConstants.CITIES_ENDPOINT)
    public ResponseEntity<List<CityResponseDTO>> getAllCities() {
        List<CityResponseDTO> cities = cityService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @Operation(
            summary = "Obtener ciudad por ID",
            description = "Retorna la información de una ciudad/departamento según su ID",
            operationId = "getCityById"
    )
    @GetMapping(AppConstants.CITIES_ENDPOINT + "/{id}")
    public ResponseEntity<CityResponseDTO> getCityById(@PathVariable Long id) {
        CityResponseDTO city = cityService.getCityById(id);
        return ResponseEntity.ok(city);
    }
}