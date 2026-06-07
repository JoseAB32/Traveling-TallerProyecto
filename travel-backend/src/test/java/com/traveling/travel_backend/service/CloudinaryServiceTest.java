package com.traveling.travel_backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Nested
    @DisplayName("CloudinaryService")
    class CloudinaryServiceTests {

        private CloudinaryService buildService(String returnUrl, boolean shouldThrow, Map<String, Object> capturedParams) {
            return new CloudinaryService(null) {
                @Override
                public String uploadProfilePicture(MultipartFile file, Long userId) throws IOException {
                    if (shouldThrow) {
                        throw new IOException("Cloudinary no disponible");
                    }
                    capturedParams.put("public_id",  "user_" + userId);
                    capturedParams.put("folder",     "traveling/profile_pictures");
                    capturedParams.put("overwrite",  true);
                    return returnUrl;
                }
            };
        }

        @Test
        @DisplayName("Debe retornar la URL segura de Cloudinary tras la subida")
        void shouldReturnSecureUrlAfterUpload() throws IOException {
            String expectedUrl = "https://res.cloudinary.com/test/image/upload/user_1.jpg";
            CloudinaryService service = buildService(expectedUrl, false, new HashMap<>());
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3}
            );

            String result = service.uploadProfilePicture(file, 1L);

            assertThat(result).isEqualTo(expectedUrl);
        }

        @Test
        @DisplayName("Debe llamar a upload con el public_id correcto según el userId")
        void shouldUseCorrectPublicId() throws IOException {
            Map<String, Object> captured = new HashMap<>();
            CloudinaryService service = buildService("https://res.cloudinary.com/test/image/upload/user_26.jpg", false, captured);
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3}
            );

            service.uploadProfilePicture(file, 26L);

            assertThat(captured.get("public_id")).isEqualTo("user_26");
        }

        @Test
        @DisplayName("Debe usar la carpeta traveling/profile_pictures")
        void shouldUseCorrectFolder() throws IOException {
            Map<String, Object> captured = new HashMap<>();
            CloudinaryService service = buildService("https://res.cloudinary.com/test/img.jpg", false, captured);
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3}
            );

            service.uploadProfilePicture(file, 5L);

            assertThat(captured.get("folder")).isEqualTo("traveling/profile_pictures");
        }

        @Test
        @DisplayName("Debe tener overwrite en true para reemplazar foto anterior")
        void shouldHaveOverwriteTrue() throws IOException {
            Map<String, Object> captured = new HashMap<>();
            CloudinaryService service = buildService("https://res.cloudinary.com/test/img.jpg", false, captured);
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3}
            );

            service.uploadProfilePicture(file, 1L);

            assertThat(captured.get("overwrite")).isEqualTo(true);
        }

        @Test
        @DisplayName("Debe propagar IOException si Cloudinary falla")
        void shouldPropagateIOExceptionOnFailure() {
            CloudinaryService service = buildService(null, true, new HashMap<>());
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3}
            );

            assertThatThrownBy(() -> service.uploadProfilePicture(file, 1L))
                    .isInstanceOf(IOException.class)
                    .hasMessage("Cloudinary no disponible");
        }
    }
}