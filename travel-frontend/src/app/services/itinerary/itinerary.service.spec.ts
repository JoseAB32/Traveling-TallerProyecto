/// <reference types="jest" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController
} from '@angular/common/http/testing';

import {
  ItineraryDraftRequest,
  ItineraryDraftResponse,
  ItineraryService
} from './itinerary.service';

import { CONSTANTS } from '../../utils/constants';

describe('ItineraryService', () => {
  let service: ItineraryService;
  let httpMock: HttpTestingController;

  const apiUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.TRIPS;

  const mockPayload: ItineraryDraftRequest = {
    name: 'Itinerario Cochabamba',
    startDate: '2026-05-12',
    endDate: '2026-05-13',
    placeIds: [1, 2, 3]
  };

  const mockDraftResponse = {
    tripId: 1,
    userId: 10,
    name: 'Itinerario Cochabamba',
    startDate: '2026-05-12',
    endDate: '2026-05-13',
    places: [
      {
        id: 1,
        name: 'Cristo de la Concordia'
      },
      {
        id: 2,
        name: 'Laguna Alalay'
      }
    ],
    routeCoordinates: [
      [-17.384, -66.156],
      [-17.402, -66.145]
    ]
  } as unknown as ItineraryDraftResponse;

  const mockItineraries = [
    mockDraftResponse,
    {
      tripId: 2,
      userId: 10,
      name: 'Itinerario La Paz',
      startDate: null,
      endDate: null,
      places: [
        {
          id: 3,
          name: 'Valle de la Luna'
        }
      ]
    } as unknown as ItineraryDraftResponse
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ItineraryService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ItineraryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get my draft', () => {
    service.getMyDraft().subscribe((response) => {
      expect(response).toEqual(mockDraftResponse);
      expect(response.tripId).toBe(1);
      expect(response.userId).toBe(10);
    });

    const req = httpMock.expectOne(`${apiUrl}/draft/me`);

    expect(req.request.method).toBe('GET');

    req.flush(mockDraftResponse);
  });

  it('should get draft by user id', () => {
    const userId = 10;

    service.getDraftByUser(userId).subscribe((response) => {
      expect(response).toEqual(mockDraftResponse);
      expect(response.userId).toBe(userId);
    });

    const req = httpMock.expectOne(`${apiUrl}/draft/user/${userId}`);

    expect(req.request.method).toBe('GET');

    req.flush(mockDraftResponse);
  });

  it('should save draft', () => {
    service.saveDraft(mockPayload).subscribe((response) => {
      expect(response).toEqual(mockDraftResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/draft`);

    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(mockPayload);

    req.flush(mockDraftResponse);
  });

  it('should create itinerary', () => {
    service.createItinerary(mockPayload).subscribe((response) => {
      expect(response).toEqual(mockDraftResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/trip`);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockPayload);

    req.flush(mockDraftResponse);
  });

  it('should get my itineraries', () => {
    service.getMyItineraries().subscribe((response) => {
      expect(response).toEqual(mockItineraries);
      expect(response.length).toBe(2);
      expect(response[0].tripId).toBe(1);
      expect(response[1].tripId).toBe(2);
    });

    const req = httpMock.expectOne(`${apiUrl}/me`);

    expect(req.request.method).toBe('GET');

    req.flush(mockItineraries);
  });

  it('should get itinerary by id', () => {
    const tripId = 1;

    service.getItineraryById(tripId).subscribe((response) => {
      expect(response).toEqual(mockDraftResponse);
      expect(response.tripId).toBe(tripId);
    });

    const req = httpMock.expectOne(`${apiUrl}/trip/${tripId}`);

    expect(req.request.method).toBe('GET');

    req.flush(mockDraftResponse);
  });

  it('should update itinerary', () => {
    const tripId = 1;

    const updatedPayload: ItineraryDraftRequest = {
      name: 'Itinerario actualizado',
      startDate: '2026-05-15',
      endDate: '2026-05-16',
      placeIds: [2, 3]
    };

    const updatedResponse = {
      ...mockDraftResponse,
      name: 'Itinerario actualizado',
      startDate: '2026-05-15',
      endDate: '2026-05-16'
    } as ItineraryDraftResponse;

    service.updateItinerary(tripId, updatedPayload).subscribe((response) => {
      expect(response).toEqual(updatedResponse);
      expect(response.name).toBe('Itinerario actualizado');
    });

    const req = httpMock.expectOne(`${apiUrl}/trip/${tripId}`);

    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updatedPayload);

    req.flush(updatedResponse);
  });
});