package com.traveling.travel_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Cambiado de long a Long (objeto)

    @Column(name = "correo", nullable = false)
    private String correo;  // Cambiado de Correo a correo

    @Column(name = "user_name", nullable = false)
    private String userName;  // Cambiado de UserName a userName

    @Column(name = "pass", nullable = false)
    private String pass;  // Cambiado de Pass a pass

    @Column(name = "birthday", nullable = true)
    private String birthday;  // Cambiado de Birthday a birthday

    @Column(name = "city", nullable = true)
    private String city;  // Cambiado de City a city

    @Column(name = "state", nullable = false)
    private boolean state = true;  // Cambiado de State a state

    public User() {}

    public User(String correo, String userName, String pass, String birthday, String city) {
        this.correo = correo;
        this.userName = userName;
        this.pass = pass;
        this.birthday = birthday;
        this.city = city;
    }

    // Getters y Setters actualizados
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }
}