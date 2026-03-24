package com.traveling.travel_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "places")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address", length = 255)
    private String address;

    // El default 5.0 que configuramos en el Trigger y la BD
    @Column(name = "rating")
    private double rating = 5.0;

    @Column(name = "price")
    private double price;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "place_type")
    private String place_type;

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

    public Place() {
    }

    // Constructor completo para facilitar inserts desde el código
    public Place(String name, String description, String address, City city, String place_type, boolean isEvent, String imageUrl, double price, double latitude, double longitude) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.city = city;
        this.isEvent = isEvent;
        this.imageUrl = imageUrl;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.place_type = place_type;
    }

    // GETTERS Y SETTERS
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

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

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getPlaceType() { return place_type; }
    public void setPlaceType(String place_type) { this.place_type = place_type; }
}