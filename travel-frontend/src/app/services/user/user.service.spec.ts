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

  const mockUsers = [
    {
      id: 1,
      userName: 'Ana Rojas',
      correo: 'ana@example.com'
    },
    {
      id: 2,
      userName: 'Carlos Vargas',
      correo: 'carlos@example.com'
    }
  ] as User[];

  const mockUser = {
    id: 1,
    userName: 'Ana Rojas',
    correo: 'ana@example.com'
  } as User;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UserService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get users list', () => {
    service.getUsersList().subscribe((users) => {
      expect(users).toEqual(mockUsers);
      expect(users.length).toBe(2);
      expect(users[0].correo).toBe('ana@example.com');
    });

    const req = httpMock.expectOne(baseURL);

    expect(req.request.method).toBe('GET');

    req.flush(mockUsers);
  });

  it('should create a user', () => {
    const mockResponse = {
      message: 'User created successfully'
    };

    service.createUser(mockUser).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(baseURL);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockUser);

    req.flush(mockResponse);
  });
});