package com.traveling.travel_backend.dto;

import com.traveling.travel_backend.model.Favorite;

public class FavoriteResponseDTO {

    private Long id;
    private PlaceResponseDTO place;
    private String createdAt;
    private boolean state;

    public FavoriteResponseDTO() {}

    public static FavoriteResponseDTO fromEntity(Favorite favorite) {
        FavoriteResponseDTO dto = new FavoriteResponseDTO();
        dto.id        = favorite.getId();
        dto.place     = favorite.getPlace() != null ? PlaceResponseDTO.fromEntity(favorite.getPlace()) : null;
        dto.createdAt = favorite.getCreatedAt();
        dto.state     = favorite.isState();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PlaceResponseDTO getPlace() { return place; }
    public void setPlace(PlaceResponseDTO place) { this.place = place; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }
}