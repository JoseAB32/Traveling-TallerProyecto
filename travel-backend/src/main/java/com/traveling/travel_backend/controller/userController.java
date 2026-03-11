package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class userController {
    
    @Autowired
    private UserRepository userRepository;
    
    // Almacén simple de tokens (en memoria)
    private Map<String, String> activeTokens = new HashMap<>();
    
    // Endpoint de login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            
            System.out.println("=== INTENTO DE LOGIN ===");
            System.out.println("Username recibido: " + username);
            
            // Validar que llegaron los datos
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body("Faltan credenciales");
            }
            
            // Buscar usuario por username
            User user = userRepository.findByUsername(username).orElse(null);
            
            if (user == null) {
                System.out.println("Usuario no encontrado: " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario o contraseña incorrectos");
            }
            
            // Validar contraseña (en producción deberías encriptar)
            if (!user.getPassword().equals(password)) {
                System.out.println("Contraseña incorrecta para: " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario o contraseña incorrectos");
            }
            
            // Generar token simple
            String token = UUID.randomUUID().toString();
            activeTokens.put(token, user.getUsername());
            
            System.out.println("Login exitoso para: " + username);
            System.out.println("Token generado: " + token);
            
            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("message", "Login exitoso");
            response.put("id", user.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("Error en login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }
    
    // Endpoint de registro
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            System.out.println("=== INTENTO DE REGISTRO ===");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            
            // Validar datos requeridos
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El username es requerido");
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El email es requerido");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La contraseña es requerida");
            }
            
            // Validar si el usuario ya existe
            if (userRepository.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest().body("El nombre de usuario ya está en uso");
            }
            
            // Validar si el email ya existe
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest().body("El email ya está registrado");
            }
            
            // Asignar rol por defecto
            user.setRole("USER");
            
            // Guardar usuario
            User savedUser = userRepository.save(user);
            System.out.println("Usuario registrado con ID: " + savedUser.getId());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Usuario registrado exitosamente");
            response.put("id", savedUser.getId().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("Error en registro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }
    
    // Endpoint para verificar token
    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token no proporcionado");
            }
            
            String token = authHeader.substring(7);
            
            if (activeTokens.containsKey(token)) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("username", activeTokens.get(token));
                
                // Obtener datos completos del usuario
                User user = userRepository.findByUsername(activeTokens.get(token)).orElse(null);
                if (user != null) {
                    response.put("email", user.getEmail());
                    response.put("role", user.getRole());
                }
                
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Token inválido");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error verificando token");
        }
    }
    
    // Endpoint de logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            activeTokens.remove(token);
            System.out.println("Token eliminado: " + token);
        }
        return ResponseEntity.ok("Sesión cerrada");
    }
    
    // Endpoint para probar la conexión
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Backend funcionando correctamente");
        response.put("database", "Conectada a Clever Cloud");
        response.put("time", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}