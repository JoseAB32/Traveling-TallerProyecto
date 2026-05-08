package com.traveling.travel_backend.service;

import com.traveling.travel_backend.dto.CreateReviewRequestDTO;
import com.traveling.travel_backend.dto.ReviewPageResponseDTO;
import com.traveling.travel_backend.dto.ReviewResponseDTO;
import com.traveling.travel_backend.exception.BadRequestException;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.model.Review;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.PlaceRepository;
import com.traveling.travel_backend.repository.ReviewRepository;
import com.traveling.travel_backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("Debe devolver reseñas paginadas con metadatos correctos")
    void getPlaceReviewsReturnsPagedResponse() {
        Review review = new Review();
        review.setId(10L);
        review.setComment("Excelente lugar");
        review.setScore(5);
        review.setState(true);
        review.setCreatedAt(OffsetDateTime.now());

        User user = new User();
        user.setId(7L);
        user.setUserName("peter");
        review.setUser(user);

        Place place = new Place();
        place.setId(2L);
        review.setPlace(place);

        Page<Review> page = new PageImpl<>(List.of(review), PageRequest.of(0, 10), 1);

        when(reviewRepository.findByPlaceIdAndStateTrueAndParentIsNullOrderByCreatedAtDesc(2L, PageRequest.of(0, 10)))
                .thenReturn(page);
        when(logRepository.save(any(LogEntity.class))).thenReturn(null);

        ReviewPageResponseDTO response = reviewService.getPlaceReviews(2L, 0, 10);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertFalse(response.isHasNext());
    }

    @Test
    @DisplayName("Debe lanzar bad request cuando comentario está vacío")
    void createReviewThrowsBadRequestWhenCommentIsEmpty() {
        CreateReviewRequestDTO request = new CreateReviewRequestDTO();
        request.setUserId(1L);
        request.setPlaceId(2L);
        request.setScore(4);
        request.setComment("   ");

        assertThrows(BadRequestException.class, () -> reviewService.createReview(request));
    }

    @Test
    @DisplayName("Debe crear reseña raíz cuando los datos son válidos")
    void createReviewCreatesRootReviewSuccessfully() {
        CreateReviewRequestDTO request = new CreateReviewRequestDTO();
        request.setUserId(1L);
        request.setPlaceId(2L);
        request.setParentId(null);
        request.setComment("Muy buen lugar");
        request.setScore(5);

        User user = new User();
        user.setId(1L);
        user.setUserName("erika");
        user.setState(true);

        Place place = new Place();
        place.setId(2L);

        Review saved = new Review();
        saved.setId(20L);
        saved.setUser(user);
        saved.setPlace(place);
        saved.setComment("Muy buen lugar");
        saved.setScore(5);
        saved.setState(true);
        saved.setCreatedAt(OffsetDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(placeRepository.findById(2L)).thenReturn(Optional.of(place));
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);
        when(logRepository.save(any(LogEntity.class))).thenReturn(null);

        ReviewResponseDTO response = reviewService.createReview(request);

        assertEquals(20L, response.getId());
        assertEquals("Muy buen lugar", response.getComment());
        assertEquals(5, response.getScore());
        assertTrue(response.isState());

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository, times(1)).save(captor.capture());
        assertTrue(captor.getValue().getParent() == null);
    }
}
