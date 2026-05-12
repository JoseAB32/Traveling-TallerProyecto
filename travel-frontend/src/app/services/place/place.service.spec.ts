/// <reference types="jest" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController
} from '@angular/common/http/testing';

import { PlaceService } from './place.service';
import { Place } from '../../models/place/place';
import { CONSTANTS } from '../../utils/constants';

describe('PlaceService', () => {
  let service: PlaceService;
  let httpMock: HttpTestingController;

  const baseUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.PLACES;

  const mockPlaces = [
    {
      id: 1,
      name: 'Cristo de la Concordia'
    },
    {
      id: 2,
      name: 'Laguna Alalay'
    }
  ] as Place[];

  const mockPlace = {
    id: 1,
    name: 'Cristo de la Concordia'
  } as Place;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PlaceService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(PlaceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get ordered places from top-rated endpoint', () => {
    service.getPlacesOrdenado().subscribe((places) => {
      expect(places).toEqual(mockPlaces);
      expect(places.length).toBe(2);
    });

    const req = httpMock.expectOne(`${baseUrl}/top-rated`);

    expect(req.request.method).toBe('GET');

    req.flush(mockPlaces);
  });

  it('should get all places', () => {
    service.getPlaces().subscribe((places) => {
      expect(places).toEqual(mockPlaces);
      expect(places[0].name).toBe('Cristo de la Concordia');
    });

    const req = httpMock.expectOne(baseUrl);

    expect(req.request.method).toBe('GET');

    req.flush(mockPlaces);
  });

  it('should search places by term', () => {
    const term = 'cristo';

    service.searchPlaces(term).subscribe((places) => {
      expect(places).toEqual(mockPlaces);
    });

    const req = httpMock.expectOne(`${baseUrl}/search?q=${term}`);

    expect(req.request.method).toBe('GET');

    req.flush(mockPlaces);
  });

  it('should get a place by id', () => {
    const placeId = 1;

    service.getPlaceById(placeId).subscribe((place) => {
      expect(place).toEqual(mockPlace);
      expect(place.id).toBe(placeId);
    });

    const req = httpMock.expectOne(`${baseUrl}/${placeId}`);

    expect(req.request.method).toBe('GET');

    req.flush(mockPlace);
  });

  it('should get places by department', () => {
    const cityId = 3;

    service.getPlacesByDepartment(cityId).subscribe((places) => {
      expect(places).toEqual(mockPlaces);
      expect(places.length).toBe(2);
    });

    const req = httpMock.expectOne(`${baseUrl}/department/${cityId}`);

    expect(req.request.method).toBe('GET');

    req.flush(mockPlaces);
  });

  it('should get top places by department', () => {
    const cityId = 3;

    service.getTopPlacesByDepartment(cityId).subscribe((places) => {
      expect(places).toEqual(mockPlaces);
      expect(places.length).toBe(2);
    });

    const req = httpMock.expectOne(`${baseUrl}/departmentTop/${cityId}`);

    expect(req.request.method).toBe('GET');

    req.flush(mockPlaces);
  });
});