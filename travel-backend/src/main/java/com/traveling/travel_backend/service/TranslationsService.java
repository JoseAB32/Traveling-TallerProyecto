package com.traveling.travel_backend.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.TranslationPageResponseDTO;
import com.traveling.travel_backend.dto.TranslationResponseDTO;
import com.traveling.travel_backend.dto.TranslationResultDTO;
import com.traveling.travel_backend.dto.UpdateTranslationRequestDTO;
import com.traveling.travel_backend.exception.BadRequestException;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Translations;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.TranslationRepository;

import org.springframework.data.domain.Pageable;

import jakarta.transaction.Transactional;

@Service
public class TranslationsService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationsService.class);

    private final TranslationRepository translationRepository;
    private final LogRepository logRepository;
    private final TranslationProviderService translationProviderService;

    public TranslationsService(
            TranslationRepository translationRepository,
            LogRepository logRepository,
            TranslationProviderService translationProviderService) {
        this.translationRepository = translationRepository;
        this.logRepository = logRepository;
        this.translationProviderService = translationProviderService;
    }

    @Transactional
    public String getTranslation(
            String entityType,
            Long entityId,
            String fieldName,
            String targetLang,
            String originalText) {
        logger.info("{} [{}] Buscando traducción para {} con ID {} en campo '{}' y lenguaje '{}'",
                AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS, entityType, entityId, fieldName, targetLang);

        logRepository.save(new LogEntity(AppConstants.LOG_TRANSLATIONS, AppConstants.LOG_INFO,
                "Buscando traducción para " + entityType + " con ID " + entityId
                        + " en campo '" + fieldName + "' y lenguaje '" + targetLang + "'", null));

        Optional<Translations> existing = translationRepository.findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                entityType, entityId, fieldName, targetLang);

        if (existing.isPresent()) {
            logger.debug("{} [{}] Traducción encontrada para {} ID: {}",
                    AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS, entityType, entityId);
            return existing.get().getTranslatedText();
        }

        TranslationResultDTO translationResult = translationProviderService.translate(
                originalText, AppConstants.DEFAULT_LANGUAGE, targetLang);

        String translatedText = translationResult.getTranslatedText();

        Translations newTranslation = new Translations();
        newTranslation.setEntityType(entityType);
        newTranslation.setEntityId(entityId);
        newTranslation.setFieldName(fieldName);
        newTranslation.setLanguage(targetLang);
        newTranslation.setTranslatedText(translatedText);

        translationRepository.save(newTranslation);

        logger.debug("{} [{}] Traducción guardada para {} ID: {} usando proveedor {}",
                AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS,
                entityType, entityId, translationResult.getProvider());

        logRepository.save(new LogEntity(AppConstants.LOG_TRANSLATIONS, AppConstants.LOG_INFO,
                "Traducción guardada para " + entityType + " con ID " + entityId
                        + " en campo '" + fieldName + "' usando proveedor " + translationResult.getProvider(), null));

        return translatedText;
    }

    @Transactional
    public TranslationPageResponseDTO getTranslations(
                String entityType,
                String language,
                String fieldName,
                Long entityId,
                int page,
                int size) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 20 : Math.min(size, 100);

        logger.info("{} [{}] Obteniendo traducciones con filtros entityType='{}', language='{}', fieldName='{}', entityId='{}'",
                AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS, entityType, language, fieldName, entityId);

        logRepository.save(new LogEntity(AppConstants.LOG_TRANSLATIONS, AppConstants.LOG_INFO,
                "Obteniendo traducciones con filtros entityType='" + entityType + "', language='" + language
                        + "', fieldName='" + fieldName + "', entityId='" + entityId + "'", null));

        Specification<Translations> specification = buildTranslationSpecification(entityType, language, fieldName, entityId);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<TranslationResponseDTO> translationsPage = translationRepository.findAll(specification, pageable)
                .map(TranslationResponseDTO::fromEntity);

        TranslationPageResponseDTO response = new TranslationPageResponseDTO();
        response.setContent(translationsPage.getContent());
        response.setPage(translationsPage.getNumber());
        response.setSize(translationsPage.getSize());
        response.setTotalElements(translationsPage.getTotalElements());
        response.setTotalPages(translationsPage.getTotalPages());
        response.setHasNext(translationsPage.hasNext());

        return response;
    }

    private Specification<Translations> buildTranslationSpecification(
    String entityType,
    String language,
    String fieldName,
    Long entityId) {
        return (root, query, criteriaBuilder) -> {
                var predicate = criteriaBuilder.conjunction();

                if (entityType != null && !entityType.trim().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("entityType"), entityType.trim()));
                }

                if (language != null && !language.trim().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("language"), language.trim()));
                }

                if (fieldName != null && !fieldName.trim().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("fieldName"), fieldName.trim()));
                }

                if (entityId != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("entityId"), entityId));
                }

                return predicate;
        };
    }

    @Transactional
    public TranslationResponseDTO updateTranslation(Long id, UpdateTranslationRequestDTO request) {
        if (request == null) {
                throw new BadRequestException("La solicitud de actualización de traducción es obligatoria");
        }

        if (request.getTranslatedText() == null || request.getTranslatedText().trim().isEmpty()) {
                throw new BadRequestException("El texto traducido es obligatorio");
        }

        logger.info("{} [{}] Actualizando traducción con ID: {}",
                AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS, id);

        logRepository.save(new LogEntity(AppConstants.LOG_TRANSLATIONS, AppConstants.LOG_INFO,
                "Actualizando traducción con ID: " + id, null));

        Translations translation = translationRepository.findById(id)
                .orElseThrow(() -> {
                        logger.warn("{} [{}] Traducción con ID {} no encontrada",
                                AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS, id);
                        logRepository.save(new LogEntity(AppConstants.LOG_TRANSLATIONS, AppConstants.LOG_WARN,
                                "Traducción con ID " + id + " no encontrada", null));
                        return new ResourceNotFoundException("Traducción no encontrada con ID: " + id);
                });

        translation.setTranslatedText(request.getTranslatedText().trim());

        Translations updatedTranslation = translationRepository.save(translation);

        logger.debug("{} [{}] Traducción con ID {} actualizada correctamente",
                AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS, id);

        logRepository.save(new LogEntity(AppConstants.LOG_TRANSLATIONS, AppConstants.LOG_INFO,
                "Traducción con ID " + id + " actualizada correctamente", null));

        return TranslationResponseDTO.fromEntity(updatedTranslation);
    }
}