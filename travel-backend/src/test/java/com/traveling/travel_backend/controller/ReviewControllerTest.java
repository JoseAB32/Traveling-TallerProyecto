package com.traveling.travel_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.dto.CreateReviewRequestDTO;
import com.traveling.travel_backend.dto.ReviewPageResponseDTO;
import com.traveling.travel_backend.dto.ReviewResponseDTO;
import com.traveling.travel_backend.security.JwtService;
import com.traveling.travel_backend.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/reviews/place/{placeId} retorna reseñas paginadas")
    void getPlaceReviewsReturnsPagedData() throws Exception {
        ReviewResponseDTO review = new ReviewResponseDTO();
        review.setId(1L);
        review.setComment("Excelente");
        review.setScore(5);
        review.setCreatedAt(OffsetDateTime.now());
        review.setState(true);

        ReviewPageResponseDTO pageResponse = new ReviewPageResponseDTO();
        pageResponse.setContent(List.of(review));
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(1);
        pageResponse.setTotalPages(1);
        pageResponse.setHasNext(false);

        when(reviewService.getPlaceReviews(3L, 0, 10, "es")).thenReturn(pageResponse);

        mockMvc.perform(get("/api/reviews/place/3?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("GET /api/reviews/place/{placeId} retorna reseñas paginadas usando X-Language")
    void getPlaceReviewsReturnsPagedDataWithLanguageHeader() throws Exception {
        ReviewResponseDTO review = new ReviewResponseDTO();
        review.setId(1L);
        review.setComment("Excellent");
        review.setScore(5);
        review.setCreatedAt(OffsetDateTime.now());
        review.setState(true);

        ReviewPageResponseDTO pageResponse = new ReviewPageResponseDTO();
        pageResponse.setContent(List.of(review));
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(1);
        pageResponse.setTotalPages(1);
        pageResponse.setHasNext(false);

        when(reviewService.getPlaceReviews(3L, 0, 10, "en")).thenReturn(pageResponse);

        mockMvc.perform(get("/api/reviews/place/3?page=0&size=10")
                        .header("X-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].comment").value("Excellent"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("GET /api/reviews/{reviewId}/replies retorna respuestas paginadas")
    void getReviewRepliesReturnsPagedData() throws Exception {
        ReviewResponseDTO reply = new ReviewResponseDTO();
        reply.setId(15L);
        reply.setComment("Respuesta");
        reply.setCreatedAt(OffsetDateTime.now());
        reply.setState(true);

        ReviewPageResponseDTO pageResponse = new ReviewPageResponseDTO();
        pageResponse.setContent(List.of(reply));
        pageResponse.setPage(0);
        pageResponse.setSize(2);
        pageResponse.setTotalElements(3);
        pageResponse.setTotalPages(2);
        pageResponse.setHasNext(true);

        when(reviewService.getReviewReplies(10L, 0, 2, "es")).thenReturn(pageResponse);

        mockMvc.perform(get("/api/reviews/10/replies?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    @DisplayName("POST /api/reviews crea reseña y retorna 201")
    void createReviewReturnsCreated() throws Exception {
        CreateReviewRequestDTO request = new CreateReviewRequestDTO();
        request.setPlaceId(3L);
        request.setComment("Muy lindo");
        request.setScore(4);

        ReviewResponseDTO response = new ReviewResponseDTO();
        response.setId(12L);
        response.setComment("Muy lindo");
        response.setScore(4);
        response.setCreatedAt(OffsetDateTime.now());
        response.setState(true);

        when(reviewService.createReview(any(CreateReviewRequestDTO.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.comment").value("Muy lindo"))
                .andExpect(jsonPath("$.score").value(4));
    }

    @Test
    @DisplayName("GET /api/reviews/mejor-resenia retorna 204 si no existe reseña")
    void getBestReviewReturnsNoContentWhenEmpty() throws Exception {
        when(reviewService.getBestReview(99L, "es")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reviews/mejor-resenia?placeId=99"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/reviews/mejor-resenia retorna 204 usando X-Language si no existe reseña")
    void getBestReviewReturnsNoContentWithLanguageHeaderWhenEmpty() throws Exception {
        when(reviewService.getBestReview(99L, "en")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reviews/mejor-resenia?placeId=99")
                        .header("X-Language", "en"))
                .andExpect(status().isNoContent());
    }
}