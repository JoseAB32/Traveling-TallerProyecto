package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.CreateReviewRequestDTO;
import com.traveling.travel_backend.dto.ReviewPageResponseDTO;
import com.traveling.travel_backend.dto.ReviewResponseDTO;
import com.traveling.travel_backend.exception.BadRequestException;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.exception.UnauthorizedException;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.model.Review;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.PlaceRepository;
import com.traveling.travel_backend.repository.ReviewRepository;
import com.traveling.travel_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final LogRepository logRepository;
    private final TranslationsService translationsService;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            PlaceRepository placeRepository,
            LogRepository logRepository,
            TranslationsService translationsService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
        this.logRepository = logRepository;
        this.translationsService = translationsService;
    }

    @Transactional
    public Optional<ReviewResponseDTO> getBestReview(Long placeId, String language) {
        String logMessage = "Solicitando la mejor resena para el lugar (ID): " + placeId + " - GET /api/reviews/mejor-resenia";

        logger.info("[{}] {}", AppConstants.LOG_REVIEWS, logMessage);
        logRepository.save(new LogEntity(AppConstants.LOG_REVIEWS, AppConstants.LOG_INFO, logMessage, null));

        Optional<ReviewResponseDTO> result = reviewRepository
                .findFirstByPlaceIdAndStateTrueAndParentIsNullOrderByScoreDesc(placeId)
                .map(review -> buildReviewResponseDTO(review, language));

        logger.debug("[{}] Mejor resena para lugar ID {}: {}", AppConstants.LOG_REVIEWS, placeId, result.isPresent() ? "encontrada" : "no encontrada");

        return result;
    }

    @Transactional
    public ReviewPageResponseDTO getPlaceReviews(Long placeId, int page, int size, String language) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 10 : Math.min(size, 50);

        String logMessage = "Solicitando reseñas paginadas para lugar (ID): " + placeId
                + " - GET /api/reviews/place/" + placeId + "?page=" + safePage + "&size=" + safeSize;

        logger.info("[{}] {}", AppConstants.LOG_REVIEWS, logMessage);
        logRepository.save(new LogEntity(AppConstants.LOG_REVIEWS, AppConstants.LOG_INFO, logMessage, null));

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<ReviewResponseDTO> reviewsPage = reviewRepository
                .findByPlaceIdAndStateTrueAndParentIsNullOrderByCreatedAtDesc(placeId, pageable)
                .map(review -> buildReviewResponseDTO(review, language));

        ReviewPageResponseDTO response = new ReviewPageResponseDTO();
        response.setContent(reviewsPage.getContent());
        response.setPage(reviewsPage.getNumber());
        response.setSize(reviewsPage.getSize());
        response.setTotalElements(reviewsPage.getTotalElements());
        response.setTotalPages(reviewsPage.getTotalPages());
        response.setHasNext(reviewsPage.hasNext());

        logger.debug("[{}] Reseñas paginadas para lugar ID {}: {} elementos en página {}",
                AppConstants.LOG_REVIEWS, placeId, reviewsPage.getNumberOfElements(), reviewsPage.getNumber());

        return response;
    }

    @Transactional
    public ReviewPageResponseDTO getReviewReplies(Long reviewId, int page, int size, String language) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 2 : size;

        String logMessage = "Solicitando respuestas paginadas para reseña (ID): " + reviewId
                + " - GET /api/reviews/" + reviewId + "/replies?page=" + safePage + "&size=" + safeSize;

        logger.info("[{}] {}", AppConstants.LOG_REVIEWS, logMessage);
        logRepository.save(new LogEntity(AppConstants.LOG_REVIEWS, AppConstants.LOG_INFO, logMessage, null));

        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Reseña no encontrada");
        }

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<ReviewResponseDTO> repliesPage = reviewRepository
                .findByParentIdAndStateTrueOrderByCreatedAtDesc(reviewId, pageable)
                .map(review -> buildReviewResponseDTO(review, language));

        ReviewPageResponseDTO response = new ReviewPageResponseDTO();
        response.setContent(repliesPage.getContent());
        response.setPage(repliesPage.getNumber());
        response.setSize(repliesPage.getSize());
        response.setTotalElements(repliesPage.getTotalElements());
        response.setTotalPages(repliesPage.getTotalPages());
        response.setHasNext(repliesPage.hasNext());

        return response;
    }

    @Transactional
    public ReviewResponseDTO createReview(CreateReviewRequestDTO request, Authentication authentication) {
        validateCreateReviewRequest(request);

        User user = resolveAuthenticatedUser(authentication);

        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Lugar no encontrado"));

        Review review = new Review();
        review.setUser(user);
        review.setPlace(place);
        review.setComment(request.getComment().trim());
        review.setState(true);

        if (request.getParentId() != null) {
            Review parentReview = reviewRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reseña padre no encontrada"));

            if (parentReview.getPlace() == null || parentReview.getPlace().getId() != place.getId()) {
                throw new BadRequestException("La reseña padre no pertenece al lugar indicado");
            }

            review.setParent(parentReview);
            review.setScore(null);
        } else {
            review.setScore(request.getScore());
        }

        Review savedReview = reviewRepository.save(review);

        String logMessage = "Reseña creada para lugar (ID): " + place.getId() + " por usuario (ID): " + user.getId()
                + " - POST /api/reviews";

        logger.info("[{}] {}", AppConstants.LOG_REVIEWS, logMessage);
        logRepository.save(new LogEntity(AppConstants.LOG_REVIEWS, AppConstants.LOG_INFO, logMessage, user.getId()));

        return ReviewResponseDTO.fromEntity(savedReview);
    }

    private ReviewResponseDTO buildReviewResponseDTO(Review review, String language) {
        ReviewResponseDTO reviewResponseDTO = ReviewResponseDTO.fromEntity(review);

        if (isSourceLanguage(language) || review.getComment() == null || review.getComment().trim().isEmpty()) {
            return reviewResponseDTO;
        }

        String translatedComment = translationsService.getTranslation(
                AppConstants.ENTITY_TYPE_REVIEW,
                review.getId(),
                AppConstants.FIELD_COMMENT,
                language,
                review.getComment()
        );

        reviewResponseDTO.setComment(translatedComment);

        return reviewResponseDTO;
    }

    private boolean isSourceLanguage(String language) {
        return language == null || AppConstants.DEFAULT_LANGUAGE.equalsIgnoreCase(language);
    }

    private void validateCreateReviewRequest(CreateReviewRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("La solicitud de reseña es obligatoria");
        }

        if (request.getPlaceId() == null) {
            throw new BadRequestException("El placeId es obligatorio");
        }

        if (request.getComment() == null || request.getComment().trim().isEmpty()) {
            throw new BadRequestException("El comentario es obligatorio");
        }

        if (request.getParentId() == null && (request.getScore() == null || request.getScore() < 1 || request.getScore() > 5)) {
            throw new BadRequestException("El puntaje debe estar entre 1 y 5");
        }

        if (request.getParentId() != null && request.getScore() != null && (request.getScore() < 1 || request.getScore() > 5)) {
            throw new BadRequestException("Si se envía puntaje en respuesta, debe estar entre 1 y 5");
        }
    }

    private User resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("No autenticado.");
        }

        return userRepository.findByUserNameAndStateTrue(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Usuario autenticado no válido."));
    }
}