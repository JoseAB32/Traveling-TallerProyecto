package com.traveling.travel_backend.dto;

public class UpdateTranslationRequestDTO {

    private String translatedText;

    public UpdateTranslationRequestDTO() {
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}