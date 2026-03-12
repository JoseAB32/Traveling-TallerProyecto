package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class userController {
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String userName = credentials.get("userName");
            String pass = credentials.get("pass");
            
            System.out.println("\n=== INTENTO DE LOGIN ===");
            System.out.println("Usuario recibido: " + userName);
            
            if (userName == null || userName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El usuario es requerido"));
            }
            if (pass == null || pass.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es requerida"));
            }
            
            // Buscar usuario por userName
            User user = userRepository.findByUserName(userName).orElse(null);
            
            if (user == null) {
                System.out.println("❌ Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario o contraseña incorrectos"));
            }
            
            if (!user.isState()) {
                System.out.println("❌ Usuario inactivo");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario desactivado"));
            }
            
            if (!user.getPass().equals(pass)) {
                System.out.println("❌ Contraseña incorrecta");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario o contraseña incorrectos"));
            }
            
            System.out.println("✅ Login exitoso para: " + userName);
            
            // Respuesta SIN JWT - solo datos básicos
            Map<String, Object> response = new HashMap<>();
            response.put("userName", user.getUserName());
            response.put("correo", user.getCorreo());
            response.put("id", user.getId());
            response.put("message", "Login exitoso");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Error en login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        try {
            String userName = userData.get("userName");
            String correo = userData.get("correo");
            String pass = userData.get("pass");
            
            if (userName == null || userName.trim().isEmpty() ||
                correo == null || correo.trim().isEmpty() ||
                pass == null || pass.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Todos los campos son requeridos"));
            }
            
            if (userRepository.existsByUserName(userName)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya existe"));
            }
            if (userRepository.existsByCorreo(correo)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El correo ya está registrado"));
            }
            
            User newUser = new User();
            newUser.setUserName(userName);
            newUser.setCorreo(correo);
            newUser.setPass(pass);
            newUser.setState(true);
            
            if (userData.containsKey("birthday")) {
                newUser.setBirthday(userData.get("birthday"));
            }
            if (userData.containsKey("city")) {
                newUser.setCity(userData.get("city"));
            }
            
            User savedUser = userRepository.save(newUser);
            System.out.println("✅ Usuario registrado: " + savedUser.getUserName());
            
            return ResponseEntity.ok(Map.of(
                "message", "Usuario registrado exitosamente",
                "id", String.valueOf(savedUser.getId())
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
            "message", "✅ Backend funcionando correctamente",
            "database", "Conectada a Clever Cloud",
            "time", java.time.LocalDateTime.now().toString(),
            "status", "OK"
        ));
    }
    
    @GetMapping("/debug-users")
    public ResponseEntity<?> debugUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}