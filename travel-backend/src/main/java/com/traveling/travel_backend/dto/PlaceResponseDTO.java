package com.traveling.travel_backend.dto;

import com.traveling.travel_backend.model.Place;

import java.time.LocalDateTime;

public class PlaceResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String address;
    private double rating;
    private double price;
    private double latitude;
    private double longitude;
    private String placeType;
    private CityResponseDTO city;
    private boolean isEvent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String imageUrl;
    private boolean state;

    public PlaceResponseDTO() {}

    public static PlaceResponseDTO fromEntity(Place place) {
        PlaceResponseDTO dto = new PlaceResponseDTO();
        dto.id          = place.getId();
        dto.name        = place.getName();
        dto.description = place.getDescription();
        dto.address     = place.getAddress();
        dto.rating      = place.getRating();
        dto.price       = place.getPrice();
        dto.latitude    = place.getLatitude();
        dto.longitude   = place.getLongitude();
        dto.placeType   = place.getPlaceType();
        dto.city        = place.getCity() != null ? CityResponseDTO.fromEntity(place.getCity()) : null;
        dto.isEvent     = place.isEvent();
        dto.startDate   = place.getStartDate();
        dto.endDate     = place.getEndDate();
        dto.imageUrl    = place.getImageUrl();
        dto.state       = place.isState();
        return dto;
    }

    // ---------- Getters & Setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getPlaceType() { return placeType; }
    public void setPlaceType(String placeType) { this.placeType = placeType; }

    public CityResponseDTO getCity() { return city; }
    public void setCity(CityResponseDTO city) { this.city = city; }

    public boolean isEvent() { return isEvent; }
    public void setEvent(boolean event) { isEvent = event; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }
}