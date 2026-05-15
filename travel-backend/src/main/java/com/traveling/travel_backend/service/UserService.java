package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.LoginRequest;
import com.traveling.travel_backend.dto.LoginResponse;
import com.traveling.travel_backend.dto.UserResponseDTO;
import com.traveling.travel_backend.exception.BadRequestException;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.exception.UnauthorizedException;
import com.traveling.travel_backend.model.City;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.CityRepository;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.UserRepository;
import com.traveling.travel_backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final LogRepository logRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CityRepository cityRepository,
                       LogRepository logRepository, JwtService jwtService,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.cityRepository  = cityRepository;
        this.logRepository   = logRepository;
        this.jwtService      = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> getAllUsers() {
        logger.info("{} [{}] Solicitando lista de todos los usuarios", AppConstants.PREFIX_USER, AppConstants.LOG_USERS);
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_INFO,
                "Recibida solicitud para obtener la lista de todos los usuarios.", null));

        List<User> users = userRepository.findAll();

        logger.debug("{} [{}] Se recuperaron {} usuarios", AppConstants.PREFIX_USER, AppConstants.LOG_USERS, users.size());
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_DEBUG,
                "Se recuperaron " + users.size() + " usuarios de la base de datos.", null));

        return users.stream().map(UserResponseDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDTO createUser(User user) {
        logger.info("{} [{}] Solicitud para crear usuario: {}", AppConstants.PREFIX_USER, AppConstants.LOG_USERS, user.getUserName());
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_INFO,
                "Recibida solicitud para crear un nuevo usuario. Nombre de usuario: " + user.getUserName(), null));

        // Validar userName no vacío
        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            logger.warn("{} [{}] Creacion rechazada: userName vacio", AppConstants.PREFIX_ERROR, AppConstants.LOG_USERS);
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                    "Creacion de usuario rechazada: userName vacio.", null));
            throw new BadRequestException(AppConstants.USER_REQUIRED);
        }

        String trimmedUserName = user.getUserName().trim();

        if (userRepository.findByUserNameAndStateTrue(trimmedUserName).isPresent()) {
            logger.warn("{} [{}] Creacion rechazada: userName '{}' ya en uso", AppConstants.PREFIX_ERROR, AppConstants.LOG_USERS, trimmedUserName);
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                    "Creacion rechazada: userName '" + trimmedUserName + "' ya en uso.", null));
            throw new BadRequestException(AppConstants.USER_ALREADY_IN_USE);
        }

        // Resolver ciudad si viene en el body
        if (user.getCity() != null) {
            logger.debug("{} [{}] Buscando ciudad con ID: {}", AppConstants.PREFIX_USER, AppConstants.LOG_USERS, user.getCity().getId());
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_DEBUG,
                    "Buscando información de la ciudad con ID: " + user.getCity().getId(), null));

            City city = cityRepository.findById(user.getCity().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ciudad no encontrada con ID: " + user.getCity().getId()));
            user.setCity(city);
        }

        // Encriptar contraseña
        if (user.getPass() != null && !user.getPass().isEmpty()) {
            user.setPass(passwordEncoder.encode(user.getPass()));
        }

        User saved = userRepository.save(user);

        logger.info("{} [{}] Usuario '{}' creado con ID: {}", AppConstants.PREFIX_USER, AppConstants.LOG_USERS, saved.getUserName(), saved.getId());
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_INFO,
                "Usuario '" + saved.getUserName() + "' creado con ID: " + saved.getId(), null));

        return UserResponseDTO.fromEntity(saved);
    }

    @Transactional
    public LoginResponse login(LoginRequest credentials) {
        String userName = credentials.getUserName();
        String pass     = credentials.getPass();

        logger.info("{} [{}] Intento de login para: {}", AppConstants.PREFIX_USER, AppConstants.LOG_USERS,
                userName != null ? userName : "DESCONOCIDO");
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_INFO,
                "Intento de login para: " + (userName != null ? userName : "DESCONOCIDO"), null));

        if (userName == null || userName.trim().isEmpty()) {
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN, "Login rechazado: userName vacio.", null));
            throw new BadRequestException("El nombre de usuario es requerido.");
        }

        if (pass == null || pass.trim().isEmpty()) {
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                    "Login rechazado para '" + userName + "': contrasena vacia.", null));
            throw new BadRequestException("La contrasena es requerida.");
        }

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> {
                    logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                            "Login fallido: usuario '" + userName + "' no encontrado.", null));
                    return new UnauthorizedException("Usuario o contrasena incorrectos.");
                });

        if (!user.isState()) {
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                    "Login fallido: usuario '" + user.getUserName() + "' inactivo.", user.getId()));
            throw new UnauthorizedException("Usuario desactivado.");
        }

        if (!passwordEncoder.matches(pass, user.getPass())) {
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                    "Login fallido: contrasena incorrecta para '" + user.getUserName() + "'.", user.getId()));
            throw new UnauthorizedException("Usuario o contrasena incorrectos.");
        }

        String token = jwtService.generateToken(user.getUserName(), user.getId());

        logger.info("{} [{}] Login exitoso para '{}' (ID: {})", AppConstants.PREFIX_USER, AppConstants.LOG_USERS, userName, user.getId());
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_INFO,
                "Login exitoso para '" + user.getUserName() + "' (ID: " + user.getId() + ").", user.getId()));

        return new LoginResponse(token, "Bearer", user.getId(), user.getUserName(), user.getCorreo(), "Login exitoso");
    }
    @Transactional
    public UserResponseDTO getProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
                logger.warn("{} [{}] Acceso a perfil rechazado: no autenticado",AppConstants.PREFIX_USER, AppConstants.LOG_USERS);
                logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                        "Acceso a perfil rechazado: no autenticado.", null));
                throw new UnauthorizedException("No autenticado.");
        }

        String userName = authentication.getName();

        logger.info("{} [{}] Solicitando perfil del usuario: {}",
                AppConstants.PREFIX_USER, AppConstants.LOG_USERS, userName);
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_INFO,
                "Solicitando perfil del usuario: " + userName + " - GET /api/profile", null));

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> {
                        logger.warn("{} [{}] Perfil no encontrado para usuario: {}",
                                AppConstants.PREFIX_ERROR, AppConstants.LOG_USERS, userName);
                        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                                "Perfil no encontrado para usuario: " + userName, null));
                        return new ResourceNotFoundException("Usuario no encontrado: " + userName);
                });

        logger.debug("{} [{}] Perfil encontrado para usuario: {} (ID: {})",
                AppConstants.PREFIX_USER, AppConstants.LOG_USERS, userName, user.getId());

        return UserResponseDTO.fromEntity(user);
        }
}