/// <reference types="jest" />

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile.component';
import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { UserService } from '../../services/user/user.service';
import { CityService } from '../../services/city/city.service';
import { AuthService } from '../../services/auth/auth.service';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslocoTestingModule, TranslocoTestingOptions } from '@jsverse/transloco';
import { Component } from '@angular/core';
import { User } from '../../models/user/user';

@Component({ selector: 'app-header', template: '', standalone: true }) class HeaderStub {}
@Component({ selector: 'app-footer', template: '', standalone: true }) class FooterStub {}

const translocoConfig: TranslocoTestingOptions = {
  langs: { es: {
    'profile.loading':    'Cargando...',
    'profile.retry':      'Reintentar',
    'profile.errorLoading': 'Error al cargar perfil',
    'profile.myData':     'Mis datos',
    'profile.email':      'Correo',
    'profile.birthday':   'Fecha de nacimiento',
    'profile.city':       'Ciudad',
    'profile.noCity':     'Sin ciudad',
    'profile.edit':       'Editar',
    'profile.changePhoto':'Cambiar foto',
  }},
};

const mockProfile: User = {
  id: 1,
  userName: 'Ana Rojas',
  correo: 'ana@example.com',
  birthday: '1995-04-12',
  city: { id: 2, name: 'Cochabamba', state: true },
  state: true
};

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let userServiceMock: jest.Mocked<Pick<UserService, 'getProfile'>>;
  let cityServiceMock: jest.Mocked<Pick<CityService, 'getCities'>>;

  beforeEach(async () => {
    userServiceMock = { getProfile: jest.fn() };
    cityServiceMock = { getCities: jest.fn() };

    await TestBed.configureTestingModule({
      imports: [
        ProfileComponent,
        TranslocoTestingModule.forRoot(translocoConfig)
      ],
      providers: [
        { provide: UserService, useValue: userServiceMock },
        { provide: CityService, useValue: cityServiceMock },
        { provide: AuthService, useValue: {} }
      ]
    })
    .overrideComponent(ProfileComponent, {
      remove: { imports: [HeaderComponent, FooterComponent] },
      add:    { imports: [HeaderStub, FooterStub] }
    })
    .compileComponents();
  });


  function createComponent() {
    fixture   = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
  }

  describe('creación', () => {
    it('should create', () => {
      userServiceMock.getProfile.mockReturnValue(of(mockProfile));
      createComponent();
      fixture.detectChanges();
      expect(component).toBeTruthy();
    });
  });


  describe('loadProfile — éxito', () => {
    beforeEach(() => {
      userServiceMock.getProfile.mockReturnValue(of(mockProfile));
      createComponent();
      fixture.detectChanges();
    });

    it('debe asignar el perfil recibido', () => {
      expect(component.profile).toEqual(mockProfile);
    });

    it('debe poner isLoading en false', () => {
      expect(component.isLoading).toBe(false);
    });

    it('debe dejar error en null', () => {
      expect(component.error).toBeNull();
    });

    it('debe llamar a getProfile exactamente una vez', () => {
      expect(userServiceMock.getProfile).toHaveBeenCalledTimes(1);
    });
  });

  describe('loadProfile — error', () => {
    beforeEach(() => {
      userServiceMock.getProfile.mockReturnValue(
        throwError(() => new HttpErrorResponse({ status: 500 }))
      );
      createComponent();
      fixture.detectChanges();
    });

    it('debe poner error con la clave de traducción', () => {
      expect(component.error).toBe('profile.errorLoading');
    });

    it('debe poner isLoading en false', () => {
      expect(component.isLoading).toBe(false);
    });

    it('debe dejar profile en null', () => {
      expect(component.profile).toBeNull();
    });
  });

  
  describe('loadProfile — reintento', () => {
    it('debe volver a llamar a getProfile al reintentar', () => {
      userServiceMock.getProfile
        .mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 500 })))
        .mockReturnValueOnce(of(mockProfile));

      createComponent();
      fixture.detectChanges();

      expect(component.error).toBe('profile.errorLoading');

      component.loadProfile();
      fixture.detectChanges();

      expect(userServiceMock.getProfile).toHaveBeenCalledTimes(2);
      expect(component.profile).toEqual(mockProfile);
      expect(component.error).toBeNull();
    });
  });


  describe('getInitials', () => {
    beforeEach(() => {
      userServiceMock.getProfile.mockReturnValue(of(mockProfile));
      createComponent();
      fixture.detectChanges();
    });

    it('debe retornar la inicial en mayúscula del userName', () => {
      expect(component.getInitials()).toBe('A');
    });

    it('debe retornar "?" si no hay perfil', () => {
      component.profile = null;
      expect(component.getInitials()).toBe('?');
    });
  });


  describe('formatBirthday', () => {
    beforeEach(() => {
      userServiceMock.getProfile.mockReturnValue(of(mockProfile));
      createComponent();
      fixture.detectChanges();
    });

    it('debe retornar "—" si birthday es undefined', () => {
      expect(component.formatBirthday(undefined)).toBe('—');
    });

    it('debe retornar una fecha formateada si birthday es válido', () => {
      const result = component.formatBirthday('1995-04-12');
      expect(result).toBeTruthy();
      expect(result).not.toBe('—');
    });

    it('debe retornar el valor original si la fecha no es parseable', () => {
      const result = component.formatBirthday('no-es-fecha');
      expect(result).toBeDefined();
    });
  });


  describe('handlers pendientes', () => {
    beforeEach(() => {
      userServiceMock.getProfile.mockReturnValue(of(mockProfile));
      cityServiceMock.getCities.mockReturnValue(of([]));
      createComponent();
      fixture.detectChanges();
    });

    it('onEditProfile no debe lanzar error', () => {
      expect(() => component.onEditProfile()).not.toThrow();
    });

    it('onChangePhoto no debe lanzar error', () => {
      expect(() => component.onChangePhoto()).not.toThrow();
    });
  });

  describe('submitChangePassword — validaciones', () => {
    beforeEach(() => {
      userServiceMock.getProfile.mockReturnValue(of(mockProfile));
      createComponent();
      fixture.detectChanges();
    });

    it('debe mostrar error si algún campo está vacío', () => {
      component.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
      component.submitChangePassword();
      expect(component.passwordError).toBe('profile.passwordAllRequired');
    });

    it('debe mostrar error si la nueva contraseña tiene espacios', () => {
      component.passwordForm = {
        currentPassword: 'actual123',
        newPassword: 'nu eva1',
        confirmPassword: 'nu eva1'
      };
      component.submitChangePassword();
      expect(component.passwordError).toBe('profile.passwordNoSpaces');
    });

    it('debe mostrar error si la contraseña actual tiene espacios', () => {
      component.passwordForm = {
        currentPassword: 'act ual',
        newPassword: 'nueva12345',
        confirmPassword: 'nueva12345'
      };
      component.submitChangePassword();
      expect(component.passwordError).toBe('profile.passwordNoSpaces');
    });

    it('debe mostrar error si la contraseña tiene menos de 8 caracteres', () => {
      component.passwordForm = {
        currentPassword: 'actual123',
        newPassword: 'corta',
        confirmPassword: 'corta'
      };
      component.submitChangePassword();
      expect(component.passwordError).toBe('profile.passwordTooShort');
    });

    it('debe mostrar error si las contraseñas no coinciden', () => {
      component.passwordForm = {
        currentPassword: 'actual123',
        newPassword: 'nueva12345',
        confirmPassword: 'nueva99999'
      };
      component.submitChangePassword();
      expect(component.passwordError).toBe('profile.passwordMismatch');
    });

    it('debe llamar a changePassword si todo es válido', () => {
      const changePwdMock = jest.fn().mockReturnValue(of({}));
      (userServiceMock as any).changePassword = changePwdMock;

      component.passwordForm = {
        currentPassword: 'actual123',
        newPassword: 'nueva12345',
        confirmPassword: 'nueva12345'
      };
      component.submitChangePassword();

      expect(changePwdMock).toHaveBeenCalledWith('actual123', 'nueva12345');
    });

    it('debe mostrar éxito y limpiar el form tras cambio correcto', () => {
      const changePwdMock = jest.fn().mockReturnValue(of({}));
      (userServiceMock as any).changePassword = changePwdMock;

      component.passwordForm = {
        currentPassword: 'actual123',
        newPassword: 'nueva12345',
        confirmPassword: 'nueva12345'
      };
      component.submitChangePassword();

      expect(component.passwordSuccess).toBe(true);
      expect(component.passwordForm.currentPassword).toBe('');
      expect(component.passwordForm.newPassword).toBe('');
    });

    it('debe mostrar error 400 si la contraseña actual es incorrecta', () => {
      const changePwdMock = jest.fn().mockReturnValue(
        throwError(() => new HttpErrorResponse({ status: 400 }))
      );
      (userServiceMock as any).changePassword = changePwdMock;

      component.passwordForm = {
        currentPassword: 'incorrecta',
        newPassword: 'nueva12345',
        confirmPassword: 'nueva12345'
      };
      component.submitChangePassword();

      expect(component.passwordError).toBe('profile.passwordWrong');
    });

    it('debe mostrar error genérico si falla con 500', () => {
      const changePwdMock = jest.fn().mockReturnValue(
        throwError(() => new HttpErrorResponse({ status: 500 }))
      );
      (userServiceMock as any).changePassword = changePwdMock;

      component.passwordForm = {
        currentPassword: 'actual123',
        newPassword: 'nueva12345',
        confirmPassword: 'nueva12345'
      };
      component.submitChangePassword();

      expect(component.passwordError).toBe('profile.passwordError');
    });
  });

  describe('openEditProfile y submitEditProfile', () => {
    const mockCities = [
      { id: 1, name: 'La Paz',       state: true },
      { id: 2, name: 'Cochabamba',   state: true },
      { id: 3, name: 'Santa Cruz',   state: true }
    ];

    beforeEach(() => {
      userServiceMock.getProfile.mockReturnValue(of(mockProfile));
      cityServiceMock.getCities.mockReturnValue(of(mockCities));
      createComponent();
      fixture.detectChanges();
    });

    it('debe abrir el modal de edición', () => {
      component.openEditProfile();
      expect(component.showEditModal).toBe(true);
    });

    it('debe pre-rellenar el form con los datos actuales del perfil', () => {
      component.openEditProfile();
      expect(component.editForm.userName).toBe('Ana Rojas');
      expect(component.editForm.correo).toBe('ana@example.com');
      expect(component.editForm.birthday).toBe('1995-04-12');
      expect(component.editForm.cityId).toBe(2);
    });

    it('debe cargar la lista de ciudades al abrir', () => {
      component.openEditProfile();
      expect(cityServiceMock.getCities).toHaveBeenCalledTimes(1);
      expect(component.cities).toEqual(mockCities);
    });

    it('debe cerrar el modal y limpiar estados', () => {
      component.openEditProfile();
      component.closeEditModal();
      expect(component.showEditModal).toBe(false);
      expect(component.editError).toBeNull();
      expect(component.editSuccess).toBe(false);
    });

    it('debe mostrar error si userName está vacío', () => {
      component.openEditProfile();
      component.editForm.userName = '';
      component.submitEditProfile();
      expect(component.editError).toBe('profile.editRequiredFields');
    });

    it('debe mostrar error si correo está vacío', () => {
      component.openEditProfile();
      component.editForm.correo = '';
      component.submitEditProfile();
      expect(component.editError).toBe('profile.editRequiredFields');
    });

    it('debe llamar a updateProfile con los datos del form', () => {
      const updatedUser = { ...mockProfile, userName: 'nuevo_nombre' } as User;
      const updateMock = jest.fn().mockReturnValue(of(updatedUser));
      (userServiceMock as any).updateProfile = updateMock;

      component.openEditProfile();
      component.editForm.userName = 'nuevo_nombre';
      component.submitEditProfile();

      expect(updateMock).toHaveBeenCalledWith({
        userName: 'nuevo_nombre',
        correo:   'ana@example.com',
        birthday: '1995-04-12',
        cityId:   2
      });
    });

    it('debe actualizar el perfil y mostrar éxito', () => {
      const updatedUser = { ...mockProfile, userName: 'nuevo_nombre' } as User;
      (userServiceMock as any).updateProfile = jest.fn().mockReturnValue(of(updatedUser));

      component.openEditProfile();
      component.submitEditProfile();

      expect(component.editSuccess).toBe(true);
      expect(component.isSavingProfile).toBe(false);
      expect(component.profile?.userName).toBe('nuevo_nombre');
    });

    it('debe mostrar error 400 si el campo ya está en uso', () => {
      (userServiceMock as any).updateProfile = jest.fn().mockReturnValue(
        throwError(() => new HttpErrorResponse({ status: 400 }))
      );

      component.openEditProfile();
      component.submitEditProfile();

      expect(component.editError).toBe('profile.editFieldTaken');
      expect(component.isSavingProfile).toBe(false);
    });

    it('debe mostrar error genérico si falla con 500', () => {
      (userServiceMock as any).updateProfile = jest.fn().mockReturnValue(
        throwError(() => new HttpErrorResponse({ status: 500 }))
      );

      component.openEditProfile();
      component.submitEditProfile();

      expect(component.editError).toBe('profile.editError');
    });

    it('debe manejar error al cargar ciudades sin explotar', () => {
      cityServiceMock.getCities.mockReturnValue(
        throwError(() => new Error('Network error'))
      );

      expect(() => component.loadCities()).not.toThrow();
      expect(component.isLoadingCities).toBe(false);
    });
  });

  describe('uploadProfilePicture', () => {
    let updatePictureMock: jest.Mock;

    beforeEach(() => {
      userServiceMock.getProfile.mockReturnValue(of(mockProfile));
      updatePictureMock = jest.fn();
      (userServiceMock as any).updateProfilePicture = updatePictureMock;
      createComponent();
      fixture.detectChanges();
    });

    it('debe subir la imagen y actualizar profilePictureUrl', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });
      const updatedUser = { ...mockProfile, profilePictureUrl: 'https://res.cloudinary.com/test/user_1.jpg' } as User;
      updatePictureMock.mockReturnValue(of(updatedUser));

      component.uploadProfilePicture(file);

      expect(updatePictureMock).toHaveBeenCalledWith(file);
      expect(component.profile?.profilePictureUrl).toContain('https://res.cloudinary.com/test/user_1.jpg');
      expect(component.isUploadingPhoto).toBe(false);
      expect(component.photoError).toBeNull();
    });

    it('debe poner isUploadingPhoto en false tras subida exitosa', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });
      updatePictureMock.mockReturnValue(of({ ...mockProfile }));

      component.uploadProfilePicture(file);

      expect(component.isUploadingPhoto).toBe(false);
    });

    it('debe limpiar photoError antes de subir', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });
      component.photoError = 'profile.photoError';
      updatePictureMock.mockReturnValue(of({ ...mockProfile }));

      component.uploadProfilePicture(file);

      expect(component.photoError).toBeNull();
    });

    it('debe rechazar archivo mayor a 5MB con error photoTooLarge', () => {
      const bigFile = new File([new Uint8Array(6 * 1024 * 1024)], 'grande.jpg', { type: 'image/jpeg' });

      component.uploadProfilePicture(bigFile);

      expect(updatePictureMock).not.toHaveBeenCalled();
      expect(component.photoError).toBe('profile.photoTooLarge');
      expect(component.isUploadingPhoto).toBe(false);
    });

    it('debe mostrar error photoInvalidType si el backend responde 400', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });
      updatePictureMock.mockReturnValue(
        throwError(() => new HttpErrorResponse({ status: 400 }))
      );

      component.uploadProfilePicture(file);

      expect(component.photoError).toBe('profile.photoInvalidType');
      expect(component.isUploadingPhoto).toBe(false);
    });

    it('debe mostrar error photoError si el backend responde 500', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });
      updatePictureMock.mockReturnValue(
        throwError(() => new HttpErrorResponse({ status: 500 }))
      );

      component.uploadProfilePicture(file);

      expect(component.photoError).toBe('profile.photoError');
      expect(component.isUploadingPhoto).toBe(false);
    });

    it('debe añadir cache-buster a la URL si profilePictureUrl existe tras la subida', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });
      const updatedUser = { ...mockProfile, profilePictureUrl: 'https://res.cloudinary.com/test/user_1.jpg' } as User;
      updatePictureMock.mockReturnValue(of(updatedUser));

      component.uploadProfilePicture(file);

      expect(component.profile?.profilePictureUrl).toMatch(/\?t=\d+$/);
    });

    it('no debe añadir cache-buster si profilePictureUrl viene vacío', () => {
      const file = new File([new Uint8Array([1, 2, 3])], 'foto.jpg', { type: 'image/jpeg' });
      const updatedUser = { ...mockProfile, profilePictureUrl: undefined } as User;
      updatePictureMock.mockReturnValue(of(updatedUser));

      component.uploadProfilePicture(file);

      expect(component.profile?.profilePictureUrl).toBeUndefined();
    });
  });
});