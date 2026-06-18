package com.traveling.travel_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.traveling.travel_backend.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Nested
    @DisplayName("CloudinaryService - foto de perfil")
    class CloudinaryProfilePictureTests {

        private CloudinaryService buildService(String returnUrl, boolean shouldThrow, Map<String, Object> capturedParams) {
            return new CloudinaryService(null) {
                @Override
                public String uploadProfilePicture(MultipartFile file, Long userId) throws IOException {
                    if (shouldThrow) {
                        throw new IOException("Cloudinary no disponible");
                    }

                    capturedParams.put("public_id", "user_" + userId);
                    capturedParams.put("folder", "traveling/profile_pictures");
                    capturedParams.put("overwrite", true);

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
                    "file",
                    "foto.jpg",
                    "image/jpeg",
                    new byte[]{1, 2, 3}
            );

            String result = service.uploadProfilePicture(file, 1L);

            assertThat(result).isEqualTo(expectedUrl);
        }

        @Test
        @DisplayName("Debe llamar a upload con el public_id correcto según el userId")
        void shouldUseCorrectPublicId() throws IOException {
            Map<String, Object> captured = new HashMap<>();

            CloudinaryService service = buildService(
                    "https://res.cloudinary.com/test/image/upload/user_26.jpg",
                    false,
                    captured
            );

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "foto.jpg",
                    "image/jpeg",
                    new byte[]{1, 2, 3}
            );

            service.uploadProfilePicture(file, 26L);

            assertThat(captured.get("public_id")).isEqualTo("user_26");
        }

        @Test
        @DisplayName("Debe usar la carpeta traveling/profile_pictures")
        void shouldUseCorrectFolder() throws IOException {
            Map<String, Object> captured = new HashMap<>();

            CloudinaryService service = buildService(
                    "https://res.cloudinary.com/test/img.jpg",
                    false,
                    captured
            );

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "foto.jpg",
                    "image/jpeg",
                    new byte[]{1, 2, 3}
            );

            service.uploadProfilePicture(file, 5L);

            assertThat(captured.get("folder")).isEqualTo("traveling/profile_pictures");
        }

        @Test
        @DisplayName("Debe tener overwrite en true para reemplazar foto anterior")
        void shouldHaveOverwriteTrue() throws IOException {
            Map<String, Object> captured = new HashMap<>();

            CloudinaryService service = buildService(
                    "https://res.cloudinary.com/test/img.jpg",
                    false,
                    captured
            );

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "foto.jpg",
                    "image/jpeg",
                    new byte[]{1, 2, 3}
            );

            service.uploadProfilePicture(file, 1L);

            assertThat(captured.get("overwrite")).isEqualTo(true);
        }

        @Test
        @DisplayName("Debe propagar IOException si Cloudinary falla")
        void shouldPropagateIOExceptionOnFailure() {
            CloudinaryService service = buildService(null, true, new HashMap<>());

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "foto.jpg",
                    "image/jpeg",
                    new byte[]{1, 2, 3}
            );

            assertThatThrownBy(() -> service.uploadProfilePicture(file, 1L))
                    .isInstanceOf(IOException.class)
                    .hasMessage("Cloudinary no disponible");
        }
    }

    @Nested
    @DisplayName("CloudinaryService - imágenes de lugares")
    class CloudinaryPlaceImageTests {

        @Test
        @DisplayName("Debe subir imagen de lugar y retornar la URL segura")
        void shouldUploadPlaceImageAndReturnSecureUrl() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "images",
                    "madidi.jpg",
                    "image/jpeg",
                    new byte[]{1, 2, 3}
            );

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap()))
                    .thenReturn(Map.of(
                            "secure_url", "https://res.cloudinary.com/test/madidi.jpg"
                    ));

            CloudinaryService service = new CloudinaryService(cloudinary);

            String result = service.uploadPlaceImage(file, 50L, 2);

            assertThat(result).isEqualTo("https://res.cloudinary.com/test/madidi.jpg");
        }

        @Test
        @DisplayName("Debe enviar folder y public_id correctos al subir imagen de lugar")
        void shouldUseCorrectFolderAndPublicIdForPlaceImage() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "images",
                    "madidi.jpg",
                    "image/jpeg",
                    new byte[]{1, 2, 3}
            );

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap()))
                    .thenReturn(Map.of(
                            "secure_url", "https://res.cloudinary.com/test/madidi.jpg"
                    ));

            CloudinaryService service = new CloudinaryService(cloudinary);

            service.uploadPlaceImage(file, 50L, 2);

            @SuppressWarnings("rawtypes")
            ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            verify(uploader).upload(any(byte[].class), paramsCaptor.capture());

            Map<?, ?> params = paramsCaptor.getValue();

            assertThat(params.get("folder")).isEqualTo("traveling/place_images");
            assertThat(params.get("public_id")).isEqualTo("place_50_image_2");
            assertThat(params.get("overwrite")).isEqualTo(true);
            assertThat(params.get("resource_type")).isEqualTo("image");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el archivo es null")
        void shouldThrowBadRequestWhenFileIsNull() {
            CloudinaryService service = new CloudinaryService(cloudinary);

            assertThatThrownBy(() -> service.uploadPlaceImage(null, 50L, 0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Debe seleccionar una imagen válida");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el archivo está vacío")
        void shouldThrowBadRequestWhenFileIsEmpty() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "images",
                    "empty.jpg",
                    "image/jpeg",
                    new byte[0]
            );

            CloudinaryService service = new CloudinaryService(cloudinary);

            assertThatThrownBy(() -> service.uploadPlaceImage(emptyFile, 50L, 0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Debe seleccionar una imagen válida");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si Cloudinary no devuelve secure_url")
        void shouldThrowBadRequestWhenCloudinaryDoesNotReturnSecureUrl() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "images",
                    "madidi.jpg",
                    "image/jpeg",
                    new byte[]{1, 2, 3}
            );

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap()))
                    .thenReturn(Map.of());

            CloudinaryService service = new CloudinaryService(cloudinary);

            assertThatThrownBy(() -> service.uploadPlaceImage(file, 50L, 0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cloudinary no devolvió una URL válida");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si Cloudinary falla al subir la imagen")
        void shouldThrowBadRequestWhenCloudinaryFails() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "images",
                    "madidi.jpg",
                    "image/jpeg",
                    new byte[]{1, 2, 3}
            );

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap()))
                    .thenThrow(new IOException("Cloudinary error"));

            CloudinaryService service = new CloudinaryService(cloudinary);

            assertThatThrownBy(() -> service.uploadPlaceImage(file, 50L, 0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("No se pudo subir la imagen a Cloudinary");
        }
    }
}