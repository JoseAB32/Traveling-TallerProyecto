/// <reference types="jest" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController
} from '@angular/common/http/testing';

import { RoutingService, RouteResult } from './routing.service';
import { environment } from '../../../environments/environment';

describe('RoutingService', () => {
  let service: RoutingService;
  let httpMock: HttpTestingController;

  const mockOrigin = {
    latitude: -17.3895,
    longitude: -66.1568
  };

  const mockDestination = {
    latitude: -17.3935,
    longitude: -66.1570
  };

  const mockRouteResult: RouteResult = {
    distanceKm: 2.5,
    durationMinutes: 12,
    coordinates: [
      [-17.3895, -66.1568],
      [-17.3910, -66.1572],
      [-17.3935, -66.1570]
    ]
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        RoutingService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(RoutingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get route between origin and destination', () => {
    service.getRoute(mockOrigin, mockDestination).subscribe((route) => {
      expect(route).toEqual(mockRouteResult);
      expect(route.distanceKm).toBe(2.5);
      expect(route.durationMinutes).toBe(12);
      expect(route.coordinates.length).toBe(3);
    });

    const req = httpMock.expectOne((request) => {
      return request.url === `${environment.apiUrl}/api/routes`;
    });

    expect(req.request.method).toBe('GET');

    expect(req.request.params.get('originLat')).toBe(String(mockOrigin.latitude));
    expect(req.request.params.get('originLng')).toBe(String(mockOrigin.longitude));
    expect(req.request.params.get('destinationLat')).toBe(String(mockDestination.latitude));
    expect(req.request.params.get('destinationLng')).toBe(String(mockDestination.longitude));

    req.flush(mockRouteResult);
  });
});