package com.traveling.travel_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.traveling.travel_backend.dto.RouteResponse;
import com.traveling.travel_backend.services.RouteService;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<RouteResponse> getRoute(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam double destinationLat,
            @RequestParam double destinationLng
    ) {
        return ResponseEntity.ok(
                routeService.getRoute(originLat, originLng, destinationLat, destinationLng)
        );
    }
}