package com.traveling.travel_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.traveling.travel_backend.model.Tranlations;

@Repository
public interface TranslationRepository extends JpaRepository<Tranlations, Long> {

    Optional<Tranlations> findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
            String entityType, Long entityId, String fieldName, String language);

    List<Tranlations> findByEntityTypeAndLanguage(String entityType, String language);
    
    List<Tranlations> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
