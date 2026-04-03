package com.traveling.travel_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.traveling.travel_backend.model.LogEntity;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, Long> {
    List<LogEntity> findByModuleOrderByTimestampDesc(String module);
    
    List<LogEntity> findByModuleAndLevelAndTimestampBetweenOrderByTimestampDesc(
        String module, 
        String level, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
}
