package com.traveling.travel_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "trip_items")
public class TripItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "visit_order")
    private int visitOrder; 

    @Column(name = "state", nullable = false)
    private boolean state = true;

    public TripItem() {
    }

    public TripItem(Trip trip, Place place, int visitOrder) {
        this.trip = trip;
        this.place = place;
        this.visitOrder = visitOrder;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public Place getPlace() { return place; }
    public void setPlace(Place place) { this.place = place; }

    public int getVisitOrder() { return visitOrder; }
    public void setVisitOrder(int visitOrder) { this.visitOrder = visitOrder; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }
}