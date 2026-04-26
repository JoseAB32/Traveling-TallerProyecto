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

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.ErrorResponse;
import com.traveling.travel_backend.dto.LoginRequest;
import com.traveling.travel_backend.dto.LoginResponse;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.CityRepository;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.UserRepository;
import com.traveling.travel_backend.security.JwtService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // 🔥 Agregar import

@RestController
@RequestMapping(AppConstants.API_BASE_PATH)
@CrossOrigin(origins = {AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY})
public class userController {
    
    private static final Logger logger = LoggerFactory.getLogger(userController.class);
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private LogRepository logRepository;

    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder; // 🔥 Agregar encoder

    public userController(JwtService jwtService, BCryptPasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(AppConstants.USERS_ENDPOINT)
    public List<User> getAllUsers() {
        logger.info(AppConstants.PREFIX_USER + " [" + AppConstants.LOG_USERS + "] Recibida solicitud para obtener la lista de todos los usuarios.");
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_INFO, "Recibida solicitud para obtener la lista de todos los usuarios.", null));

        List<User> users = userRepository.findAll();
        
        logger.debug(AppConstants.PREFIX_USER + " [" + AppConstants.LOG_USERS + "] Se recuperaron {} usuarios de la base de datos.", users.size());
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_DEBUG, "Se recuperaron " + users.size() + " usuarios de la base de datos.", null));
        
        return users;
    }

    @PostMapping(AppConstants.USERS_ENDPOINT)
    public ResponseEntity<?> createUser(@RequestBody User user) {
        logger.info(AppConstants.PREFIX_USER + " [" + AppConstants.LOG_USERS + "] Recibida solicitud para crear un nuevo usuario. Nombre de usuario: {}", user.getUserName());
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_INFO, "Recibida solicitud para crear un nuevo usuario. Nombre de usuario: " + user.getUserName(), null));

        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            logger.warn(AppConstants.PREFIX_ERROR + " [" + AppConstants.LOG_USERS + "] Creación de usuario rechazada: El nombre de usuario está vacío o es nulo.");
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN, "Creación de usuario rechazada: El nombre de usuario está vacío o es nulo.", null));
            return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(AppConstants.USER_REQUIRED));
        }

        String trimmedUserName = user.getUserName().trim();
        Optional<User> existingUser = userRepository.findByUserNameAndStateTrue(trimmedUserName);

        if (existingUser.isPresent()) {
            logger.warn(AppConstants.PREFIX_ERROR + " [" + AppConstants.LOG_USERS + "] Creación de usuario rechazada: El nombre de usuario '{}' ya se encuentra en uso.", trimmedUserName);
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN, "Creación de usuario rechazada: El nombre de usuario '" + trimmedUserName + "' ya se encuentra en uso.", null));

            return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(AppConstants.USER_ALREADY_IN_USE));
        }
    
        if (user.getCity() != null) {
            logger.debug(AppConstants.PREFIX_USER + " [" + AppConstants.LOG_USERS + "] Buscando información de la ciudad con ID: {}", user.getCity().getId());
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_DEBUG, "Buscando información de la ciudad con ID: " + user.getCity().getId(), null));

            City realCity = cityRepository.findById(user.getCity().getId()).orElse(null);
            user.setCity(realCity); 
        }
        
        // 🔥 Encriptar la contraseña antes de guardar
        if (user.getPass() != null && !user.getPass().isEmpty()) {
            user.setPass(passwordEncoder.encode(user.getPass()));
        }
        
        User savedUser = userRepository.save(user);
        logger.info("👤 [USERS] Usuario '{}' creado exitosamente con el ID: {}", savedUser.getUserName(), savedUser.getId());
        logRepository.save(new LogEntity("USERS", "INFO", "Usuario '" + savedUser.getUserName() + "' creado exitosamente con el ID: " + savedUser.getId(), null));

        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest credentials) {
            String userName = credentials.getUserName();
            String pass = credentials.getPass();
            
            logger.info("👤 [USERS] Intento de inicio de sesión para el usuario: {}", userName != null ? userName : "DESCONOCIDO");
            logRepository.save(new LogEntity("USERS", "INFO", "Intento de inicio de sesión para el usuario: " + (userName != null ? userName : "DESCONOCIDO"), null));
            
            if (userName == null || userName.trim().isEmpty()) {
                logger.warn("❌ [USERS] Login rechazado: El nombre de usuario es nulo o está vacío.");
                logRepository.save(new LogEntity("USERS", "WARN", "Login rechazado: El nombre de usuario es nulo o está vacío.", null));

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario es requerido.");
            }
            if (pass == null || pass.trim().isEmpty()) {
                logger.warn("❌ [USERS] Login rechazado para el usuario '{}': La contraseña está vacía.", userName);
                logRepository.save(new LogEntity("USERS", "WARN", "Login rechazado para el usuario '" + userName + "': La contraseña está vacía.", null));

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es requerida.");
            }

            logger.debug("👤 [USERS] Buscando al usuario '{}' en la base de datos.", userName);
            logRepository.save(new LogEntity("USERS", "DEBUG", "Buscando al usuario '" + userName + "' en la base de datos.", null));

            Optional<User> optionalUser = userRepository.findByUserName(userName);

            if(optionalUser.isEmpty()) {
                logger.warn("❌ [USERS] Login fallido: No se encontró ningún usuario con el nombre '{}'.", userName);
                logRepository.save(new LogEntity("USERS", "WARN", "Login fallido: No se encontró ningún usuario con el nombre '" + userName + "'.", null));

                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
            }

            User user = optionalUser.get();
            
            if (!user.isState()) {
                logger.warn("❌ [USERS] Login fallido: El usuario '{}' (ID: {}) intentó acceder pero está inactivo.", user.getUserName(), user.getId());
                logRepository.save(new LogEntity("USERS", "WARN", "Login fallido: El usuario '" + user.getUserName() + "' (ID: " + user.getId() + ") intentó acceder pero está inactivo.", user.getId()));

                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario desactivado.");
            }
            
            // 🔥 CAMBIO IMPORTANTE: Usar BCrypt para comparar contraseñas
            if (!passwordEncoder.matches(pass, user.getPass())) {
                logger.warn("❌ [USERS] Login fallido: Contraseña incorrecta para el usuario '{}'.", user.getUserName());
                logRepository.save(new LogEntity("USERS", "WARN", "Login fallido: Contraseña incorrecta para el usuario '" + user.getUserName() + "'.", user.getId()));

                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
            }

            logger.debug("👤 [USERS] Generando token JWT para el usuario '{}'.", user.getUserName());
            logRepository.save(new LogEntity("USERS", "DEBUG", "Generando token JWT para el usuario '" + user.getUserName() + "'.", user.getId()));

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
            logRepository.save(new LogEntity("USERS", "INFO", "Inicio de sesión exitoso para el usuario '" + user.getUserName() + "' (ID: " + user.getId() + ").", user.getId()));

            return ResponseEntity.ok(response);
    }
}