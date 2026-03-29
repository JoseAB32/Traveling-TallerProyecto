package com.traveling.travel_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "places")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "price")
    private Double price;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "place_type", length = 50)
    private String placeType;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "is_event")
    private boolean isEvent = false;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Column(name = "state", nullable = false)
    private boolean state = true;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getPlaceType() { return placeType; }
    public void setPlaceType(String placeType) { this.placeType = placeType; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public boolean isEvent() { return isEvent; }
    public void setEvent(boolean event) { isEvent = event; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }
}