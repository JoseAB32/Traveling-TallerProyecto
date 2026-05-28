package com.traveling.travel_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveling.travel_backend.dto.TranslationPageResponseDTO;
import com.traveling.travel_backend.dto.TranslationResponseDTO;
import com.traveling.travel_backend.dto.UpdateTranslationRequestDTO;
import com.traveling.travel_backend.security.JwtService;
import com.traveling.travel_backend.service.TranslationsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TranslationController.class)
@AutoConfigureMockMvc(addFilters = false)
class TranslationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TranslationsService translationsService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/translations retorna traducciones paginadas")
    void getTranslationsReturnsPagedTranslations() throws Exception {
        TranslationResponseDTO translation = new TranslationResponseDTO();
        translation.setId(1L);
        translation.setEntityType("REVIEW");
        translation.setEntityId(10L);
        translation.setFieldName("comment");
        translation.setLanguage("en");
        translation.setTranslatedText("Excellent place");

        TranslationPageResponseDTO pageResponse = new TranslationPageResponseDTO();
        pageResponse.setContent(List.of(translation));
        pageResponse.setPage(0);
        pageResponse.setSize(20);
        pageResponse.setTotalElements(1);
        pageResponse.setTotalPages(1);
        pageResponse.setHasNext(false);

        when(translationsService.getTranslations("REVIEW", "en", "comment", 10L, 0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/translations")
                        .param("entityType", "REVIEW")
                        .param("language", "en")
                        .param("fieldName", "comment")
                        .param("entityId", "10")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].entityType").value("REVIEW"))
                .andExpect(jsonPath("$.content[0].entityId").value(10))
                .andExpect(jsonPath("$.content[0].fieldName").value("comment"))
                .andExpect(jsonPath("$.content[0].language").value("en"))
                .andExpect(jsonPath("$.content[0].translatedText").value("Excellent place"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("GET /api/translations usa valores por defecto cuando no llegan page y size")
    void getTranslationsUsesDefaultPagination() throws Exception {
        TranslationPageResponseDTO pageResponse = new TranslationPageResponseDTO();
        pageResponse.setContent(List.of());
        pageResponse.setPage(0);
        pageResponse.setSize(20);
        pageResponse.setTotalElements(0);
        pageResponse.setTotalPages(0);
        pageResponse.setHasNext(false);

        when(translationsService.getTranslations(null, null, null, null, 0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/translations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @DisplayName("PUT /api/translations/{id} actualiza traducción")
    void updateTranslationReturnsUpdatedTranslation() throws Exception {
        UpdateTranslationRequestDTO request = new UpdateTranslationRequestDTO();
        request.setTranslatedText("Corrected translation");

        TranslationResponseDTO response = new TranslationResponseDTO();
        response.setId(1L);
        response.setEntityType("REVIEW");
        response.setEntityId(10L);
        response.setFieldName("comment");
        response.setLanguage("en");
        response.setTranslatedText("Corrected translation");

        when(translationsService.updateTranslation(eq(1L), any(UpdateTranslationRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/translations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.translatedText").value("Corrected translation"));
    }
}