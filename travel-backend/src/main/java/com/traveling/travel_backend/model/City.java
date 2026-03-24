package com.traveling.travel_backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore; 
import java.util.List;

@Entity
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "state", nullable = false)
    private boolean state = true;

    @OneToMany(mappedBy = "city") 
    @JsonIgnore 
    private List<User> users;

    public City() {}

    
    public City(String name) {
        this.name = name;
    }

    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
}