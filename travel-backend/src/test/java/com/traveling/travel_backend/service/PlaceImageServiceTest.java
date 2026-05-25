package com.traveling.travel_backend.service;

import com.traveling.travel_backend.dto.PlaceImageResponseDTO;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.model.PlaceImage;
import com.traveling.travel_backend.repository.PlaceImageRepository;
import com.traveling.travel_backend.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceImageServiceTest {

    @Mock
    private PlaceImageRepository placeImageRepository;

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private PlaceImageService placeImageService;

    private Place place;
    private PlaceImage image1;
    private PlaceImage image2;

    @BeforeEach
    void setUp() {
        place = new Place();
        place.setId(1L);
        place.setName("Cristo de la Concordia");

        image1 = new PlaceImage();
        image1.setId(1L);
        image1.setImageUrl("https://res.cloudinary.com/test/image/upload/cristo1.jpg");
        image1.setPublicId("cristo1");
        image1.setAltText("Imagen principal del Cristo");
        image1.setDisplayOrder(1);
        image1.setIsMain(true);
        image1.setState(true);
        image1.setPlace(place);

        image2 = new PlaceImage();
        image2.setId(2L);
        image2.setImageUrl("https://res.cloudinary.com/test/image/upload/cristo2.jpg");
        image2.setPublicId("cristo2");
        image2.setAltText("Segunda imagen del Cristo");
        image2.setDisplayOrder(2);
        image2.setIsMain(false);
        image2.setState(true);
        image2.setPlace(place);
    }

    @Test
    void getImagesByPlaceId_WhenPlaceExists_ShouldReturnImages() {
        Long placeId = 1L;

        when(placeRepository.existsById(placeId)).thenReturn(true);
        when(placeImageRepository.findByPlaceIdAndStateTrueOrderByDisplayOrderAsc(placeId))
                .thenReturn(List.of(image1, image2));

        List<PlaceImageResponseDTO> result = placeImageService.getImagesByPlaceId(placeId);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals(
                "https://res.cloudinary.com/test/image/upload/cristo1.jpg",
                result.get(0).getImageUrl()
        );
        assertEquals("cristo1", result.get(0).getPublicId());
        assertEquals("Imagen principal del Cristo", result.get(0).getAltText());
        assertEquals(1, result.get(0).getDisplayOrder());
        assertTrue(result.get(0).getIsMain());

        assertEquals(2L, result.get(1).getId());
        assertEquals(
                "https://res.cloudinary.com/test/image/upload/cristo2.jpg",
                result.get(1).getImageUrl()
        );
        assertEquals("cristo2", result.get(1).getPublicId());
        assertEquals("Segunda imagen del Cristo", result.get(1).getAltText());
        assertEquals(2, result.get(1).getDisplayOrder());
        assertFalse(result.get(1).getIsMain());

        verify(placeRepository).existsById(placeId);
        verify(placeImageRepository)
                .findByPlaceIdAndStateTrueOrderByDisplayOrderAsc(placeId);
    }

    @Test
    void getImagesByPlaceId_WhenPlaceExistsButHasNoImages_ShouldReturnEmptyList() {
        Long placeId = 1L;

        when(placeRepository.existsById(placeId)).thenReturn(true);
        when(placeImageRepository.findByPlaceIdAndStateTrueOrderByDisplayOrderAsc(placeId))
                .thenReturn(List.of());

        List<PlaceImageResponseDTO> result = placeImageService.getImagesByPlaceId(placeId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(placeRepository).existsById(placeId);
        verify(placeImageRepository)
                .findByPlaceIdAndStateTrueOrderByDisplayOrderAsc(placeId);
    }

    @Test
    void getImagesByPlaceId_WhenPlaceDoesNotExist_ShouldThrowResourceNotFoundException() {
        Long placeId = 99L;

        when(placeRepository.existsById(placeId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> placeImageService.getImagesByPlaceId(placeId)
        );

        assertEquals("Lugar no encontrado con ID: 99", exception.getMessage());

        verify(placeRepository).existsById(placeId);
        verify(placeImageRepository, never())
                .findByPlaceIdAndStateTrueOrderByDisplayOrderAsc(anyLong());
    }

    @Test
    void deleteImage_WhenImageExists_ShouldSetStateFalseAndSaveImage() {
        Long imageId = 1L;

        when(placeImageRepository.findById(imageId)).thenReturn(Optional.of(image1));

        placeImageService.deleteImage(imageId);

        assertFalse(image1.isState());

        verify(placeImageRepository).findById(imageId);
        verify(placeImageRepository).save(image1);
    }

    @Test
    void deleteImage_WhenImageDoesNotExist_ShouldThrowResourceNotFoundException() {
        Long imageId = 99L;

        when(placeImageRepository.findById(imageId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> placeImageService.deleteImage(imageId)
        );

        assertEquals("Imagen no encontrada con ID: 99", exception.getMessage());

        verify(placeImageRepository).findById(imageId);
        verify(placeImageRepository, never()).save(any(PlaceImage.class));
    }
}