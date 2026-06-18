package com.traveling.travel_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.traveling.travel_backend.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadProfilePicture(MultipartFile file, Long userId) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "traveling/profile_pictures",
                        "public_id", "user_" + userId,
                        "overwrite", true,
                        "width", 400,
                        "height", 400,
                        "crop", "fill",
                        "gravity", "face"
                )
        );

        return (String) uploadResult.get("secure_url");
    }

    public String uploadPlaceImage(MultipartFile file, Long placeId, int imageIndex) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Debe seleccionar una imagen válida.");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "traveling/place_images",
                            "public_id", "place_" + placeId + "_image_" + imageIndex,
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");

            if (secureUrl == null) {
                throw new BadRequestException("Cloudinary no devolvió una URL válida.");
            }

            return secureUrl.toString();

        } catch (IOException e) {
            throw new BadRequestException("No se pudo subir la imagen a Cloudinary.");
        }
    }
}