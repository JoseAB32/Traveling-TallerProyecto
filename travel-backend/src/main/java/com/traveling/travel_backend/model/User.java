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
    private String Correo;

    @Column(name = "user_name", nullable = false)
    private String UserName;

    @Column(name = "pass", nullable = false)
    private String Pass;

    @Column(name = "birthday", nullable = true)
    private String Birthday;

    @Column(name = "city", nullable = true)
    private String City;

    @Column(name = "state", nullable = false)
    private boolean State = true; //Inicia siempre como true para manejar delete logico

    public User() {

    }

    public User(String correo, String userName, String pass, String birthday, String city) {
        super();
        this.Correo = correo;
        this.UserName = userName;
        this.Pass = pass;
        this.Birthday = birthday;
        this.City = city;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCorreo() {
        return Correo;
    }

    public void setCorreo(String correo) {
        this.Correo = correo;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        this.UserName = userName;
    }

    public String getPass() {
        return Pass;
    }

    public void setPass(String pass) {
        this.Pass = pass;
    }

    public String getBirthday() {
        return Birthday;
    }

    public void setBirthday(String birthday) {
        this.Birthday = birthday;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        this.City = city;
    }

    public boolean isState() {
        return State;
    }

    public void setState(boolean state) {
        this.State = state;
    }
}
