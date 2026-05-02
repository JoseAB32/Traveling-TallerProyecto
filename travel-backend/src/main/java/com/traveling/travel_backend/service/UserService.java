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
            logger.warn("{} [{}] Creación rechazada: userName vacío", AppConstants.PREFIX_ERROR, AppConstants.LOG_USERS);
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                    "Creación de usuario rechazada: El nombre de usuario está vacío o es nulo.", null));
            throw new BadRequestException(AppConstants.USER_REQUIRED);
        }

        String trimmedUserName = user.getUserName().trim();

        // Validar que no exista ya
        if (userRepository.findByUserNameAndStateTrue(trimmedUserName).isPresent()) {
            logger.warn("{} [{}] Creación rechazada: userName '{}' ya en uso", AppConstants.PREFIX_ERROR, AppConstants.LOG_USERS, trimmedUserName);
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                    "Creación de usuario rechazada: El nombre de usuario '" + trimmedUserName + "' ya se encuentra en uso.", null));
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
                "Usuario '" + saved.getUserName() + "' creado exitosamente con el ID: " + saved.getId(), null));

        return UserResponseDTO.fromEntity(saved);
    }


    public LoginResponse login(LoginRequest credentials) {
        String userName = credentials.getUserName();
        String pass     = credentials.getPass();

        logger.info("{} [{}] Intento de login para: {}", AppConstants.PREFIX_USER, AppConstants.LOG_USERS,
                userName != null ? userName : "DESCONOCIDO");
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_INFO,
                "Intento de inicio de sesión para el usuario: " + (userName != null ? userName : "DESCONOCIDO"), null));

        // Validaciones de entrada
        if (userName == null || userName.trim().isEmpty()) {
            logger.warn("{} [{}] Login rechazado: userName vacío", AppConstants.PREFIX_ERROR, AppConstants.LOG_USERS);
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                    "Login rechazado: El nombre de usuario es nulo o está vacío.", null));
            throw new BadRequestException("El nombre de usuario es requerido.");
        }

        if (pass == null || pass.trim().isEmpty()) {
            logger.warn("{} [{}] Login rechazado: contraseña vacía para '{}'", AppConstants.PREFIX_ERROR, AppConstants.LOG_USERS, userName);
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                    "Login rechazado para el usuario '" + userName + "': La contraseña está vacía.", null));
            throw new BadRequestException("La contraseña es requerida.");
        }

        // Buscar usuario
        logger.debug("{} [{}] Buscando usuario '{}'", AppConstants.PREFIX_USER, AppConstants.LOG_USERS, userName);
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_DEBUG,
                "Buscando al usuario '" + userName + "' en la base de datos.", null));

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> {
                    logger.warn("{} [{}] Login fallido: usuario '{}' no existe", AppConstants.PREFIX_ERROR, AppConstants.LOG_USERS, userName);
                    logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                            "Login fallido: No se encontró ningún usuario con el nombre '" + userName + "'.", null));
                    return new UnauthorizedException("Usuario o contraseña incorrectos.");
                });

        // Verificar estado activo
        if (!user.isState()) {
            logger.warn("{} [{}] Login fallido: usuario '{}' inactivo", AppConstants.PREFIX_ERROR, AppConstants.LOG_USERS, userName);
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                    "Login fallido: El usuario '" + user.getUserName() + "' está inactivo.", user.getId()));
            throw new UnauthorizedException("Usuario desactivado.");
        }

        // Verificar contraseña
        if (!passwordEncoder.matches(pass, user.getPass())) {
            logger.warn("{} [{}] Login fallido: contraseña incorrecta para '{}'", AppConstants.PREFIX_ERROR, AppConstants.LOG_USERS, userName);
            logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_WARN,
                    "Login fallido: Contraseña incorrecta para el usuario '" + user.getUserName() + "'.", user.getId()));
            throw new UnauthorizedException("Usuario o contraseña incorrectos.");
        }

        // Generar token
        logger.debug("{} [{}] Generando token JWT para '{}'", AppConstants.PREFIX_USER, AppConstants.LOG_USERS, userName);
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_DEBUG,
                "Generando token JWT para el usuario '" + user.getUserName() + "'.", user.getId()));

        String token = jwtService.generateToken(user.getUserName(), user.getId());

        logger.info("{} [{}] Login exitoso para '{}' (ID: {})", AppConstants.PREFIX_USER, AppConstants.LOG_USERS, userName, user.getId());
        logRepository.save(new LogEntity(AppConstants.LOG_USERS, AppConstants.LOG_INFO,
                "Inicio de sesión exitoso para el usuario '" + user.getUserName() + "' (ID: " + user.getId() + ").", user.getId()));

        return new LoginResponse(token, "Bearer", user.getId(), user.getUserName(), user.getCorreo(), "Login exitoso");
    }
}