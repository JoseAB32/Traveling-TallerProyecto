package com.traveling.travel_backend.service;

import com.traveling.travel_backend.dto.LoginRequest;
import com.traveling.travel_backend.dto.LoginResponse;
import com.traveling.travel_backend.dto.UpdateProfileRequestDTO;
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
import com.traveling.travel_backend.service.CloudinaryService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.IOException;
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
    private CloudinaryService cloudinaryService;

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

        cloudinaryService = new CloudinaryService(null) {
            @Override
            public String uploadProfilePicture(org.springframework.web.multipart.MultipartFile file, Long userId) {
                return "https://fake-cloudinary-url.com/user_" + userId + ".jpg";
            }
        };

        userService = new UserService(userRepository, cityRepository, logRepository, jwtService, passwordEncoder, cloudinaryService);

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
    @Nested
    @DisplayName("changePassword")
    class ChangePasswordTests {

        private Authentication authentication;

        @BeforeEach
        void setUpAuth() {
            authentication = new UsernamePasswordAuthenticationToken("carlos_viajero", null, List.of());
        }

        @Test
        @DisplayName("Debe cambiar la contraseña correctamente")
        void shouldChangePasswordSuccessfully() {
            String newPassword = "nuevaPassword123";
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            userService.changePassword(authentication, rawPassword, newPassword);

            verify(userRepository).save(any(User.class));
            assertThat(passwordEncoder.matches(newPassword, sampleUser.getPass())).isTrue();
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si la autenticación es nula")
        void shouldThrowWhenAuthIsNull() {
            assertThatThrownBy(() -> userService.changePassword(null, rawPassword, "nuevaPassword123"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("No autenticado.");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el usuario no existe en BD")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changePassword(authentication, rawPassword, "nuevaPassword123"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la contraseña actual es incorrecta")
        void shouldThrowWhenCurrentPasswordIsWrong() {
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.changePassword(authentication, "contrasenaIncorrecta", "nuevaPassword123"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("La contraseña actual es incorrecta.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la nueva contraseña tiene menos de 8 caracteres")
        void shouldThrowWhenNewPasswordTooShort() {
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.changePassword(authentication, rawPassword, "corta"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("La nueva contraseña debe tener mínimo 8 caracteres.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la nueva contraseña es nula")
        void shouldThrowWhenNewPasswordIsNull() {
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.changePassword(authentication, rawPassword, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("La nueva contraseña debe tener mínimo 8 caracteres.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("No debe guardar si la contraseña actual falla la validación")
        void shouldNotSaveWhenValidationFails() {
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));

            try {
                userService.changePassword(authentication, "wrongPass", "nuevaPassword123");
            } catch (BadRequestException ignored) {}

            verify(userRepository, never()).save(any());
        }
    }
    @Nested
    @DisplayName("updateProfile")
    class UpdateProfileTests {

        private Authentication authentication;

        @BeforeEach
        void setUpAuth() {
            authentication = new UsernamePasswordAuthenticationToken("carlos_viajero", null, List.of());
        }

        @Test
        @DisplayName("Debe actualizar userName si es nuevo y no está en uso")
        void shouldUpdateUserName() {
            UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
            request.setUserName("nuevo_nombre");

            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));
            when(userRepository.findByUserNameAndStateTrue("nuevo_nombre")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            userService.updateProfile(authentication, request);

            verify(userRepository).save(any(User.class));
            verify(logRepository, atLeastOnce()).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el nuevo userName ya está en uso")
        void shouldThrowWhenUserNameTaken() {
            UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
            request.setUserName("nombre_ocupado");

            User otherUser = new User();
            otherUser.setUserName("nombre_ocupado");

            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));
            when(userRepository.findByUserNameAndStateTrue("nombre_ocupado")).thenReturn(Optional.of(otherUser));

            assertThatThrownBy(() -> userService.updateProfile(authentication, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("ya está en uso");
        }

        @Test
        @DisplayName("Debe actualizar correo si es nuevo y no está en uso")
        void shouldUpdateCorreo() {
            UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
            request.setCorreo("nuevo@mail.com");

            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));
            when(userRepository.findByCorreo("nuevo@mail.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            userService.updateProfile(authentication, request);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el correo ya está en uso")
        void shouldThrowWhenCorreoTaken() {
            UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
            request.setCorreo("ocupado@mail.com");

            User otherUser = new User();
            otherUser.setCorreo("ocupado@mail.com");

            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));
            when(userRepository.findByCorreo("ocupado@mail.com")).thenReturn(Optional.of(otherUser));

            assertThatThrownBy(() -> userService.updateProfile(authentication, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("correo");
        }

        @Test
        @DisplayName("Debe actualizar birthday si viene en el request")
        void shouldUpdateBirthday() {
            UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
            request.setBirthday("2000-01-15");

            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            userService.updateProfile(authentication, request);

            assertThat(sampleUser.getBirthday()).isEqualTo("2000-01-15");
        }

        @Test
        @DisplayName("Debe actualizar ciudad si el cityId existe")
        void shouldUpdateCity() {
            City city = new City();
            city.setId(5L);
            city.setName("Santa Cruz");

            UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
            request.setCityId(5L);

            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));
            when(cityRepository.findById(5L)).thenReturn(Optional.of(city));
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            userService.updateProfile(authentication, request);

            assertThat(sampleUser.getCity()).isEqualTo(city);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si la ciudad no existe")
        void shouldThrowWhenCityNotFound() {
            UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
            request.setCityId(99L);

            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));
            when(cityRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateProfile(authentication, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si no hay autenticación")
        void shouldThrowWhenAuthIsNull() {
            assertThatThrownBy(() -> userService.updateProfile(null, new UpdateProfileRequestDTO()))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("No autenticado.");
        }

        @Test
        @DisplayName("No debe actualizar userName si es el mismo que el actual")
        void shouldNotUpdateWhenUserNameUnchanged() {
            UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
            request.setUserName("carlos_viajero");

            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            userService.updateProfile(authentication, request);

            verify(userRepository, never()).findByUserNameAndStateTrue("carlos_viajero");
        }
    }

    @Nested
    @DisplayName("updateProfilePicture")
    class UpdateProfilePictureTests {

        private Authentication authentication;

        private String cloudinaryResult = "https://res.cloudinary.com/test/image/upload/user_1.jpg";
        private boolean cloudinaryShouldFail = false;

        @BeforeEach
        void setUpAuth() {
            authentication = new UsernamePasswordAuthenticationToken("carlos_viajero", null, List.of());

            CloudinaryService localCloudinary = new CloudinaryService(null) {
                @Override
                public String uploadProfilePicture(
                        org.springframework.web.multipart.MultipartFile file, Long userId) throws java.io.IOException {
                    if (cloudinaryShouldFail) {
                        throw new java.io.IOException("Cloudinary no disponible");
                    }
                    return cloudinaryResult;
                }
            };

            userService = new UserService(userRepository, cityRepository, logRepository,
                    jwtService, passwordEncoder, localCloudinary);
        }

        @Test
        @DisplayName("Debe subir la imagen y actualizar profilePictureUrl")
        void shouldUploadAndUpdatePictureUrl() throws IOException {
            cloudinaryResult = "https://res.cloudinary.com/test/image/upload/user_1.jpg";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3}
            );
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            userService.updateProfilePicture(authentication, file);

            verify(userRepository).save(any(User.class));
            assertThat(sampleUser.getProfilePictureUrl())
                    .isEqualTo("https://res.cloudinary.com/test/image/upload/user_1.jpg");
        }

        @Test
        @DisplayName("Debe lanzar RuntimeException si Cloudinary falla")
        void shouldThrowWhenCloudinaryFails() {
            cloudinaryShouldFail = true;
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3}
            );
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.updateProfilePicture(authentication, file))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error al subir la imagen");
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si no hay autenticación")
        void shouldThrowWhenAuthIsNull() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3}
            );
            assertThatThrownBy(() -> userService.updateProfilePicture(null, file))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("No autenticado.");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el archivo está vacío")
        void shouldThrowWhenFileIsEmpty() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", new byte[0]
            );
            assertThatThrownBy(() -> userService.updateProfilePicture(authentication, emptyFile))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("No se recibió ninguna imagen.");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el archivo es nulo")
        void shouldThrowWhenFileIsNull() {
            assertThatThrownBy(() -> userService.updateProfilePicture(authentication, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("No se recibió ninguna imagen.");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el archivo no es una imagen")
        void shouldThrowWhenFileIsNotImage() {
            MockMultipartFile pdfFile = new MockMultipartFile(
                    "file", "documento.pdf", "application/pdf", new byte[]{1, 2, 3}
            );
            assertThatThrownBy(() -> userService.updateProfilePicture(authentication, pdfFile))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El archivo debe ser una imagen.");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el usuario no existe")
        void shouldThrowWhenUserNotFound() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3}
            );
            when(userRepository.findByUserName("carlos_viajero")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateProfilePicture(authentication, file))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}