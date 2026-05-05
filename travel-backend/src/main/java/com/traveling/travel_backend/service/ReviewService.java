package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.ReviewResponseDTO;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final LogRepository logRepository;

    public ReviewService(ReviewRepository reviewRepository, LogRepository logRepository) {
        this.reviewRepository = reviewRepository;
        this.logRepository    = logRepository;
    }

    @Transactional
    public Optional<ReviewResponseDTO> getBestReview(Long placeId) {
        String logMessage = "Solicitando la mejor resena para el lugar (ID): " + placeId + " - GET /api/reviews/mejor-resenia";

        logger.info("[{}] {}", AppConstants.LOG_REVIEWS, logMessage);
        logRepository.save(new LogEntity(AppConstants.LOG_REVIEWS, AppConstants.LOG_INFO, logMessage, null));

        Optional<ReviewResponseDTO> result = reviewRepository
                .findFirstByPlaceIdAndStateTrueOrderByScoreDesc(placeId)
                .map(ReviewResponseDTO::fromEntity);

        logger.debug("[{}] Mejor resena para lugar ID {}: {}", AppConstants.LOG_REVIEWS, placeId, result.isPresent() ? "encontrada" : "no encontrada");

        return result;
    }
}