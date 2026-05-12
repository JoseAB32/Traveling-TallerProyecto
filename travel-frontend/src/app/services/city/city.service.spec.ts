/// <reference types="jest" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController
} from '@angular/common/http/testing';

import { CityService } from './city.service';
import { City } from '../../models/city/city';
import { CONSTANTS } from '../../utils/constants';

describe('CityService', () => {
  let service: CityService;
  let httpMock: HttpTestingController;

  const baseURL = CONSTANTS.API.BASE_URL + CONSTANTS.API.CITIES;

  const mockCities = [
    {
      id: 1,
      name: 'Cochabamba'
    },
    {
      id: 2,
      name: 'La Paz'
    }
  ] as City[];

  const mockCity = {
    id: 1,
    name: 'Cochabamba'
  } as City;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CityService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(CityService);
    httpMock = TestBed.inject(HttpTestingController);

    jest.spyOn(console, 'log').mockImplementation(() => {});
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    httpMock.verify();
    jest.restoreAllMocks();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get cities and log the amount of cities', () => {
    service.getCities().subscribe((cities) => {
      expect(cities).toEqual(mockCities);
      expect(cities.length).toBe(2);
    });

    const req = httpMock.expectOne(baseURL);

    expect(req.request.method).toBe('GET');

    req.flush(mockCities);

    expect(console.log).toHaveBeenCalledWith(
      'Cantidad de ciudades:',
      mockCities.length
    );
  });

  it('should handle error when getting cities', () => {
    const mockError = {
      status: 500,
      statusText: 'Internal Server Error'
    };

    service.getCities().subscribe({
      next: () => {
        fail('Expected an error, but got cities');
      },
      error: (error) => {
        expect(error.status).toBe(500);
        expect(error.statusText).toBe('Internal Server Error');
      }
    });

    const req = httpMock.expectOne(baseURL);

    expect(req.request.method).toBe('GET');

    req.flush('Error loading cities', mockError);

    expect(console.error).toHaveBeenCalledWith(
      CONSTANTS.MESSAGES.ERROR.LOAD_CITIES,
      expect.any(Object)
    );
  });

  it('should get city by id', () => {
    const cityId = 1;

    service.getCityById(cityId).subscribe((city) => {
      expect(city).toEqual(mockCity);
      expect(city.id).toBe(cityId);
    });

    const req = httpMock.expectOne(`${baseURL}/${cityId}`);

    expect(req.request.method).toBe('GET');

    req.flush(mockCity);

    expect(console.log).toHaveBeenCalledWith(
      CONSTANTS.LOGS.API_CALL,
      baseURL
    );
  });
});