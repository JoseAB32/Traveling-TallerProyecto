package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.TranslationResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TranslationProviderService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationProviderService.class);

    private final AzureTranslatorService azureTranslatorService;
    private final GoogleTranslatorService googleTranslatorService;

    public TranslationProviderService(
            AzureTranslatorService azureTranslatorService,
            GoogleTranslatorService googleTranslatorService) {
        this.azureTranslatorService = azureTranslatorService;
        this.googleTranslatorService = googleTranslatorService;
    }

    public TranslationResultDTO translate(String text, String sourceLanguage, String targetLanguage) {
        try {
            return azureTranslatorService.translate(text, sourceLanguage, targetLanguage);
        } catch (Exception azureException) {
            logger.warn("{} [{}] Azure Translator falló. Intentando con Google Translator. Motivo: {}",
                    AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS, azureException.getMessage());
        }

        try {
            return googleTranslatorService.translate(text, sourceLanguage, targetLanguage);
        } catch (Exception googleException) {
            logger.error("{} [{}] Google Translator también falló. Se devolverá el texto original. Motivo: {}",
                    AppConstants.PREFIX_TRANSLATION, AppConstants.LOG_TRANSLATIONS, googleException.getMessage());
            return new TranslationResultDTO(text, AppConstants.PROVIDER_ORIGINAL);
        }
    }
}