/// <reference types="jest" />

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile.component';
import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { UserService } from '../../services/user/user.service';
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

  beforeEach(async () => {
    userServiceMock = { getProfile: jest.fn() };

    await TestBed.configureTestingModule({
      imports: [
        ProfileComponent,
        TranslocoTestingModule.forRoot(translocoConfig)
      ],
      providers: [
        { provide: UserService, useValue: userServiceMock },
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
});