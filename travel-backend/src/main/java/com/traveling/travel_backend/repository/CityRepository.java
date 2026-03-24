package com.traveling.travel_backend.repository;

import com.traveling.travel_backend.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    
    boolean existsByName(String name);
    java.util.List<City> findByStateTrue();
}