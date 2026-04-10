package com.traveling.travel_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.traveling.travel_backend.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findFirstByPlaceIdAndStateTrueOrderByScoreDesc(Long placeId);
}
