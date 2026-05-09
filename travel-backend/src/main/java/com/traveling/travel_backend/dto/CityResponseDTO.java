package com.traveling.travel_backend.dto;
 
import com.traveling.travel_backend.model.City;
 
public class CityResponseDTO {
 
    private Long id;
    private String name;
    private boolean state;
 
    public CityResponseDTO() {}
 
    public CityResponseDTO(Long id, String name, boolean state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }
 
    public static CityResponseDTO fromEntity(City city) {
        return new CityResponseDTO(
                city.getId(),
                city.getName(),
                city.isState()
        );
    }
 
 
    public Long getId() {
        return id;
    }
 
    public void setId(Long id) {
        this.id = id;
    }
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public boolean isState() {
        return state;
    }
 
    public void setState(boolean state) {
        this.state = state;
    }
}