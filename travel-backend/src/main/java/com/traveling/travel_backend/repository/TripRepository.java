package com.traveling.travel_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.traveling.travel_backend.model.Trip;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

}
