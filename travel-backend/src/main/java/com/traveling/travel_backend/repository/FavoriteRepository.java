package com.traveling.travel_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.traveling.travel_backend.model.Favorite;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long>{
    List<Favorite> findByUserIdAndStateTrue(Long userId);
}