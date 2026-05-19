package com.traveling.travel_backend.service;

import java.util.Optional;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Translations;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.TranslationRepository;

import jakarta.transaction.Transactional;

@Service
public class TranslationsService {
    private static final Logger logger = LoggerFactory.getLogger(TranslationsService.class);

    private final TranslationRepository translationRepository;
    private final LogRepository logRepository;

    public TranslationsService(TranslationRepository translationRepository, LogRepository logRepository) {
        this.translationRepository = translationRepository;
        this.logRepository = logRepository;
    }

    @Transactional
    public String getTranslation(String entityType, Long entityId, String fieldName, String targetLang, String originalText) {
        logger.info("{} [{}] Buscando traducción para {} con ID {} en campo '{}' y lenguaje '{}'", 
                AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS, entityType, entityId, fieldName, targetLang);
        logRepository.save(new LogEntity(AppConstants.LOG_TRANSLATIONS, AppConstants.LOG_INFO,
                "Buscando traducción para " + entityType + " con ID " + entityId + " en campo '" + fieldName + "' y lenguaje '" + targetLang + "'", null));
        Optional<Translations> existing = translationRepository.findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                entityType, entityId, fieldName, targetLang);

        if (existing.isPresent()) {
            logger.debug("{} [{}] Traducción encontrada para {} ID: {}", 
                    AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS, entityType, entityId);
            return existing.get().getTranslatedText();
        }

        String translatedText = callExternalTranslateApi(originalText, targetLang);

        Translations newTranslation = new Translations();
        newTranslation.setEntityType(entityType);
        newTranslation.setEntityId(entityId);
        newTranslation.setFieldName(fieldName);
        newTranslation.setLanguage(targetLang);
        newTranslation.setTranslatedText(translatedText);
        
        translationRepository.save(newTranslation);
        logger.debug("{} [{}] Traducción guardada para {} ID: {}", 
                AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS, entityType, entityId);

        return translatedText;
    }

    private String callExternalTranslateApi(String text, String lang) {
        // Aquí llamarías a DeepL o Google Translate. 
        // Por ahora simulamos la respuesta:
        return "[Traducido a " + lang + "] " + text;
    }
}
