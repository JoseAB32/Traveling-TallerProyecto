/// <reference types="jest" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController
} from '@angular/common/http/testing';

import { UserService } from './user.service';
import { User } from '../../models/user/user';
import { CONSTANTS } from '../../utils/constants';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  const baseURL = CONSTANTS.API.BASE_URL + CONSTANTS.API.USERS;
  const profileURL = CONSTANTS.API.BASE_URL + CONSTANTS.API.USERS + '/profile';

  const mockUsers = [
    { id: 1, userName: 'Ana Rojas',     correo: 'ana@example.com'    },
    { id: 2, userName: 'Carlos Vargas', correo: 'carlos@example.com' }
  ] as User[];

  const mockUser = { id: 1, userName: 'Ana Rojas', correo: 'ana@example.com' } as User;

  const mockProfile = {
    id: 1,
    userName: 'Ana Rojas',
    correo: 'ana@example.com',
    birthday: '1995-04-12',
    city: { id: 2, name: 'Cochabamba', state: true },
    state: true
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UserService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service  = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getUsersList', () => {
    it('should GET the users list', () => {
      service.getUsersList().subscribe(users => {
        expect(users).toEqual(mockUsers);
        expect(users.length).toBe(2);
        expect(users[0].correo).toBe('ana@example.com');
      });

      const req = httpMock.expectOne(baseURL);
      expect(req.request.method).toBe('GET');
      req.flush(mockUsers);
    });
  });

  describe('createUser', () => {
    it('should POST a new user', () => {
      const mockResponse = { message: 'User created successfully' };

      service.createUser(mockUser).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(baseURL);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockUser);
      req.flush(mockResponse);
    });
  });

  describe('getProfile', () => {
    it('should GET /api/profile and return the user profile', () => {
      service.getProfile().subscribe(profile => {
        expect(profile).toEqual(mockProfile);
        expect(profile.userName).toBe('Ana Rojas');
        expect(profile.correo).toBe('ana@example.com');
      });

      const req = httpMock.expectOne(profileURL);
      expect(req.request.method).toBe('GET');
      req.flush(mockProfile);
    });

    it('should include city in the profile when present', () => {
      service.getProfile().subscribe(profile => {
        expect(profile.city).toBeDefined();
        expect(profile.city).toBe('Cochabamba');
      });

      const req = httpMock.expectOne(profileURL);
      req.flush(mockProfile);
    });

    it('should handle a profile without city', () => {
      const profileNoCity = { ...mockProfile, city: null };

      service.getProfile().subscribe(profile => {
        expect(profile.city).toBeNull();
      });

      const req = httpMock.expectOne(profileURL);
      req.flush(profileNoCity);
    });

    it('should call GET exactly once per invocation', () => {
      service.getProfile().subscribe();

      const req = httpMock.expectOne(profileURL);
      expect(req.request.method).toBe('GET');
      req.flush(mockProfile);
    });
  });
});