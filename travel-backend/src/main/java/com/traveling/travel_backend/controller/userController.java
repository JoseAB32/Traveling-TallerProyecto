package com.traveling.travel_backend.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@RestController
@RequestMapping("api/")
@CrossOrigin(origins = "http://localhost:4200")
public class userController {
    
    private static final Logger logger = LoggerFactory.getLogger(userController.class);
    
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
        logger.info("👤 [USERS] Recibida solicitud para obtener la lista de todos los usuarios.");

        List<User> users = userRepository.findAll();
        
        logger.debug("👤 [USERS] Se recuperaron {} usuarios de la base de datos.", users.size());
        
        return users;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        logger.info("👤 [USERS] Recibida solicitud para crear un nuevo usuario. Nombre de usuario: {}", user.getUserName());

        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            logger.warn("❌ [USERS] Creación de usuario rechazada: El nombre de usuario está vacío o es nulo.");

            return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("El nombre de usuario es requerido."));
        }

        String trimmedUserName = user.getUserName().trim();
        Optional<User> existingUser = userRepository.findByUserNameAndStateTrue(trimmedUserName);

        if (existingUser.isPresent()) {
            logger.warn("❌ [USERS] Creación de usuario rechazada: El nombre de usuario '{}' ya se encuentra en uso.", trimmedUserName);

            return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("El nombre de usuario ya está en uso."));
        }
    
        if (user.getCity() != null) {
            logger.debug("👤 [USERS] Buscando información de la ciudad con ID: {}", user.getCity().getId());

            City realCity = cityRepository.findById(user.getCity().getId()).orElse(null);
            user.setCity(realCity); 
        }
        
        User savedUser = userRepository.save(user);
        logger.info("👤 [USERS] Usuario '{}' creado exitosamente con el ID: {}", savedUser.getUserName(), savedUser.getId());

        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest credentials) {
            String userName = credentials.getUserName();
            String pass = credentials.getPass();
            
            logger.info("👤 [USERS] Intento de inicio de sesión para el usuario: {}", userName != null ? userName : "DESCONOCIDO");
            
            if (userName == null || userName.trim().isEmpty()) {
                logger.warn("❌ [USERS] Login rechazado: El nombre de usuario es nulo o está vacío.");

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario es requerido.");
            }
            if (pass == null || pass.trim().isEmpty()) {
                logger.warn("❌ [USERS] Login rechazado para el usuario '{}': La contraseña está vacía.", userName);

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es requerida.");
            }

            logger.debug("👤 [USERS] Buscando al usuario '{}' en la base de datos.", userName);

            Optional<User> optionalUser = userRepository.findByUserName(userName);

            if(optionalUser.isEmpty()) {
                logger.warn("❌ [USERS] Login fallido: No se encontró ningún usuario con el nombre '{}'.", userName);

                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
            }

            User user = optionalUser.get(); //Obtenemos el valor que guarda Optional.
            
            if (!user.isState()) {
                logger.warn("❌ [USERS] Login fallido: El usuario '{}' (ID: {}) intentó acceder pero está inactivo.", user.getUserName(), user.getId());

                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario desactivado.");
            }
            
            if (!user.getPass().equals(pass)) {
                logger.warn("❌ [USERS] Login fallido: Contraseña incorrecta para el usuario '{}'.", user.getUserName());

                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
            }

            logger.debug("👤 [USERS] Generando token JWT para el usuario '{}'.", user.getUserName());

            String token = jwtService.generateToken(user.getUserName(), user.getId());

            LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUserName(),
                user.getCorreo(),
                "Login exitoso"
            );

            logger.info("👤 [USERS] Inicio de sesión exitoso para el usuario '{}' (ID: {}).", user.getUserName(), user.getId());

            return ResponseEntity.ok(response);
    }
}