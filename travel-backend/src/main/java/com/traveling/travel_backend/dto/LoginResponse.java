package com.traveling.travel_backend.dto;

public class LoginResponse {
    private String token;
    private String type;
    private long id;
    private String userName;
    private String correo;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(String token, String type, Long id, String userName, String correo, String message) {
        this.token = token;
        this.type = type;
        this.id = id;
        this.userName = userName;
        this.correo = correo;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
