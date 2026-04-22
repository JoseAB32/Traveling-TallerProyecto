package com.traveling.travel_backend.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.traveling.travel_backend.model.Trip;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findFirstByUserIdAndStateTrueOrderByIdDesc(Long userId);
    List<Trip> findByUserIdAndStateTrue(Long userId);
}
