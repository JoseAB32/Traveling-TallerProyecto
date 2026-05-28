package com.traveling.travel_backend.dto;

public class UpdateProfileRequestDTO {

    private String userName;
    private String correo;
    private String birthday;
    private Long cityId;

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
}