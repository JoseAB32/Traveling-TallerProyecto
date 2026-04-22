package com.traveling.travel_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.traveling.travel_backend.model.TripItem;

@Repository
public interface TripItemRepository extends JpaRepository<TripItem, Long> {
    List<TripItem> findByTripIdAndStateTrueOrderByVisitOrderAsc(Long tripId);

    @Modifying
    @Transactional
    void deleteByTripId(Long tripId);
}
