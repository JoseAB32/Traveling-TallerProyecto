package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class userController {
    
    @Autowired
    private UserRepository userRepository;
    
    // Almacén de tokens activos (en memoria)
    private Map<String, String> activeTokens = new HashMap<>();
    
    /**
     * ENDPOINT DE LOGIN
     * Recibe credenciales y valida contra la base de datos
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            
            System.out.println("\n=== INTENTO DE LOGIN ===");
            System.out.println("📝 Username recibido: " + username);
            System.out.println("🔍 URL: /api/login");
            
            // Validar que llegaron los datos
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El usuario es requerido"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es requerida"));
            }
            
            // Buscar usuario por username
            System.out.println("🔍 Buscando usuario en BD: " + username);
            User user = userRepository.findByUsername(username).orElse(null);
            
            if (user == null) {
                System.out.println("❌ Usuario no encontrado: " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario o contraseña incorrectos"));
            }
            
            System.out.println("✅ Usuario encontrado en BD");
            
            // Verificar si el usuario está activo
            if (!user.isActive()) {
                System.out.println("❌ Usuario inactivo: " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario desactivado"));
            }
            
            // Validar contraseña
            if (!user.getPassword().equals(password)) {
                System.out.println("❌ Contraseña incorrecta para: " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario o contraseña incorrectos"));
            }
            
            // Generar token único
            String token = UUID.randomUUID().toString();
            activeTokens.put(token, user.getUsername());
            
            System.out.println("✅ Login exitoso para: " + username);
            
            // Preparar respuesta exitosa
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("id", user.getId());
            response.put("message", "Login exitoso");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Error en login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * ENDPOINT DE REGISTRO
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        try {
            System.out.println("\n=== INTENTO DE REGISTRO ===");
            
            String username = userData.get("username");
            String email = userData.get("email");
            String password = userData.get("password");
            String birthday = userData.get("birthday");
            String city = userData.get("city");
            
            // Validaciones
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El username es requerido"));
            }
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El email es requerido"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es requerida"));
            }
            
            // Verificar si el usuario ya existe
            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya está en uso"));
            }
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
            }
            
            // Crear nuevo usuario
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setBirthday(birthday);
            newUser.setCity(city);
            newUser.setRole("USER");
            newUser.setActive(true);
            
            User savedUser = userRepository.save(newUser);
            System.out.println("✅ Usuario registrado con ID: " + savedUser.getId());
            
            return ResponseEntity.ok(Map.of(
                "message", "Usuario registrado exitosamente",
                "id", savedUser.getId().toString()
            ));
            
        } catch (Exception e) {
            System.out.println("❌ Error en registro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * ENDPOINT DE PRUEBA
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        System.out.println("\n=== ENDPOINT TEST ACCEDIDO ===");
        System.out.println("🔍 URL: /api/test");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "✅ Backend funcionando correctamente");
        response.put("database", "Conectada a Clever Cloud");
        response.put("time", java.time.LocalDateTime.now().toString());
        response.put("status", "OK");
        response.put("endpoints", "/api/login, /api/register, /api/test, /api/debug-users");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * ENDPOINT PARA DEBUG
     */
    @GetMapping("/debug-users")
    public ResponseEntity<?> debugUsers() {
        System.out.println("\n=== DEBUG USERS ACCEDIDO ===");
        
        List<User> users = userRepository.findAll();
        
        List<Map<String, Object>> userList = users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("role", user.getRole());
            userMap.put("active", user.isActive());
            userMap.put("birthday", user.getBirthday());
            userMap.put("city", user.getCity());
            return userMap;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", users.size());
        response.put("users", userList);
        response.put("activeTokens", activeTokens.size());
        
        System.out.println("📊 Usuarios encontrados: " + users.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * ENDPOINT PARA VERIFICAR TOKEN
     */
    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Token no proporcionado"));
            }
            
            String token = authHeader.substring(7);
            
            if (activeTokens.containsKey(token)) {
                String username = activeTokens.get(token);
                User user = userRepository.findByUsername(username).orElse(null);
                
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("username", username);
                
                if (user != null) {
                    response.put("email", user.getEmail());
                    response.put("role", user.getRole());
                    response.put("id", user.getId());
                }
                
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valid", false, "error", "Token inválido"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("valid", false, "error", "Error verificando token"));
        }
    }
    
    /**
     * ENDPOINT DE LOGOUT
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            activeTokens.remove(token);
        }
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada exitosamente"));
    }
}