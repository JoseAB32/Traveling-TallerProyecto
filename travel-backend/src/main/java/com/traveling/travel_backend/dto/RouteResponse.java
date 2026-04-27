package com.traveling.travel_backend.dto;

import java.util.List;

public class RouteResponse {
    private double distanceKm;
    private double durationMinutes;
    private List<List<Double>> coordinates;

    public RouteResponse() {}

    public RouteResponse(double distanceKm, double durationMinutes, List<List<Double>> coordinates) {
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.coordinates = coordinates;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(double durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }
}
