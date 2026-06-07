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
  const passwordURL = CONSTANTS.API.BASE_URL + CONSTANTS.API.USERS + '/profile/password';
  const updateURL = CONSTANTS.API.BASE_URL + CONSTANTS.API.USERS + '/profile';

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

  describe('changePassword', () => {

    it('should PATCH /api/profile/password with credentials', () => {
      service.changePassword('oldPass123', 'newPass456').subscribe(res => {
        expect(res).toBeTruthy();
      });

      const req = httpMock.expectOne(passwordURL);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual({
        currentPassword: 'oldPass123',
        newPassword: 'newPass456'
      });
      req.flush({ message: 'Contraseña actualizada' });
    });

    it('should send currentPassword and newPassword in the body', () => {
      service.changePassword('actual123', 'nueva456').subscribe();

      const req = httpMock.expectOne(passwordURL);
      expect(req.request.body.currentPassword).toBe('actual123');
      expect(req.request.body.newPassword).toBe('nueva456');
      req.flush({});
    });

    it('should propagate 400 error when current password is wrong', () => {
      let errorStatus = 0;

      service.changePassword('incorrecta', 'nueva456').subscribe({
        error: (err) => { errorStatus = err.status; }
      });

      const req = httpMock.expectOne(passwordURL);
      req.flush({ message: 'Contraseña incorrecta' }, { status: 400, statusText: 'Bad Request' });

      expect(errorStatus).toBe(400);
    });
  });

  describe('updateProfile', () => {

    it('should PATCH /api/users/profile with the provided data', () => {
      const payload = { userName: 'nuevo_nombre', correo: 'nuevo@mail.com' };
      const mockResponse = { ...mockUser, userName: 'nuevo_nombre', correo: 'nuevo@mail.com' } as User;

      service.updateProfile(payload).subscribe(res => {
        expect(res.userName).toBe('nuevo_nombre');
        expect(res.correo).toBe('nuevo@mail.com');
      });

      const req = httpMock.expectOne(updateURL);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual(payload);
      req.flush(mockResponse);
    });

    it('should send only the fields provided', () => {
      const payload = { birthday: '2000-05-15' };

      service.updateProfile(payload).subscribe();

      const req = httpMock.expectOne(updateURL);
      expect(req.request.body).toEqual({ birthday: '2000-05-15' });
      expect(req.request.body.userName).toBeUndefined();
      req.flush(mockUser);
    });

    it('should propagate 400 error when userName is taken', () => {
      let errorStatus = 0;
      service.updateProfile({ userName: 'ocupado' }).subscribe({
        error: (err) => { errorStatus = err.status; }
      });

      const req = httpMock.expectOne(updateURL);
      req.flush({ message: 'El nombre de usuario ya está en uso' }, { status: 400, statusText: 'Bad Request' });
      expect(errorStatus).toBe(400);
    });

    it('should call PATCH exactly once', () => {
      service.updateProfile({ correo: 'test@mail.com' }).subscribe();
      const req = httpMock.expectOne(updateURL);
      expect(req.request.method).toBe('PATCH');
      req.flush(mockUser);
    });
  });

  describe('updateProfilePicture', () => {
    const pictureURL = CONSTANTS.API.BASE_URL + CONSTANTS.API.USERS + '/profile/picture';

    it('should PATCH /api/users/profile/picture with FormData', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });

      service.updateProfilePicture(file).subscribe(res => {
        expect(res).toEqual(mockUser);
      });

      const req = httpMock.expectOne(pictureURL);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toBeInstanceOf(FormData);
      req.flush(mockUser);
    });

    it('should append the file under the key "file" in FormData', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });

      service.updateProfilePicture(file).subscribe();

      const req = httpMock.expectOne(pictureURL);
      expect(req.request.body.get('file')).toBe(file);
      req.flush(mockUser);
    });

    it('should return the updated user after upload', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });
      const updatedUser = { ...mockUser, profilePictureUrl: 'https://res.cloudinary.com/test/user_1.jpg' } as User;

      service.updateProfilePicture(file).subscribe(res => {
        expect(res.profilePictureUrl).toBe('https://res.cloudinary.com/test/user_1.jpg');
      });

      const req = httpMock.expectOne(pictureURL);
      req.flush(updatedUser);
    });

    it('should call PATCH exactly once', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });

      service.updateProfilePicture(file).subscribe();

      const req = httpMock.expectOne(pictureURL);
      expect(req.request.method).toBe('PATCH');
      req.flush(mockUser);
    });

    it('should propagate 400 error when file is not an image', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'documento.pdf', { type: 'application/pdf' });
      let errorStatus = 0;

      service.updateProfilePicture(file).subscribe({
        error: (err) => { errorStatus = err.status; }
      });

      const req = httpMock.expectOne(pictureURL);
      req.flush({ message: 'El archivo debe ser una imagen.' }, { status: 400, statusText: 'Bad Request' });

      expect(errorStatus).toBe(400);
    });

    it('should propagate 401 error when user is not authenticated', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });
      let errorStatus = 0;

      service.updateProfilePicture(file).subscribe({
        error: (err) => { errorStatus = err.status; }
      });

      const req = httpMock.expectOne(pictureURL);
      req.flush({ message: 'No autenticado.' }, { status: 401, statusText: 'Unauthorized' });

      expect(errorStatus).toBe(401);
    });
  });
});