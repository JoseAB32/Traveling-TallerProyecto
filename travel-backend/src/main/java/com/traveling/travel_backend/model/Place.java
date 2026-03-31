package com.traveling.travel_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "places")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String description;

    private String address;

    private double rating = 5.0;

    private double price;

    private double latitude;

    private double longitude;

    @Column(name = "place_type")
    @JsonProperty("place_type") // 🔥 CLAVE
    private String placeType;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "is_event")
    @JsonProperty("is_event") // 🔥 CLAVE
    private boolean isEvent = false;

    @Column(name = "start_date")
    @JsonProperty("start_date") // 🔥 CLAVE
    private String startDate;

    @Column(name = "end_date")
    @JsonProperty("end_date") // 🔥 CLAVE
    private String endDate;

    @Column(name = "image_url")
    private String imageUrl;

    private boolean state = true;

    public Place() {}

    // GETTERS

    public long getId() { return id; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getAddress() { return address; }

    public double getRating() { return rating; }

    public double getPrice() { return price; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public String getPlaceType() { return placeType; }

    public City getCity() { return city; }

    public boolean isEvent() { return isEvent; }

    public String getStartDate() { return startDate; }

    public String getEndDate() { return endDate; }

    public String getImageUrl() { return imageUrl; }

    public boolean isState() { return state; }

    // SETTERS

    public void setId(long id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setDescription(String description) { this.description = description; }

    public void setAddress(String address) { this.address = address; }

    public void setRating(double rating) { this.rating = rating; }

    public void setPrice(double price) { this.price = price; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    public void setPlaceType(String placeType) { this.placeType = placeType; }

    public void setCity(City city) { this.city = city; }

    public void setEvent(boolean event) { isEvent = event; }

    public void setStartDate(String startDate) { this.startDate = startDate; }

    public void setEndDate(String endDate) { this.endDate = endDate; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setState(boolean state) { this.state = state; }
}