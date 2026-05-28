package com.traveling.travel_backend.dto;

import com.traveling.travel_backend.model.Translations;

public class TranslationResponseDTO {

    private Long id;
    private String entityType;
    private Long entityId;
    private String fieldName;
    private String language;
    private String translatedText;

    public TranslationResponseDTO() {
    }

    public static TranslationResponseDTO fromEntity(Translations translation) {
        TranslationResponseDTO dto = new TranslationResponseDTO();
        dto.setId(translation.getId());
        dto.setEntityType(translation.getEntityType());
        dto.setEntityId(translation.getEntityId());
        dto.setFieldName(translation.getFieldName());
        dto.setLanguage(translation.getLanguage());
        dto.setTranslatedText(translation.getTranslatedText());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}