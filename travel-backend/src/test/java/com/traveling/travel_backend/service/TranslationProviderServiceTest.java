package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.TranslationResultDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationProviderServiceTest {

    @Mock
    private AzureTranslatorService azureTranslatorService;

    @Mock
    private GoogleTranslatorService googleTranslatorService;

    @InjectMocks
    private TranslationProviderService translationProviderService;

    @Test
    @DisplayName("Debe usar Azure cuando Azure responde correctamente")
    void shouldUseAzureWhenAzureWorks() {
        TranslationResultDTO azureResult = new TranslationResultDTO("Hello world", AppConstants.PROVIDER_AZURE);

        when(azureTranslatorService.translate("Hola mundo", "es", "en")).thenReturn(azureResult);

        TranslationResultDTO result = translationProviderService.translate("Hola mundo", "es", "en");

        assertEquals("Hello world", result.getTranslatedText());
        assertEquals(AppConstants.PROVIDER_AZURE, result.getProvider());

        verify(azureTranslatorService).translate("Hola mundo", "es", "en");
        verifyNoInteractions(googleTranslatorService);
    }

    @Test
    @DisplayName("Debe usar Google cuando Azure falla")
    void shouldUseGoogleWhenAzureFails() {
        TranslationResultDTO googleResult = new TranslationResultDTO("Hello world", AppConstants.PROVIDER_GOOGLE);

        when(azureTranslatorService.translate("Hola mundo", "es", "en"))
                .thenThrow(new RuntimeException("Azure error"));
        when(googleTranslatorService.translate("Hola mundo", "es", "en")).thenReturn(googleResult);

        TranslationResultDTO result = translationProviderService.translate("Hola mundo", "es", "en");

        assertEquals("Hello world", result.getTranslatedText());
        assertEquals(AppConstants.PROVIDER_GOOGLE, result.getProvider());

        verify(azureTranslatorService).translate("Hola mundo", "es", "en");
        verify(googleTranslatorService).translate("Hola mundo", "es", "en");
    }

    @Test
    @DisplayName("Debe devolver texto original cuando Azure y Google fallan")
    void shouldReturnOriginalTextWhenBothProvidersFail() {
        when(azureTranslatorService.translate("Hola mundo", "es", "en"))
                .thenThrow(new RuntimeException("Azure error"));
        when(googleTranslatorService.translate("Hola mundo", "es", "en"))
                .thenThrow(new RuntimeException("Google error"));

        TranslationResultDTO result = translationProviderService.translate("Hola mundo", "es", "en");

        assertEquals("Hola mundo", result.getTranslatedText());
        assertEquals(AppConstants.PROVIDER_ORIGINAL, result.getProvider());

        verify(azureTranslatorService).translate("Hola mundo", "es", "en");
        verify(googleTranslatorService).translate("Hola mundo", "es", "en");
    }
}