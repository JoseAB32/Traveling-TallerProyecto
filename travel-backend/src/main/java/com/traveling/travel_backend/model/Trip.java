package com.traveling.travel_backend.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Relación: Muchos viajes pertenecen a un solo Usuario
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "start_date")
    private String startDate; 

    @Column(name = "end_date")
    private String endDate;   

    @Column(name = "state", nullable = false)
    private boolean state = true;

    
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    @JsonIgnore 
    private List<TripItem> items;

    public Trip() {
    }


    public Trip(User user, String name, String startDate, String endDate) {
        this.user = user;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }

    public List<TripItem> getItems() { return items; }
    public void setItems(List<TripItem> items) { this.items = items; }
}