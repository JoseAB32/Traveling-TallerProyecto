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
    private long id;

    @Column(name = "correo", nullable = false)
    private String correo;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "pass", nullable = false)
    private String pass;

    @Column(name = "birthday", nullable = true)
    private String birthday;

    @Column(name = "city", nullable = true)
    private String city;

    @Column(name = "state", nullable = false)
    private boolean state = true; //Inicia siempre como true para manejar delete logico

    public User() {

    }

    public User(String correo, String userName, String pass, String birthday, String city) {
        super();
        this.correo = correo;
        this.userName = userName;
        this.pass = pass;
        this.birthday = birthday;
        this.city = city;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
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

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
