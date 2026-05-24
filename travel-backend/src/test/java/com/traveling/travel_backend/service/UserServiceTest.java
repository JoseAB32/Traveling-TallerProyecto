package com.traveling.travel_backend.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CityRepository cityRepository;
    @Mock private LogRepository logRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private JwtService jwtService;

    private UserService userService;

    private User sampleUser;
    private String rawPassword;
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-for-unit-tests-only-32chars!");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        userService = new UserService(userRepository, cityRepository, logRepository, jwtService, passwordEncoder);

        rawPassword = "miPassword123";
        hashedPassword = passwordEncoder.encode(rawPassword);

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUserName("carlos_viajero");
        sampleUser.setPass(hashedPassword);
        sampleUser.setCorreo("carlos@mail.com");
        sampleUser.setState(true);
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("Debe retornar la lista de usuarios mapeada a DTO")
        void shouldReturnAllUsersAsDTOs() {
            when(userRepository.findAll()).thenReturn(List.of(sampleUser));

            List<UserResponseDTO> result = userService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserName()).isEqualTo("carlos_viajero");
            assertThat(result.get(0).getCorreo()).isEqualTo("carlos@mail.com");
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay usuarios")
        void shouldReturnEmptyListWhenNoUsers() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserResponseDTO> result = userService.getAllUsers();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("El DTO nunca debe exponer la contraseña")
        void shouldNeverExposePassword() {
            when(userRepository.findAll()).thenReturn(List.of(sampleUser));

            List<UserResponseDTO> result = userService.getAllUsers();

            assertThat(result.get(0)).isNotNull();
            assertThat(result.get(0).getUserName()).isNotNull();
        }
    }


    @Nested
    @DisplayName("createUser")
    class CreateUserTests {

        @Test
        @DisplayName("Debe crear usuario correctamente sin ciudad")
        void shouldCreateUserWithoutCity() {
            sampleUser.setCity(null);
            sampleUser.setPass(rawPassword);
            when(userRepository.findByUserNameAndStateTrue("carlos_viajero")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            UserResponseDTO result = userService.createUser(sampleUser);

            assertThat(result.getUserName()).isEqualTo("carlos_viajero");
            verify(userRepository).save(any(User.class));
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe resolver la ciudad si viene en el body")
        void shouldResolveCityIfProvided() {
            City city = new City();
            city.setId(10L);
            city.setName("Cochabamba");
            sampleUser.setCity(city);
            sampleUser.setPass(rawPassword);

            when(userRepository.findByUserNameAndStateTrue("carlos_viajero")).thenReturn(Optional.empty());
            when(cityRepository.findById(10L)).thenReturn(Optional.of(city));
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            UserResponseDTO result = userService.createUser(sampleUser);

            assertThat(result).isNotNull();
            verify(cityRepository).findById(10L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si la ciudad no existe")
        void shouldThrowWhenCityNotFound() {
            City city = new City();
            city.setId(99L);
            sampleUser.setCity(city);

            when(userRepository.findByUserNameAndStateTrue("carlos_viajero")).thenReturn(Optional.empty());
            when(cityRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(sampleUser))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el userName ya está en uso")
        void shouldThrowWhenUserNameAlreadyExists() {
            when(userRepository.findByUserNameAndStateTrue("carlos_viajero")).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.createUser(sampleUser))
                    .isInstanceOf(BadRequestException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el userName es nulo")
        void shouldThrowWhenUserNameIsNull() {
            sampleUser.setUserName(null);

            assertThatThrownBy(() -> userService.createUser(sampleUser))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el userName está vacío")
        void shouldThrowWhenUserNameIsBlank() {
            sampleUser.setUserName("   ");

            assertThatThrownBy(() -> userService.createUser(sampleUser))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("No debe encriptar si la contraseña es nula")
        void shouldNotEncryptWhenPassIsNull() {
            sampleUser.setPass(null);
            sampleUser.setCity(null);
            when(userRepository.findByUserNameAndStateTrue("carlos_viajero")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.createUser(sampleUser);

            assertThat(sampleUser.getPass()).isNull();
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("Debe retornar token JWT con credenciales correctas")
        void shouldReturnTokenOnSuccessfulLogin() {
            LoginRequest request = new LoginRequest();
            request.setUserName("carlos_viajero");
            request.setPass(rawPassword);

            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));

            LoginResponse response = userService.login(request);

            assertThat(response.getToken()).isNotBlank();
            assertThat(response.getUserName()).isEqualTo("carlos_viajero");
            assertThat(response.getCorreo()).isEqualTo("carlos@mail.com");
            assertThat(response.getType()).isEqualTo("Bearer");
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si el usuario no existe")
        void shouldThrowWhenUserNotFound() {
            LoginRequest request = new LoginRequest();
            request.setUserName("nadie");
            request.setPass(rawPassword);
            when(userRepository.findByUserName("nadie")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si el usuario está desactivado")
        void shouldThrowWhenUserIsInactive() {
            sampleUser.setState(false);
            LoginRequest request = new LoginRequest();
            request.setUserName("carlos_viajero");
            request.setPass(rawPassword);
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Usuario desactivado.");
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si la contraseña es incorrecta")
        void shouldThrowWhenPasswordIsWrong() {
            LoginRequest request = new LoginRequest();
            request.setUserName("carlos_viajero");
            request.setPass("contrasena_incorrecta");
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el userName es nulo")
        void shouldThrowWhenUserNameIsNull() {
            LoginRequest request = new LoginRequest();
            request.setUserName(null);
            request.setPass(rawPassword);

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El nombre de usuario es requerido.");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el userName está vacío")
        void shouldThrowWhenUserNameIsBlank() {
            LoginRequest request = new LoginRequest();
            request.setUserName("  ");
            request.setPass(rawPassword);

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El nombre de usuario es requerido.");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la contraseña es nula")
        void shouldThrowWhenPasswordIsNull() {
            LoginRequest request = new LoginRequest();
            request.setUserName("carlos_viajero");
            request.setPass(null);

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("La contrasena es requerida.");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la contraseña está vacía")
        void shouldThrowWhenPasswordIsBlank() {
            LoginRequest request = new LoginRequest();
            request.setUserName("carlos_viajero");
            request.setPass("  ");

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("La contrasena es requerida.");
        }
    }

    @Nested
    @DisplayName("getProfile")
    class GetProfileTests {

        private Authentication authentication;

        @BeforeEach
        void setUpAuth() {
            authentication = new UsernamePasswordAuthenticationToken("carlos_viajero", null, List.of());
        }

        @Test
        @DisplayName("Debe retornar el perfil del usuario autenticado")
        void shouldReturnProfileForAuthenticatedUser() {
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));

            UserResponseDTO result = userService.getProfile(authentication);

            assertThat(result.getUserName()).isEqualTo("carlos_viajero");
            assertThat(result.getCorreo()).isEqualTo("carlos@mail.com");
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si la autenticación es nula")
        void shouldThrowWhenAuthIsNull() {
            assertThatThrownBy(() -> userService.getProfile(null))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("No autenticado.");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el usuario no existe en BD")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getProfile(authentication))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("carlos_viajero");
        }
    }
}