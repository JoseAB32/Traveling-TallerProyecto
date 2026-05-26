package com.traveling.travel_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.traveling.travel_backend.model.PlaceImage;

public interface PlaceImageRepository extends JpaRepository<PlaceImage, Long> {

    List<PlaceImage> findByPlaceIdAndStateTrueOrderByDisplayOrderAsc(Long placeId);
}