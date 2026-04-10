package com.traveling.travel_backend.controller;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Review;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.ReviewRepository;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping(AppConstants.API_BASE_PATH + AppConstants.REVIEWS_ENDPOINT)
@CrossOrigin(origins = AppConstants.CORS_LOCALHOST)
public class reviewContoller {
    
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private LogRepository logRepository;

    private static final Logger logger = LoggerFactory.getLogger(placeController.class);


    @GetMapping("/mejor-resenia")
    public Optional<Review> getBestReview(@RequestParam Long placeId) {
        
        String logMessage = "Solicitando la mejor reseña para el lugar (ID): " + placeId + " - GET /api/places/mejor-resenia";
        
        logger.info(AppConstants.PREFIX_PLACE + " [" + AppConstants.LOG_REVIEWS + "] " + logMessage);
        logRepository.save(new LogEntity(AppConstants.LOG_REVIEWS, AppConstants.LOG_INFO, logMessage, null));

        Optional<Review> bestReview = reviewRepository.findFirstByPlaceIdAndStateTrueOrderByScoreDesc(placeId);

        logger.debug(AppConstants.PREFIX_PLACE + " [" + AppConstants.LOG_REVIEWS + "] Mejor reseña encontrada para ID {}: {}", placeId, bestReview);

        return bestReview;
    }
    




}
