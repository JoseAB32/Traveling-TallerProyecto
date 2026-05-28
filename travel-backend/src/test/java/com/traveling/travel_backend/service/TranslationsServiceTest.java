package com.traveling.travel_backend.service;

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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TranslationsServiceTest {

    @Mock
    private TranslationRepository translationRepository;

    @Mock
    private LogRepository logRepository;

    @Mock
    private TranslationProviderService translationProviderService;

    @InjectMocks
    private TranslationsService translationsService;

    @Nested
    @DisplayName("Tests para getTranslation")
    class GetTranslationTests {

        @Test
        @DisplayName("Debe devolver traducción existente sin llamar al proveedor")
        void shouldReturnExistingTranslationWithoutCallingProvider() {
            Translations existingTranslation = buildTranslation(
                    1L,
                    AppConstants.ENTITY_TYPE_REVIEW,
                    10L,
                    AppConstants.FIELD_COMMENT,
                    "en",
                    "Excellent place"
            );

            when(translationRepository.findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                    AppConstants.ENTITY_TYPE_REVIEW, 10L, AppConstants.FIELD_COMMENT, "en"))
                    .thenReturn(Optional.of(existingTranslation));

            String result = translationsService.getTranslation(
                    AppConstants.ENTITY_TYPE_REVIEW,
                    10L,
                    AppConstants.FIELD_COMMENT,
                    "en",
                    "Excelente lugar"
            );

            assertEquals("Excellent place", result);

            verify(translationRepository).findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                    AppConstants.ENTITY_TYPE_REVIEW, 10L, AppConstants.FIELD_COMMENT, "en");
            verifyNoInteractions(translationProviderService);
            verify(translationRepository, never()).save(any(Translations.class));
            verify(logRepository, times(1)).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe crear, guardar y devolver traducción cuando no existe")
        void shouldCreateSaveAndReturnTranslationWhenItDoesNotExist() {
            when(translationRepository.findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                    AppConstants.ENTITY_TYPE_REVIEW, 10L, AppConstants.FIELD_COMMENT, "en"))
                    .thenReturn(Optional.empty());

            when(translationProviderService.translate("Excelente lugar", AppConstants.DEFAULT_LANGUAGE, "en"))
                    .thenReturn(new TranslationResultDTO("Excellent place", AppConstants.PROVIDER_AZURE));

            String result = translationsService.getTranslation(
                    AppConstants.ENTITY_TYPE_REVIEW,
                    10L,
                    AppConstants.FIELD_COMMENT,
                    "en",
                    "Excelente lugar"
            );

            assertEquals("Excellent place", result);

            ArgumentCaptor<Translations> captor = ArgumentCaptor.forClass(Translations.class);
            verify(translationRepository).saveAndFlush(captor.capture());

            Translations savedTranslation = captor.getValue();
            assertEquals(AppConstants.ENTITY_TYPE_REVIEW, savedTranslation.getEntityType());
            assertEquals(10L, savedTranslation.getEntityId());
            assertEquals(AppConstants.FIELD_COMMENT, savedTranslation.getFieldName());
            assertEquals("en", savedTranslation.getLanguage());
            assertEquals("Excellent place", savedTranslation.getTranslatedText());

            verify(translationProviderService).translate("Excelente lugar", AppConstants.DEFAULT_LANGUAGE, "en");
            verify(logRepository, times(2)).save(any(LogEntity.class));
        }
    }

    @Nested
    @DisplayName("Tests para getTranslations")
    class GetTranslationsTests {

        @Test
        @DisplayName("Debe devolver traducciones paginadas con filtros")
        void shouldReturnPagedTranslationsWithFilters() {
            Translations translation = buildTranslation(
                    1L,
                    AppConstants.ENTITY_TYPE_REVIEW,
                    10L,
                    AppConstants.FIELD_COMMENT,
                    "en",
                    "Excellent place"
            );

            when(translationRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 20))))
                    .thenReturn(new PageImpl<>(List.of(translation), PageRequest.of(0, 20), 1));

            TranslationPageResponseDTO result = translationsService.getTranslations(
                    AppConstants.ENTITY_TYPE_REVIEW,
                    "en",
                    AppConstants.FIELD_COMMENT,
                    10L,
                    -1,
                    0
            );

            assertEquals(1, result.getContent().size());
            assertEquals(0, result.getPage());
            assertEquals(20, result.getSize());
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getTotalPages());
            assertEquals("Excellent place", result.getContent().get(0).getTranslatedText());

            verify(translationRepository).findAll(any(Specification.class), eq(PageRequest.of(0, 20)));
            verify(logRepository).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe limitar el tamaño máximo de página a 100")
        void shouldLimitPageSizeToOneHundred() {
            when(translationRepository.findAll(any(Specification.class), eq(PageRequest.of(2, 100))))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 100), 0));

            TranslationPageResponseDTO result = translationsService.getTranslations(
                    null,
                    null,
                    null,
                    null,
                    2,
                    500
            );

            assertEquals(2, result.getPage());
            assertEquals(100, result.getSize());
            assertEquals(0, result.getTotalElements());

            verify(translationRepository).findAll(any(Specification.class), eq(PageRequest.of(2, 100)));
        }

        @Test
        @DisplayName("Debe mantener tamaño normal cuando size es válido")
        void shouldKeepValidPageSize() {
            when(translationRepository.findAll(any(Specification.class), eq(PageRequest.of(1, 50))))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 50), 0));

            TranslationPageResponseDTO result = translationsService.getTranslations(
                    null,
                    null,
                    null,
                    null,
                    1,
                    50
            );

            assertEquals(1, result.getPage());
            assertEquals(50, result.getSize());

            verify(translationRepository).findAll(any(Specification.class), eq(PageRequest.of(1, 50)));
        }

        @Test
        @DisplayName("Debe construir specification con todos los filtros cuando llegan valores válidos")
        void shouldBuildSpecificationWithAllFilters() {
            when(translationRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 20))))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            translationsService.getTranslations(" REVIEW ", " en ", " comment ", 10L, 0, 20);

            Specification<Translations> specification = captureSpecification();

            Root<Translations> root = mock(Root.class);
            CriteriaQuery<?> query = mock(CriteriaQuery.class);
            CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

            Predicate conjunctionPredicate = mock(Predicate.class);
            Predicate entityTypePredicate = mock(Predicate.class);
            Predicate afterEntityTypePredicate = mock(Predicate.class);
            Predicate languagePredicate = mock(Predicate.class);
            Predicate afterLanguagePredicate = mock(Predicate.class);
            Predicate fieldNamePredicate = mock(Predicate.class);
            Predicate afterFieldNamePredicate = mock(Predicate.class);
            Predicate entityIdPredicate = mock(Predicate.class);
            Predicate finalPredicate = mock(Predicate.class);

            Path<Object> entityTypePath = mockPath(root, "entityType");
            Path<Object> languagePath = mockPath(root, "language");
            Path<Object> fieldNamePath = mockPath(root, "fieldName");
            Path<Object> entityIdPath = mockPath(root, "entityId");

            when(criteriaBuilder.conjunction()).thenReturn(conjunctionPredicate);
            when(criteriaBuilder.equal(entityTypePath, "REVIEW")).thenReturn(entityTypePredicate);
            when(criteriaBuilder.and(conjunctionPredicate, entityTypePredicate)).thenReturn(afterEntityTypePredicate);
            when(criteriaBuilder.equal(languagePath, "en")).thenReturn(languagePredicate);
            when(criteriaBuilder.and(afterEntityTypePredicate, languagePredicate)).thenReturn(afterLanguagePredicate);
            when(criteriaBuilder.equal(fieldNamePath, "comment")).thenReturn(fieldNamePredicate);
            when(criteriaBuilder.and(afterLanguagePredicate, fieldNamePredicate)).thenReturn(afterFieldNamePredicate);
            when(criteriaBuilder.equal(entityIdPath, 10L)).thenReturn(entityIdPredicate);
            when(criteriaBuilder.and(afterFieldNamePredicate, entityIdPredicate)).thenReturn(finalPredicate);

            Predicate result = specification.toPredicate(root, query, criteriaBuilder);

            assertSame(finalPredicate, result);
        }

        @Test
        @DisplayName("Debe construir specification sin filtros cuando llegan valores nulos o vacíos")
        void shouldBuildSpecificationWithoutFiltersWhenValuesAreBlankOrNull() {
            when(translationRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 20))))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            translationsService.getTranslations("   ", "   ", "   ", null, 0, 20);

            Specification<Translations> specification = captureSpecification();

            Root<Translations> root = mock(Root.class);
            CriteriaQuery<?> query = mock(CriteriaQuery.class);
            CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
            Predicate conjunctionPredicate = mock(Predicate.class);

            when(criteriaBuilder.conjunction()).thenReturn(conjunctionPredicate);

            Predicate result = specification.toPredicate(root, query, criteriaBuilder);

            assertSame(conjunctionPredicate, result);
        }

        @Test
        @DisplayName("No debe guardar traducción cuando el proveedor devuelve ORIGINAL")
        void shouldNotSaveTranslationWhenProviderReturnsOriginal() {
            when(translationRepository.findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                    AppConstants.ENTITY_TYPE_REVIEW, 10L, AppConstants.FIELD_COMMENT, "en"))
                    .thenReturn(Optional.empty());

            when(translationProviderService.translate("Excelente lugar", AppConstants.DEFAULT_LANGUAGE, "en"))
                    .thenReturn(new TranslationResultDTO("Excelente lugar", AppConstants.PROVIDER_ORIGINAL));

            String result = translationsService.getTranslation(
                    AppConstants.ENTITY_TYPE_REVIEW,
                    10L,
                    AppConstants.FIELD_COMMENT,
                    "en",
                    "Excelente lugar"
            );

            assertEquals("Excelente lugar", result);

            verify(translationProviderService).translate("Excelente lugar", AppConstants.DEFAULT_LANGUAGE, "en");
            verify(translationRepository, never()).save(any(Translations.class));
            verify(translationRepository, never()).saveAndFlush(any(Translations.class));
            verify(logRepository, times(2)).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe recuperar traducción existente si ocurre conflicto concurrente al guardar")
        void shouldReturnExistingTranslationWhenConcurrentSaveConflictHappens() {
            Translations existingTranslation = buildTranslation(
                    1L,
                    AppConstants.ENTITY_TYPE_REVIEW,
                    10L,
                    AppConstants.FIELD_COMMENT,
                    "en",
                    "Excellent place"
            );

            when(translationRepository.findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                    AppConstants.ENTITY_TYPE_REVIEW, 10L, AppConstants.FIELD_COMMENT, "en"))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(existingTranslation));

            when(translationProviderService.translate("Excelente lugar", AppConstants.DEFAULT_LANGUAGE, "en"))
                    .thenReturn(new TranslationResultDTO("Excellent place", AppConstants.PROVIDER_AZURE));

            when(translationRepository.saveAndFlush(any(Translations.class)))
                    .thenThrow(new DataIntegrityViolationException("Duplicate translation"));

            String result = translationsService.getTranslation(
                    AppConstants.ENTITY_TYPE_REVIEW,
                    10L,
                    AppConstants.FIELD_COMMENT,
                    "en",
                    "Excelente lugar"
            );

            assertEquals("Excellent place", result);

            verify(translationProviderService).translate("Excelente lugar", AppConstants.DEFAULT_LANGUAGE, "en");
            verify(translationRepository).saveAndFlush(any(Translations.class));
            verify(translationRepository, times(2)).findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                    AppConstants.ENTITY_TYPE_REVIEW, 10L, AppConstants.FIELD_COMMENT, "en");
        }

        @Test
        @DisplayName("Debe normalizar el idioma antes de buscar y guardar traducción")
        void shouldNormalizeLanguageBeforeSearchingAndSavingTranslation() {
            when(translationRepository.findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                    AppConstants.ENTITY_TYPE_REVIEW, 10L, AppConstants.FIELD_COMMENT, "en"))
                    .thenReturn(Optional.empty());

            when(translationProviderService.translate("Excelente lugar", AppConstants.DEFAULT_LANGUAGE, "en"))
                    .thenReturn(new TranslationResultDTO("Excellent place", AppConstants.PROVIDER_AZURE));

            String result = translationsService.getTranslation(
                    AppConstants.ENTITY_TYPE_REVIEW,
                    10L,
                    AppConstants.FIELD_COMMENT,
                    " EN ",
                    "Excelente lugar"
            );

            assertEquals("Excellent place", result);

            ArgumentCaptor<Translations> captor = ArgumentCaptor.forClass(Translations.class);
            verify(translationRepository).saveAndFlush(captor.capture());

            Translations savedTranslation = captor.getValue();
            assertEquals("en", savedTranslation.getLanguage());

            verify(translationRepository).findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                    AppConstants.ENTITY_TYPE_REVIEW, 10L, AppConstants.FIELD_COMMENT, "en");
            verify(translationProviderService).translate("Excelente lugar", AppConstants.DEFAULT_LANGUAGE, "en");
        }
    }

    @Nested
    @DisplayName("Tests para updateTranslation")
    class UpdateTranslationTests {

        @Test
        @DisplayName("Debe actualizar una traducción existente")
        void shouldUpdateExistingTranslation() {
            UpdateTranslationRequestDTO request = new UpdateTranslationRequestDTO();
            request.setTranslatedText("Corrected translation");

            Translations existingTranslation = buildTranslation(
                    1L,
                    AppConstants.ENTITY_TYPE_REVIEW,
                    10L,
                    AppConstants.FIELD_COMMENT,
                    "en",
                    "Old translation"
            );

            when(translationRepository.findById(1L)).thenReturn(Optional.of(existingTranslation));
            when(translationRepository.save(existingTranslation)).thenReturn(existingTranslation);

            TranslationResponseDTO result = translationsService.updateTranslation(1L, request);

            assertEquals(1L, result.getId());
            assertEquals(AppConstants.ENTITY_TYPE_REVIEW, result.getEntityType());
            assertEquals(10L, result.getEntityId());
            assertEquals(AppConstants.FIELD_COMMENT, result.getFieldName());
            assertEquals("en", result.getLanguage());
            assertEquals("Corrected translation", result.getTranslatedText());

            verify(translationRepository).findById(1L);
            verify(translationRepository).save(existingTranslation);
            verify(logRepository, times(2)).save(any(LogEntity.class));
        }

        @Test
        @DisplayName("Debe limpiar espacios al actualizar una traducción")
        void shouldTrimTranslatedTextWhenUpdatingTranslation() {
            UpdateTranslationRequestDTO request = new UpdateTranslationRequestDTO();
            request.setTranslatedText("   Corrected translation   ");

            Translations existingTranslation = buildTranslation(
                    1L,
                    AppConstants.ENTITY_TYPE_REVIEW,
                    10L,
                    AppConstants.FIELD_COMMENT,
                    "en",
                    "Old translation"
            );

            when(translationRepository.findById(1L)).thenReturn(Optional.of(existingTranslation));
            when(translationRepository.save(existingTranslation)).thenReturn(existingTranslation);

            TranslationResponseDTO result = translationsService.updateTranslation(1L, request);

            assertEquals("Corrected translation", result.getTranslatedText());
            verify(translationRepository).save(existingTranslation);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException cuando request es null")
        void shouldThrowBadRequestWhenUpdateRequestIsNull() {
            assertThrows(BadRequestException.class, () -> translationsService.updateTranslation(1L, null));

            verify(translationRepository, never()).findById(any());
            verify(translationRepository, never()).save(any());
            verifyNoInteractions(logRepository);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException cuando translatedText es null")
        void shouldThrowBadRequestWhenTranslatedTextIsNull() {
            UpdateTranslationRequestDTO request = new UpdateTranslationRequestDTO();
            request.setTranslatedText(null);

            assertThrows(BadRequestException.class, () -> translationsService.updateTranslation(1L, request));

            verify(translationRepository, never()).findById(any());
            verify(translationRepository, never()).save(any());
            verifyNoInteractions(logRepository);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException cuando translatedText está vacío")
        void shouldThrowBadRequestWhenTranslatedTextIsBlank() {
            UpdateTranslationRequestDTO request = new UpdateTranslationRequestDTO();
            request.setTranslatedText("   ");

            assertThrows(BadRequestException.class, () -> translationsService.updateTranslation(1L, request));

            verify(translationRepository, never()).findById(any());
            verify(translationRepository, never()).save(any());
            verifyNoInteractions(logRepository);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando la traducción no existe")
        void shouldThrowResourceNotFoundWhenTranslationDoesNotExist() {
            UpdateTranslationRequestDTO request = new UpdateTranslationRequestDTO();
            request.setTranslatedText("Corrected translation");

            when(translationRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> translationsService.updateTranslation(99L, request));

            assertTrue(exception.getMessage().contains("Traducción no encontrada con ID: 99"));

            verify(translationRepository).findById(99L);
            verify(translationRepository, never()).save(any());
            verify(logRepository, times(2)).save(any(LogEntity.class));
        }
    }

    private Translations buildTranslation(
            Long id,
            String entityType,
            Long entityId,
            String fieldName,
            String language,
            String translatedText) {
        Translations translation = new Translations();
        translation.setId(id);
        translation.setEntityType(entityType);
        translation.setEntityId(entityId);
        translation.setFieldName(fieldName);
        translation.setLanguage(language);
        translation.setTranslatedText(translatedText);
        return translation;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Path<Object> mockPath(Root<Translations> root, String fieldName) {
        Path path = mock(Path.class);
        when(root.get(fieldName)).thenReturn(path);
        return path;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Specification<Translations> captureSpecification() {
        ArgumentCaptor<Specification> captor = ArgumentCaptor.forClass(Specification.class);
        verify(translationRepository).findAll(captor.capture(), any(PageRequest.class));
        return captor.getValue();
    }
}