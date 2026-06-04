package com.traveling.travel_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder",      "traveling/profile_pictures",
                        "public_id",   "user_" + userId,
                        "overwrite",   true,
                        "width",       400,
                        "height",      400,
                        "crop",        "fill",
                        "gravity",     "face"
                )
        );
        return (String) uploadResult.get("secure_url");
    }
}