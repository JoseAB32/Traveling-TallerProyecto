package com.traveling.travel_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "pass", nullable = false)
    private String pass;

    @Column(name = "birthday")
    private String birthday; 

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @ManyToOne
    @JoinColumn(name = "city_id") 
    private City city;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "state", nullable = false)
    private boolean state = true;

    public User() {}

    // Constructor actualizado
    public User(String correo, String userName, String pass, String birthday, City city) {
        this.correo = correo;
        this.userName = userName;
        this.pass = pass;
        this.birthday = birthday;
        this.city = city;
    }

    // Getters y Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String url) { this.profilePictureUrl = url; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}