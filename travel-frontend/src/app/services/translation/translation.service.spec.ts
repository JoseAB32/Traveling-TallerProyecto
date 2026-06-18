/// <reference types="jest" />

import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { TranslationService } from './translation.service';
import { CONSTANTS } from '../../utils/constants';
import {
  TranslationFilters,
  TranslationPageResponse,
  Translation,
  UpdateTranslationRequest
} from '../../models/translation/translation';

describe('TranslationService', () => {
  let service: TranslationService;
  let httpMock: HttpTestingController;

  const baseUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.TRANSLATIONS;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TranslationService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(TranslationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getTranslations', () => {
    it('should send GET request with default page and size when filters are empty', () => {
      const filters: TranslationFilters = {};

      const mockResponse = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 20,
        number: 0
      } as unknown as TranslationPageResponse;

      service.getTranslations(filters).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(request =>
        request.method === 'GET' &&
        request.url === baseUrl
      );

      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('20');
      expect(req.request.params.has('entityType')).toBe(false);
      expect(req.request.params.has('language')).toBe(false);
      expect(req.request.params.has('fieldName')).toBe(false);
      expect(req.request.params.has('entityId')).toBe(false);

      req.flush(mockResponse);
    });

    it('should send GET request with provided pagination filters', () => {
      const filters: TranslationFilters = {
        page: 2,
        size: 10
      };

      const mockResponse = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 10,
        number: 2
      } as unknown as TranslationPageResponse;

      service.getTranslations(filters).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(request =>
        request.method === 'GET' &&
        request.url === baseUrl
      );

      expect(req.request.params.get('page')).toBe('2');
      expect(req.request.params.get('size')).toBe('10');

      req.flush(mockResponse);
    });

    it('should send GET request with all optional filters when they are provided', () => {
      const filters: TranslationFilters = {
        page: 1,
        size: 5,
        entityType: 'PLACE',
        language: 'en',
        fieldName: 'description',
        entityId: 15
      };

      const mockResponse = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 5,
        number: 1
      } as unknown as TranslationPageResponse;

      service.getTranslations(filters).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(request =>
        request.method === 'GET' &&
        request.url === baseUrl
      );

      expect(req.request.params.get('page')).toBe('1');
      expect(req.request.params.get('size')).toBe('5');
      expect(req.request.params.get('entityType')).toBe('PLACE');
      expect(req.request.params.get('language')).toBe('en');
      expect(req.request.params.get('fieldName')).toBe('description');
      expect(req.request.params.get('entityId')).toBe('15');

      req.flush(mockResponse);
    });

    it('should include entityId when entityId is 0', () => {
      const filters: TranslationFilters = {
        entityId: 0
      };

      const mockResponse = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 20,
        number: 0
      } as unknown as TranslationPageResponse;

      service.getTranslations(filters).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(request =>
        request.method === 'GET' &&
        request.url === baseUrl
      );

      expect(req.request.params.get('entityId')).toBe('0');

      req.flush(mockResponse);
    });

    it('should not include entityId when entityId is null', () => {
      const filters: TranslationFilters = {
        entityId: null
      };

      const mockResponse = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 20,
        number: 0
      } as unknown as TranslationPageResponse;

      service.getTranslations(filters).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(request =>
        request.method === 'GET' &&
        request.url === baseUrl
      );

      expect(req.request.params.has('entityId')).toBe(false);

      req.flush(mockResponse);
    });

    it('should not include entityId when entityId is undefined', () => {
      const filters: TranslationFilters = {
        entityId: undefined
      };

      const mockResponse = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 20,
        number: 0
      } as unknown as TranslationPageResponse;

      service.getTranslations(filters).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(request =>
        request.method === 'GET' &&
        request.url === baseUrl
      );

      expect(req.request.params.has('entityId')).toBe(false);

      req.flush(mockResponse);
    });

    it('should not include empty optional string filters', () => {
      const filters: TranslationFilters = {
        entityType: '',
        language: '',
        fieldName: ''
      };

      const mockResponse = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 20,
        number: 0
      } as unknown as TranslationPageResponse;

      service.getTranslations(filters).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(request =>
        request.method === 'GET' &&
        request.url === baseUrl
      );

      expect(req.request.params.has('entityType')).toBe(false);
      expect(req.request.params.has('language')).toBe(false);
      expect(req.request.params.has('fieldName')).toBe(false);

      req.flush(mockResponse);
    });
  });

  describe('updateTranslation', () => {
    it('should send PUT request with translation id and body', () => {
      const translationId = 7;

      const requestBody = {
        translatedText: 'Texto actualizado'
      } as UpdateTranslationRequest;

      const mockResponse = {
        id: translationId,
        translatedText: 'Texto actualizado'
      } as unknown as Translation;

      service.updateTranslation(translationId, requestBody).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/${translationId}`);

      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(requestBody);

      req.flush(mockResponse);
    });
  });
});