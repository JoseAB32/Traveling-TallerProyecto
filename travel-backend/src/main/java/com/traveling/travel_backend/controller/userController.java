package com.traveling.travel_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.traveling.travel_backend.dto.LoginRequest;
import com.traveling.travel_backend.dto.LoginResponse;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.UserRepository;
import com.traveling.travel_backend.security.JwtService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/")
@CrossOrigin(origins = "http://localhost:4200")
public class userController {

    @Autowired
    private UserRepository userRepository;

    private final JwtService jwtService;

    public userController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // @PostMapping("/login")
    // public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
    //     try {
    //         String userName = credentials.get("userName");
    //         String pass = credentials.get("pass");
            
    //         // System.out.println("\n=== INTENTO DE LOGIN ===");
    //         // System.out.println("Usuario recibido: " + userName);
            
    //         if (userName == null || userName.trim().isEmpty()) {
    //             return ResponseEntity.badRequest().body(Map.of("error", "El usuario es requerido"));
    //         }
    //         if (pass == null || pass.trim().isEmpty()) {
    //             return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es requerida"));
    //         }
            
    //         // Buscar usuario por userName
    //         User user = userRepository.findByUserName(userName).orElse(null);
            
    //         if (user == null) {
    //             // System.out.println("❌ Usuario no encontrado");
    //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    //                 .body(Map.of("error", "Usuario o contraseña incorrectos"));
    //         }
            
    //         if (!user.isState()) {
    //             // System.out.println("❌ Usuario inactivo");
    //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    //                 .body(Map.of("error", "Usuario desactivado"));
    //         }
            
    //         if (!user.getPass().equals(pass)) {
    //             // System.out.println("❌ Contraseña incorrecta");
    //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    //                 .body(Map.of("error", "Usuario o contraseña incorrectos"));
    //         }
            
    //         // System.out.println("✅ Login exitoso para: " + userName);
            
    //         // Respuesta SIN JWT - solo datos básicos
    //         Map<String, Object> response = new HashMap<>();
    //         response.put("userName", user.getUserName());
    //         response.put("correo", user.getCorreo());
    //         response.put("id", user.getId());
    //         response.put("message", "Login exitoso");
            
    //         return ResponseEntity.ok(response);
            
    //     } catch (Exception e) {
    //         // System.out.println("❌ Error en login: " + e.getMessage());
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body(Map.of("error", "Error interno del servidor"));
    //     }
    // }

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