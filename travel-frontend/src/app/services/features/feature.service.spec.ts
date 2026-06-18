/// <reference types="jest" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController
} from '@angular/common/http/testing';

import { FeatureService, Features } from './feature.service';
import { CONSTANTS } from '../../utils/constants';

describe('FeatureService', () => {
  let service: FeatureService;
  let httpMock: HttpTestingController;

  const baseUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.FEATURES;

  const mockFeatures: Features = {
  };

  const updatedFeatures: Features = {
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        FeatureService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(FeatureService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have default cached features', () => {
    expect(service.features()).toEqual({
    });
  });

  it('should load features and update the signal cache', () => {
    service.loadFeatures().subscribe((features) => {
      expect(features).toEqual(mockFeatures);
    });

    const req = httpMock.expectOne(baseUrl);

    expect(req.request.method).toBe('GET');

    req.flush(mockFeatures);

    expect(service.features()).toEqual(mockFeatures);
  });

  it('should return true when a feature is enabled', () => {
    service.loadFeatures().subscribe();

    const req = httpMock.expectOne(baseUrl);
    req.flush(mockFeatures);
  });

  it('should return false when a feature is disabled', () => {
    service.loadFeatures().subscribe();

    const req = httpMock.expectOne(baseUrl);
    req.flush(mockFeatures);
  });

  it('should return false when feature key does not exist', () => {
    service.loadFeatures().subscribe();

    const req = httpMock.expectOne(baseUrl);
    req.flush(mockFeatures);

    expect(service.isEnabled('unknownFeature')).toBe(false);
  });

  it('should update features and update the signal cache', () => {
    service.updateFeatures(updatedFeatures).subscribe((features) => {
      expect(features).toEqual(updatedFeatures);
    });

    const req = httpMock.expectOne(baseUrl);

    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updatedFeatures);

    req.flush(updatedFeatures);

    expect(service.features()).toEqual(updatedFeatures);
  });
});