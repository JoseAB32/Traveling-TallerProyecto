package com.traveling.travel_backend.service;

import com.traveling.travel_backend.dto.PlaceImageResponseDTO;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.model.PlaceImage;
import com.traveling.travel_backend.repository.PlaceImageRepository;
import com.traveling.travel_backend.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaceImageService {

    private final PlaceImageRepository placeImageRepository;
    private final PlaceRepository placeRepository;

    public PlaceImageService(
            PlaceImageRepository placeImageRepository,
            PlaceRepository placeRepository
    ) {
        this.placeImageRepository = placeImageRepository;
        this.placeRepository = placeRepository;
    }

    @Transactional(readOnly = true)
    public List<PlaceImageResponseDTO> getImagesByPlaceId(Long placeId) {
        if (!placeRepository.existsById(placeId)) {
            throw new ResourceNotFoundException("Lugar no encontrado con ID: " + placeId);
        }

        return placeImageRepository.findByPlaceIdAndStateTrueOrderByDisplayOrderAsc(placeId)
                .stream()
                .map(PlaceImageResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteImage(Long imageId) {
        PlaceImage image = placeImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + imageId));

        image.setState(false);
        placeImageRepository.save(image);
    }
}