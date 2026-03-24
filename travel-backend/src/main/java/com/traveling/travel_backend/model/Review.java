package com.traveling.travel_backend.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Relación: Muchas reseñas pertenecen a un solo Usuario
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relación: Muchas reseñas pertenecen a un solo Lugar
    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "score", nullable = true) 
    private Integer score;

    @Column(name = "created_at", insertable = false, updatable = false)
    private String createdAt; // MySQL se encarga del CURRENT_TIMESTAMP

    @Column(name = "state", nullable = false)
    private boolean state = true;

    // 2. RELACIÓN RECURSIVA: Apunta a la misma clase
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true) 
    @JsonIgnore 
    private Review parent;

    // 3. HIJOS (Respuestas): Para traer todas las respuestas de una reseña
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Review> replies;

    public Review() {
    }

    // Constructor para crear reseñas rápidas
    public Review(User user, Place place, String comment, int score) {
        this.user = user;
        this.place = place;
        this.comment = comment;
        this.score = score;
    }

    // GETTERS Y SETTERS
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Place getPlace() { return place; }
    public void setPlace(Place place) { this.place = place; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }
}
