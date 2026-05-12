/// <reference types="jest" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController
} from '@angular/common/http/testing';

import { FavoriteService } from './favorite.service';
import { CONSTANTS } from '../../utils/constants';

describe('FavoriteService', () => {
  let service: FavoriteService;
  let httpMock: HttpTestingController;

  const apiUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.FAVORITES;

  const mockFavorites = [
    {
      id: 1,
      name: 'Cristo de la Concordia',
      placeId: 10
    },
    {
      id: 2,
      name: 'Laguna Alalay',
      placeId: 20
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        FavoriteService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(FavoriteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get user favorites', () => {
    service.getUserFavorites().subscribe((favorites) => {
      expect(favorites).toEqual(mockFavorites);
      expect(favorites.length).toBe(2);
    });

    const req = httpMock.expectOne(`${apiUrl}/user`);

    expect(req.request.method).toBe('GET');

    req.flush(mockFavorites);
  });

  it('should add a favorite place', () => {
    const placeId = 10;
    const mockResponse = {
      message: 'Favorite added successfully'
    };

    service.addFavorite(placeId).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/user/place/${placeId}`);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});

    req.flush(mockResponse);
  });

  it('should remove a favorite place', () => {
    const placeId = 10;
    const mockResponse = {
      message: 'Favorite removed successfully'
    };

    service.removeFavorite(placeId).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/user/place/${placeId}`);

    expect(req.request.method).toBe('DELETE');

    req.flush(mockResponse);
  });
});