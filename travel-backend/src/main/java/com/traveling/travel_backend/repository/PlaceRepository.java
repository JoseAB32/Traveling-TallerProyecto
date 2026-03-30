package com.traveling.travel_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.traveling.travel_backend.model.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long>{
    List<Place> findTop5ByOrderByRatingDesc();
    List<Place> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCity_NameContainingIgnoreCase(
        String name, String address, String cityName
    );
}
