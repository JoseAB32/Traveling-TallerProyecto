package com.traveling.travel_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Mapeo correcto de columnas
    @Column(name = "user_name", unique = true, nullable = false)
    private String username;
    
    @Column(name = "correo", unique = true, nullable = false)
    private String email;
    
    @Column(name = "pass", nullable = false)
    private String password;
    
    @Column(name = "birthday")
    private String birthday;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "role", nullable = false)
    private String role = "USER";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "state", nullable = false)
    private boolean active = true;
    
    // Constructores
    public User() {
        this.createdAt = LocalDateTime.now();
    }
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = "USER";
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getBirthday() {
        return birthday;
    }
    
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // Métodos de conveniencia (para mantener compatibilidad con código existente)
    public String getUserName() {
        return username;
    }
    
    public void setUserName(String userName) {
        this.username = userName;
    }
    
    public String getCorreo() {
        return email;
    }
    
    public void setCorreo(String correo) {
        this.email = correo;
    }
    
    public String getPass() {
        return password;
    }
    
    public void setPass(String pass) {
        this.password = pass;
    }
    
    public boolean isState() {
        return active;
    }
    
    public void setState(boolean state) {
        this.active = state;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}