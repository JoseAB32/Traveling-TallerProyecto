/// <reference types="jest" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController
} from '@angular/common/http/testing';

import {
  AuthService,
  LoginResponse,
  User
} from './auth.service';

import { CONSTANTS } from '../../utils/constants';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const baseUrl = CONSTANTS.API.BASE_URL;
  const loginUrl = baseUrl + CONSTANTS.API.LOGIN;
  const passwordUrl = baseUrl + '/api/password';

  const mockLoginResponse: LoginResponse = {
    token: 'mock-token',
    type: 'Bearer',
    userName: 'vanessa',
    correo: 'vanessa@example.com',
    message: 'Login correcto',
    id: 1,
    role: 'USER'
  };

  const mockUser: User = {
    id: 1,
    correo: 'vanessa@example.com',
    userName: 'vanessa',
    role: 'USER'
  };

  function setupService(): void {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  }

  function createFakeJwt(payload: object): string {
    const encodedPayload = btoa(JSON.stringify(payload));

    return `header.${encodedPayload}.signature`;
  }

  beforeEach(() => {
    localStorage.clear();
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    if (httpMock) {
      httpMock.verify();
    }

    localStorage.clear();
    jest.restoreAllMocks();
    TestBed.resetTestingModule();
  });

  it('should be created', () => {
    setupService();

    expect(service).toBeTruthy();
  });

  it('should login, save token, save current user and update currentUser$', () => {
    setupService();

    service.login('vanessa', '123456').subscribe((response) => {
      expect(response).toEqual(mockLoginResponse);
    });

    const req = httpMock.expectOne(loginUrl);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      userName: 'vanessa',
      pass: '123456'
    });

    req.flush(mockLoginResponse);

    expect(localStorage.getItem('token')).toBe(mockLoginResponse.token);
    expect(localStorage.getItem('currentUser')).toBe(JSON.stringify(mockUser));
    expect(service.getCurrentUser()).toEqual(mockUser);
  });

  it('should emit current user after successful login', () => {
    setupService();

    const emittedUsers: Array<User | null> = [];

    service.currentUser$.subscribe((user) => {
      emittedUsers.push(user);
    });

    service.login('vanessa', '123456').subscribe();

    const req = httpMock.expectOne(loginUrl);
    req.flush(mockLoginResponse);

    expect(emittedUsers[0]).toBeNull();
    expect(emittedUsers[1]).toEqual(mockUser);
  });

  it('should send forgot password request', () => {
    setupService();

    const correo = 'vanessa@example.com';
    const mockResponse = 'Correo enviado correctamente';

    service.forgotPassword(correo).subscribe((response) => {
      expect(response).toBe(mockResponse);
    });

    const req = httpMock.expectOne(`${passwordUrl}/forgot`);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ correo });
    expect(req.request.responseType).toBe('text');

    req.flush(mockResponse);
  });

  it('should send reset password request', () => {
    setupService();

    const token = 'reset-token';
    const password = 'new-password';
    const mockResponse = 'Contraseña actualizada correctamente';

    service.resetPassword(token, password).subscribe((response) => {
      expect(response).toBe(mockResponse);
    });

    const req = httpMock.expectOne(`${passwordUrl}/reset`);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      token,
      password
    });
    expect(req.request.responseType).toBe('text');

    req.flush(mockResponse);
  });

  it('should validate reset token', () => {
    setupService();

    const token = 'valid-token';
    const mockResponse = 'Token válido';

    service.validateResetToken(token).subscribe((response) => {
      expect(response).toBe(mockResponse);
    });

    const req = httpMock.expectOne(`${passwordUrl}/validate?token=${token}`);

    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('text');

    req.flush(mockResponse);
  });

  it('should logout and clear localStorage and current user', () => {
    setupService();

    localStorage.setItem('token', 'mock-token');
    localStorage.setItem('currentUser', JSON.stringify(mockUser));

    service.logout();

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('currentUser')).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
  });

  it('should get token from localStorage', () => {
    setupService();

    localStorage.setItem('token', 'mock-token');

    expect(service.getToken()).toBe('mock-token');
  });

  it('should return null when token does not exist', () => {
    setupService();

    expect(service.getToken()).toBeNull();
  });

  it('should return true when user is authenticated', () => {
    setupService();

    localStorage.setItem('token', 'mock-token');

    expect(service.isAuthenticated()).toBe(true);
  });

  it('should return false when user is not authenticated', () => {
    setupService();

    expect(service.isAuthenticated()).toBe(false);
  });

  it('should return current user', () => {
    setupService();

    service.login('vanessa', '123456').subscribe();

    const req = httpMock.expectOne(loginUrl);
    req.flush(mockLoginResponse);

    expect(service.getCurrentUser()).toEqual(mockUser);
  });

  it('should return false for isAdmin when token does not exist', () => {
    setupService();

    expect(service.isAdmin()).toBe(false);
  });

  it('should return true for isAdmin when token role is ADMIN', () => {
    setupService();

    const adminToken = createFakeJwt({
      sub: 'vanessa',
      role: 'ADMIN'
    });

    localStorage.setItem('token', adminToken);

    expect(service.isAdmin()).toBe(true);
  });

  it('should return false for isAdmin when token role is USER', () => {
    setupService();

    const userToken = createFakeJwt({
      sub: 'vanessa',
      role: 'USER'
    });

    localStorage.setItem('token', userToken);

    expect(service.isAdmin()).toBe(false);
  });

  it('should return false for isAdmin when token is invalid', () => {
    setupService();

    localStorage.setItem('token', 'invalid-token');

    expect(service.isAdmin()).toBe(false);
  });

  it('should return true for isAdmin when token role is SUPERADMIN', () => {
    setupService();

    const superAdminToken = createFakeJwt({
      sub: 'vanessa',
      role: 'SUPERADMIN'
    });

    localStorage.setItem('token', superAdminToken);

    expect(service.isAdmin()).toBe(true);
  });

  it('should return true for isSuperAdmin when token role is SUPERADMIN', () => {
    setupService();

    const superAdminToken = createFakeJwt({
      sub: 'vanessa',
      role: 'SUPERADMIN'
    });

    localStorage.setItem('token', superAdminToken);

    expect(service.isSuperAdmin()).toBe(true);
  });

  it('should return false for isSuperAdmin when token role is ADMIN', () => {
    setupService();

    const adminToken = createFakeJwt({
      sub: 'vanessa',
      role: 'ADMIN'
    });

    localStorage.setItem('token', adminToken);

    expect(service.isSuperAdmin()).toBe(false);
  });

  it('should load stored user from localStorage when service is created', () => {
    localStorage.setItem('currentUser', JSON.stringify(mockUser));

    setupService();

    expect(service.getCurrentUser()).toEqual(mockUser);
  });

  it('should remove stored user when localStorage contains invalid JSON', () => {
    localStorage.setItem('currentUser', 'invalid-json');

    setupService();

    expect(localStorage.getItem('currentUser')).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
  });

  it('should handle connection error with status 0', () => {
    setupService();

    service.login('vanessa', '123456').subscribe({
      next: () => {
        throw new Error('Expected error, but got successful response');
      },
      error: (errorMessage) => {
        expect(errorMessage).toBe('No se pudo conectar al servidor');
      }
    });

    const req = httpMock.expectOne(loginUrl);

    req.flush('Network error', {
      status: 0,
      statusText: 'Unknown Error'
    });

    expect(console.error).toHaveBeenCalled();
  });

  it('should handle 401 error', () => {
    setupService();

    service.login('vanessa', 'wrong-password').subscribe({
      next: () => {
        throw new Error('Expected error, but got successful response');
      },
      error: (errorMessage) => {
        expect(errorMessage).toBe('Credenciales incorrectas');
      }
    });

    const req = httpMock.expectOne(loginUrl);

    req.flush(
      { message: 'Unauthorized' },
      {
        status: 401,
        statusText: 'Unauthorized'
      }
    );

    expect(console.error).toHaveBeenCalled();
  });

  it('should handle 400 error using error.error value', () => {
    setupService();

    service.forgotPassword('bad-email').subscribe({
      next: () => {
        throw new Error('Expected error, but got successful response');
      },
      error: (errorMessage) => {
        expect(errorMessage).toBe('Correo inválido');
      }
    });

    const req = httpMock.expectOne(`${passwordUrl}/forgot`);

    req.flush(
      { error: 'Correo inválido' },
      {
        status: 400,
        statusText: 'Bad Request'
      }
    );

    expect(console.error).toHaveBeenCalled();
  });

  it('should handle 400 error with default invalid request message', () => {
    setupService();

    service.forgotPassword('bad-email').subscribe({
      next: () => {
        throw new Error('Expected error, but got successful response');
      },
      error: (errorMessage) => {
        expect(errorMessage).toBe('Solicitud inválida');
      }
    });

    const req = httpMock.expectOne(`${passwordUrl}/forgot`);

    req.flush(
      {},
      {
        status: 400,
        statusText: 'Bad Request'
      }
    );

    expect(console.error).toHaveBeenCalled();
  });

  it('should handle string error response', () => {
    setupService();

    service.resetPassword('token', 'password').subscribe({
      next: () => {
        throw new Error('Expected error, but got successful response');
      },
      error: (errorMessage) => {
        expect(errorMessage).toBe('Token expirado');
      }
    });

    const req = httpMock.expectOne(`${passwordUrl}/reset`);

    req.flush('Token expirado', {
      status: 500,
      statusText: 'Internal Server Error'
    });

    expect(console.error).toHaveBeenCalled();
  });

  it('should handle error response with message property', () => {
    setupService();

    service.validateResetToken('invalid-token').subscribe({
      next: () => {
        throw new Error('Expected error, but got successful response');
      },
      error: (errorMessage) => {
        expect(errorMessage).toBe('Token inválido');
      }
    });

    const req = httpMock.expectOne(`${passwordUrl}/validate?token=invalid-token`);

    req.flush(
      { message: 'Token inválido' },
      {
        status: 500,
        statusText: 'Internal Server Error'
      }
    );

    expect(console.error).toHaveBeenCalled();
  });

  it('should handle unknown error with default message', () => {
    setupService();

    service.login('vanessa', '123456').subscribe({
      next: () => {
        throw new Error('Expected error, but got successful response');
      },
      error: (errorMessage) => {
        expect(errorMessage).toBe('Usuario o contraseña incorrectos.');
      }
    });

    const req = httpMock.expectOne(loginUrl);

    req.flush(
      {},
      {
        status: 500,
        statusText: 'Internal Server Error'
      }
    );

    expect(console.error).toHaveBeenCalled();
  });
});