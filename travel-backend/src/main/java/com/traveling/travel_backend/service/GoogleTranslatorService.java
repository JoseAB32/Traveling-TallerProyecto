package com.traveling.travel_backend.service;

import com.traveling.travel_backend.dto.TranslationResultDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GoogleTranslatorService {

    private static final String PROVIDER_NAME = "GOOGLE";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String endpoint;

    public GoogleTranslatorService(
            @Value("${translator.google.api-key}") String apiKey,
            @Value("${translator.google.endpoint}") String endpoint) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.restTemplate = new RestTemplate();
    }

    public TranslationResultDTO translate(String text, String sourceLanguage, String targetLanguage) {
        String url = endpoint + "/language/translate/v2?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "q", text,
                "source", sourceLanguage,
                "target", targetLanguage,
                "format", "text"
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        Map<?, ?> response = restTemplate.postForObject(url, requestEntity, Map.class);

        String translatedText = extractTranslatedText(response);

        return new TranslationResultDTO(translatedText, PROVIDER_NAME);
    }

    private String extractTranslatedText(Map<?, ?> response) {
        if (response == null || !response.containsKey("data")) {
            throw new IllegalStateException("Google Translator returned empty response");
        }

        Object dataObject = response.get("data");

        if (!(dataObject instanceof Map<?, ?> data)) {
            throw new IllegalStateException("Google Translator returned invalid data format");
        }

        Object translationsObject = data.get("translations");

        if (!(translationsObject instanceof List<?> translations) || translations.isEmpty()) {
            throw new IllegalStateException("Google Translator returned no translations");
        }

        Object firstTranslationObject = translations.get(0);

        if (!(firstTranslationObject instanceof Map<?, ?> firstTranslation)) {
            throw new IllegalStateException("Google Translator returned invalid translation format");
        }

        Object translatedText = firstTranslation.get("translatedText");

        if (translatedText == null) {
            throw new IllegalStateException("Google Translator returned translation without text");
        }

        return translatedText.toString();
    }
}