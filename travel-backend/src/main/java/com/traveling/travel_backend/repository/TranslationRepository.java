package com.traveling.travel_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.traveling.travel_backend.model.Translations;

@Repository
public interface TranslationRepository extends JpaRepository<Translations, Long> {

    Optional<Translations> findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
            String entityType, Long entityId, String fieldName, String language);

    List<Translations> findByEntityTypeAndLanguage(String entityType, String language);
    
    List<Translations> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
