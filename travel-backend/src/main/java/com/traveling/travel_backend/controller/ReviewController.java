package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.CreateReviewRequestDTO;
import com.traveling.travel_backend.dto.ReviewPageResponseDTO;
import com.traveling.travel_backend.dto.ReviewResponseDTO;
import com.traveling.travel_backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + AppConstants.REVIEWS_ENDPOINT)
@CrossOrigin(origins = { AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY })
@Tag(name = "Review", description = "Gestion de reseñas por lugar")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Get best place review", description = "Returns the best review of a specific place", operationId = "getBestReview")
    @GetMapping("/mejor-resenia")
    public ResponseEntity<ReviewResponseDTO> getBestReview(@RequestParam Long placeId) {
        return reviewService.getBestReview(placeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Get paginated reviews by place", description = "Returns active root reviews ordered by latest created date", operationId = "getPlaceReviews")
    @GetMapping("/place/{placeId}")
    public ResponseEntity<ReviewPageResponseDTO> getPlaceReviews(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getPlaceReviews(placeId, page, size));
    }

    @Operation(summary = "Create place review", description = "Creates a new review for a place", operationId = "createReview")
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody CreateReviewRequestDTO request, Authentication authentication) {
        ReviewResponseDTO createdReview = reviewService.createReview(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }
}
