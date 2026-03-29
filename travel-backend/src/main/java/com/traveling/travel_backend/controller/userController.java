package com.traveling.travel_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.traveling.travel_backend.dto.ErrorResponse;
import com.traveling.travel_backend.dto.LoginRequest;
import com.traveling.travel_backend.dto.LoginResponse;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.CityRepository;
import com.traveling.travel_backend.repository.UserRepository;
import com.traveling.travel_backend.security.JwtService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/")
@CrossOrigin(origins = "http://localhost:4200")
public class userController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityRepository cityRepository;

    private final JwtService jwtService;

    public userController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("El nombre de usuario es requerido."));
        }

        Optional<User> existingUser = userRepository.findByUserNameAndStateTrue(user.getUserName().trim());

        if (existingUser.isPresent()) {
            return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("El nombre de usuario ya está en uso."));
        }
    
        if (user.getCity() != null) {
            City realCity = cityRepository.findById(user.getCity().getId()).orElse(null);
            user.setCity(realCity); 
        }
        
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest credentials) {
            String userName = credentials.getUserName();
            String pass = credentials.getPass();
            
            if (userName == null || userName.trim().isEmpty()) {
                // return ResponseEntity.badRequest().body(Map.of("error", "El usuario es requerido"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario es requerido.");
            }
            if (pass == null || pass.trim().isEmpty()) {
                // return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es requerida"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es requerida.");
            }

            Optional<User> optionalUser = userRepository.findByUserName(userName);

            if(optionalUser.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
            }

            User user = optionalUser.get(); //Obtenemos el valor que guarda Optional.
            
            if (!user.isState()) {
                // System.out.println("❌ Usuario inactivo");
                // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario desactivado"));
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario desactivado.");
            }
            
            if (!user.getPass().equals(pass)) {
                // System.out.println("❌ Contraseña incorrecta");
                // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario o contraseña incorrectos"));
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
            }

            String token = jwtService.generateToken(user.getUserName(), user.getId());

            LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUserName(),
                user.getCorreo(),
                "Login exitoso"
            );

            return ResponseEntity.ok(response);
    }
}