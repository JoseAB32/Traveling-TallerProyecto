package com.traveling.travel_backend.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.dto.RouteResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RouteService {

    public RouteResponse getRoute(double originLat, double originLng, double destinationLat, double destinationLng) {
        try {
            String url = String.format(
                Locale.US,
                "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                originLng, originLat, destinationLng, destinationLat
            );

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode route = root.path("routes").get(0);

            double distanceKm = route.path("distance").asDouble() / 1000;
            double durationMinutes = route.path("duration").asDouble() / 60;

            List<List<Double>> coordinates = new ArrayList<>();

            for (JsonNode coord : route.path("geometry").path("coordinates")) {
                List<Double> point = new ArrayList<>();
                point.add(coord.get(1).asDouble()); 
                point.add(coord.get(0).asDouble()); 
                coordinates.add(point);
            }

            return new RouteResponse(distanceKm, durationMinutes, coordinates);

        } catch (Exception e) {
            throw new RuntimeException("Error obtaining route from OSRM", e);
        }
    }
}