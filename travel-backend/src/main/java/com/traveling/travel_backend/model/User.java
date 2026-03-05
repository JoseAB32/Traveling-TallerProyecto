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
}
