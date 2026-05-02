package com.traveling.travel_backend.dto;

import com.traveling.travel_backend.model.User;

public class UserResponseDTO {

    private Long id;
    private String correo;
    private String userName;
    private String birthday;
    private CityResponseDTO city;
    private boolean state;

    public UserResponseDTO() {}

    public static UserResponseDTO fromEntity(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.id       = user.getId();
        dto.correo   = user.getCorreo();
        dto.userName = user.getUserName();
        dto.birthday = user.getBirthday();
        dto.city     = user.getCity() != null ? CityResponseDTO.fromEntity(user.getCity()) : null;
        dto.state    = user.isState();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public CityResponseDTO getCity() { return city; }
    public void setCity(CityResponseDTO city) { this.city = city; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }
}