package com.traveling.travel_backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.traveling.travel_backend.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findFirstByPlaceIdAndStateTrueAndParentIsNullOrderByScoreDesc(Long placeId);
    Page<Review> findByPlaceIdAndStateTrueAndParentIsNullOrderByCreatedAtDesc(Long placeId, Pageable pageable);
    Page<Review> findByParentIdAndStateTrueOrderByCreatedAtDesc(Long parentId, Pageable pageable);
}
