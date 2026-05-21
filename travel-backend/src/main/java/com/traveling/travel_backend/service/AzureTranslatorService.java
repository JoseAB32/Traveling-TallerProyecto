package com.traveling.travel_backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.traveling.travel_backend.dto.TranslationResultDTO;

@Service
public class AzureTranslatorService {

    private static final String PROVIDER_NAME = "AZURE";

    private final RestTemplate restTemplate;
    private final String key;
    private final String endpoint;
    private final String region;

    public AzureTranslatorService(
            @Value("${translator.azure.key}") String key,
            @Value("${translator.azure.endpoint}") String endpoint,
            @Value("${translator.azure.region}") String region
    ) {
        this.key = key;
        this.endpoint = endpoint;
        this.region = region;
        this.restTemplate = new RestTemplate();
    }

    public TranslationResultDTO translate(
            String text,
            String sourceLanguage,
            String targetLanguage
    ) {
        String url = endpoint
                + "/translate?api-version=3.0"
                + "&from=" + sourceLanguage
                + "&to=" + targetLanguage;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Ocp-Apim-Subscription-Key", key);
        headers.set("Ocp-Apim-Subscription-Region", region);

        List<Map<String, String>> requestBody = List.of(
                Map.of("Text", text)
        );

        HttpEntity<List<Map<String, String>>> requestEntity =
                new HttpEntity<>(requestBody, headers);

        List<?> response = restTemplate.postForObject(
                url,
                requestEntity,
                List.class
        );

        String translatedText = extractTranslatedText(response);

        return new TranslationResultDTO(translatedText, PROVIDER_NAME);
    }

    private String extractTranslatedText(List<?> response) {
        if (response == null || response.isEmpty()) {
            throw new IllegalStateException("Azure Translator returned empty response");
        }

        Object firstItem = response.get(0);

        if (!(firstItem instanceof Map<?, ?> firstResponseItem)) {
            throw new IllegalStateException("Azure Translator returned invalid response format");
        }

        Object translationsObject = firstResponseItem.get("translations");

        if (!(translationsObject instanceof List<?> translations)
                || translations.isEmpty()) {
            throw new IllegalStateException("Azure Translator returned no translations");
        }

        Object firstTranslationObject = translations.get(0);

        if (!(firstTranslationObject instanceof Map<?, ?> firstTranslation)) {
            throw new IllegalStateException("Azure Translator returned invalid translation format");
        }

        Object translatedText = firstTranslation.get("text");

        if (translatedText == null) {
            throw new IllegalStateException("Azure Translator returned translation without text");
        }

        return translatedText.toString();
    }
}