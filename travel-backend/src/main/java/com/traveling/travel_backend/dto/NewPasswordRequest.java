package com.traveling.travel_backend.dto;

public class NewPasswordRequest {

    private String token;
    private String password;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}