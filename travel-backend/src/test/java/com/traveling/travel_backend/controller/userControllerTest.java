package com.traveling.travel_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.dto.LoginRequest;
import com.traveling.travel_backend.dto.LoginResponse;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.CityRepository;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.UserRepository;
import com.traveling.travel_backend.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
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
    private LogRepository logRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        when(logRepository.save(any())).thenReturn(null);
    }

    @Test
    @DisplayName("Debe retornar lista de todos los usuarios")
    void getAllUsersShouldReturnListOfUsers() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setUserName("Pablo");
        
        User user2 = new User();
        user2.setId(2L);
        user2.setUserName("Ana");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("Pablo"))
                .andExpect(jsonPath("$[1].userName").value("Ana"));
    }

    @Test
    @DisplayName("Debe crear usuario exitosamente cuando los datos son válidos")
    void createUserSuccessfully() throws Exception {
        City city = new City();
        city.setId(3L);
        city.setName("Cochabamba");

        User user = new User();
        user.setUserName("Pablo");
        user.setPass("12345678");
        user.setCorreo("pablo@test.com");
        user.setState(true);
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUserName("Pablo");
        savedUser.setPass("encodedPassword");
        savedUser.setCorreo("pablo@test.com");
        savedUser.setCity(city);
        savedUser.setState(true);

        when(userRepository.findByUserNameAndStateTrue("Pablo")).thenReturn(Optional.empty());
        when(cityRepository.findById(any())).thenReturn(Optional.of(city));
        when(passwordEncoder.encode("12345678")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Pablo"))
                .andExpect(jsonPath("$.correo").value("pablo@test.com"));
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el userName está vacío")
    void createUserWhenUserNameIsEmpty() throws Exception {
        User user = new User();
        user.setUserName(" ");
        user.setPass("12345678");
        user.setCorreo("pablo@test.com");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debe retornar 200 y token cuando el login es correcto")
    void loginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserName("Pablo");
        request.setPass("12345678");

        User user = new User();
        user.setId(1L);
        user.setUserName("Pablo");
        user.setPass("encodedPassword");
        user.setCorreo("pablo@test.com");
        user.setState(true);

        when(userRepository.findByUserName("Pablo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("12345678", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("Pablo", 1L)).thenReturn("token-prueba-123");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-prueba-123"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.userName").value("Pablo"))
                .andExpect(jsonPath("$.correo").value("pablo@test.com"));
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el username está vacío en login")
    void loginWhenUsernameIsEmpty() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserName("   ");
        request.setPass("12345678");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando la contraseña está vacía en login")
    void loginWhenPasswordIsEmpty() throws Exception {
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
    void loginWhenUserDoesNotExist() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserName("UsuarioInexistente");
        request.setPass("12345678");

        when(userRepository.findByUserName("UsuarioInexistente")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Debe retornar 401 cuando el usuario está inactivo")
    void loginWhenUserIsInactive() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserName("Pablo");
        request.setPass("12345678");

        User user = new User();
        user.setId(1L);
        user.setUserName("Pablo");
        user.setPass("encodedPassword");
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
    void loginWhenPasswordIsIncorrect() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserName("Pablo");
        request.setPass("contraseñaIncorrecta");

        User user = new User();
        user.setId(1L);
        user.setUserName("Pablo");
        user.setPass("encodedPassword");
        user.setCorreo("pablo@test.com");
        user.setState(true);

        when(userRepository.findByUserName("Pablo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("contraseñaIncorrecta", "encodedPassword")).thenReturn(false);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}