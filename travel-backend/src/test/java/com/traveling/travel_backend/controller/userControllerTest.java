package com.traveling.travel_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.dto.LoginRequest;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.CityRepository;
import com.traveling.travel_backend.repository.UserRepository;
import com.traveling.travel_backend.security.JwtService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(userController.class)
@AutoConfigureMockMvc(addFilters = false)
public class userControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CityRepository cityRepository;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("Debe retornar 200 y usuario registrado/guardado correctamente.")
    void SignUpSuccessfully() throws Exception {
        City city = new City();
        city.setId(3L);
        city.setName("Cochabamba");

        City cityId = new City();
        cityId.setId(3L);

        User user = new User();
        user.setId(1L);
        user.setUserName("Pablo");
        user.setPass("1234");
        user.setCorreo("pablo@test.com");
        user.setCity(cityId);
        user.setState(true);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUserName("Pablo");
        savedUser.setPass("1234");
        savedUser.setCorreo("pablo@test.com");
        savedUser.setCity(city);
        savedUser.setState(true);

        when(userRepository.findByUserNameAndStateTrue("Pablo")).thenReturn(Optional.empty());
        when(cityRepository.findById(cityId.getId())).thenReturn(Optional.of(city));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.userName").value("Pablo"))
        .andExpect(jsonPath("$.pass").value("1234"))
        .andExpect(jsonPath("$.correo").value("pablo@test.com"))
        .andExpect(jsonPath("$.city.id").value(3))
        .andExpect(jsonPath("$.city.name").value("Cochabamba"))
        .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el userName está vacío.")
    void SignUpWhenUserNameIsEmpty() throws Exception {
        City cityId = new City();
        cityId.setId(3L);

        User user = new User();
        user.setId(1L);
        user.setUserName(" ");
        user.setPass("1234");
        user.setCorreo("pablo@test.com");
        user.setCity(cityId);
        user.setState(true);

        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("El nombre de usuario es requerido."));
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el usuario existe y esta activo.")
    void SignUpWhenUserIsActive() throws Exception {
        City cityId = new City();
        cityId.setId(3L);

        User user = new User();
        user.setId(1L);
        user.setUserName("Pablo");
        user.setPass("1234");
        user.setCorreo("pablo@test.com");
        user.setCity(cityId);
        user.setState(true);

        City otherCityId = new City();
        otherCityId.setId(7L);

        User otherUser = new User();
        otherUser.setId(5L);
        otherUser.setUserName("Pablo");
        otherUser.setPass("4567");
        otherUser.setCorreo("pablito@test.com");
        otherUser.setCity(otherCityId);
        otherUser.setState(true);

        when(userRepository.findByUserNameAndStateTrue("Pablo")).thenReturn(Optional.of(otherUser));

        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("El nombre de usuario ya está en uso."));
    }

    @Test
    @DisplayName("Debe retornar 200 y token cuando el login es correcto")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserName("Pablo");
        request.setPass("1234");

        User user = new User();
        user.setId(1L);
        user.setUserName("Pablo");
        user.setPass("1234");
        user.setCorreo("pablo@test.com");
        user.setState(true);

        when(userRepository.findByUserName("Pablo")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("Pablo", 1L)).thenReturn("token-prueba");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-prueba"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userName").value("Pablo"))
                .andExpect(jsonPath("$.correo").value("pablo@test.com"))
                .andExpect(jsonPath("$.message").value("Login exitoso"));
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el username está vacío")
    void shouldReturn400WhenUsernameIsEmpty() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserName("   ");
        request.setPass("1234");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando la contraseña está vacía")
    void shouldReturn400WhenPasswordIsEmpty() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserName("Pablo");
        request.setPass("   ");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debe retornar 401 cuando el usuario no existe")
    void shouldReturn401WhenUserDoesNotExist() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserName("Pablo");
        request.setPass("1234");

        when(userRepository.findByUserName("Pablo")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Debe retornar 401 cuando el usuario está inactivo")
    void shouldReturn401WhenUserIsInactive() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserName("Pablo");
        request.setPass("1234");

        User user = new User();
        user.setId(1L);
        user.setUserName("Pablo");
        user.setPass("1234");
        user.setCorreo("pablo@test.com");
        user.setState(false);

        when(userRepository.findByUserName("Pablo")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Debe retornar 401 cuando la contraseña es incorrecta")
    void shouldReturn401WhenPasswordIsIncorrect() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserName("Pablo");
        request.setPass("9999");

        User user = new User();
        user.setId(1L);
        user.setUserName("Pablo");
        user.setPass("1234");
        user.setCorreo("pablo@test.com");
        user.setState(true);

        when(userRepository.findByUserName("Pablo")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
