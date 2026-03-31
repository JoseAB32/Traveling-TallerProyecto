package com.traveling.travel_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore; 
import jakarta.persistence.*;

@Entity
@Table(name = "favorites")
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "created_at", insertable = false, updatable = false)
    private String createdAt; 

    @Column(name = "state", nullable = false)
    private boolean state = true;

    public Favorite() {
    }

    public Favorite(User user, Place place) {
        this.user = user;
        this.place = place;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Place getPlace() { return place; }
    public void setPlace(Place place) { this.place = place; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }
}