package com.traveling.travel_backend.dto;

public class TranslationResultDTO {

    private String translatedText;
    private String provider;

    public TranslationResultDTO() {
    }

    public TranslationResultDTO(String translatedText, String provider) {
        this.translatedText = translatedText;
        this.provider = provider;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}