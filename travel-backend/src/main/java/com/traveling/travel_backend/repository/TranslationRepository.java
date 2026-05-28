package com.traveling.travel_backend.repository;

import com.traveling.travel_backend.model.Translations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TranslationRepository extends JpaRepository<Translations, Long>, JpaSpecificationExecutor<Translations> {

    Optional<Translations> findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
            String entityType, Long entityId, String fieldName, String language);

    List<Translations> findByEntityTypeAndLanguage(String entityType, String language);

    List<Translations> findByEntityTypeAndEntityId(String entityType, Long entityId);
}