/// <reference types="jest" />

import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';
import { PLATFORM_ID } from '@angular/core';

describe('ThemeService', () => {
  const storageKey = 'traveling-theme';

  const mockMatchMedia = (matches: boolean): jest.Mock => {
    const matchMediaMock = jest.fn().mockImplementation((query: string) => ({
      matches,
      media: query,
      onchange: null,
      addListener: jest.fn(),
      removeListener: jest.fn(),
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
      dispatchEvent: jest.fn()
    }));

    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      configurable: true,
      value: matchMediaMock
    });

    return matchMediaMock;
  };

  const createService = (platformId: 'browser' | 'server' = 'browser'): ThemeService => {
    TestBed.configureTestingModule({
      providers: [
        ThemeService,
        { provide: DOCUMENT, useValue: document },
        { provide: PLATFORM_ID, useValue: platformId }
      ]
    });

    return TestBed.inject(ThemeService);
  };

  beforeEach(() => {
    localStorage.clear();
    document.body.classList.remove('dark-mode');
    document.documentElement.classList.remove('dark-mode');
    mockMatchMedia(false);
    jest.clearAllMocks();
  });

  afterEach(() => {
    TestBed.resetTestingModule();
    jest.restoreAllMocks();
    localStorage.clear();
    document.body.classList.remove('dark-mode');
    document.documentElement.classList.remove('dark-mode');
  });

  it('should be created', () => {
    const service = createService();

    expect(service).toBeTruthy();
  });

  it('should apply dark mode when saved theme is dark', () => {
    localStorage.setItem(storageKey, 'dark');
    const setItemSpy = jest.spyOn(Storage.prototype, 'setItem');

    const service = createService();

    expect(service.isDarkMode()).toBe(true);
    expect(document.body.classList.contains('dark-mode')).toBe(true);
    expect(document.documentElement.classList.contains('dark-mode')).toBe(true);

    // El constructor llama setTheme(..., false), por eso no debe volver a guardar.
    expect(setItemSpy).not.toHaveBeenCalled();
  });

  it('should apply light mode when saved theme is light even if system prefers dark', () => {
    mockMatchMedia(true);
    localStorage.setItem(storageKey, 'light');

    const service = createService();

    expect(service.isDarkMode()).toBe(false);
    expect(document.body.classList.contains('dark-mode')).toBe(false);
    expect(document.documentElement.classList.contains('dark-mode')).toBe(false);
  });

  it('should apply dark mode when there is no saved theme and system prefers dark', () => {
    const matchMediaMock = mockMatchMedia(true);

    const service = createService();

    expect(matchMediaMock).toHaveBeenCalledWith('(prefers-color-scheme: dark)');
    expect(service.isDarkMode()).toBe(true);
    expect(document.body.classList.contains('dark-mode')).toBe(true);
    expect(document.documentElement.classList.contains('dark-mode')).toBe(true);
  });

  it('should apply light mode when there is no saved theme and system does not prefer dark', () => {
    const matchMediaMock = mockMatchMedia(false);

    const service = createService();

    expect(matchMediaMock).toHaveBeenCalledWith('(prefers-color-scheme: dark)');
    expect(service.isDarkMode()).toBe(false);
    expect(document.body.classList.contains('dark-mode')).toBe(false);
    expect(document.documentElement.classList.contains('dark-mode')).toBe(false);
  });

  it('should enable dark mode and save it when setTheme(true) is called', () => {
    const service = createService();
    const setItemSpy = jest.spyOn(Storage.prototype, 'setItem');

    service.setTheme(true);

    expect(service.isDarkMode()).toBe(true);
    expect(document.body.classList.contains('dark-mode')).toBe(true);
    expect(document.documentElement.classList.contains('dark-mode')).toBe(true);
    expect(setItemSpy).toHaveBeenCalledWith(storageKey, 'dark');
  });

  it('should disable dark mode and save light when setTheme(false) is called', () => {
    const service = createService();
    const setItemSpy = jest.spyOn(Storage.prototype, 'setItem');

    service.setTheme(true);
    service.setTheme(false);

    expect(service.isDarkMode()).toBe(false);
    expect(document.body.classList.contains('dark-mode')).toBe(false);
    expect(document.documentElement.classList.contains('dark-mode')).toBe(false);
    expect(setItemSpy).toHaveBeenLastCalledWith(storageKey, 'light');
  });

  it('should not save in localStorage when setTheme is called with save false', () => {
    const service = createService();
    const setItemSpy = jest.spyOn(Storage.prototype, 'setItem');

    service.setTheme(true, false);

    expect(service.isDarkMode()).toBe(true);
    expect(document.body.classList.contains('dark-mode')).toBe(true);
    expect(document.documentElement.classList.contains('dark-mode')).toBe(true);
    expect(setItemSpy).not.toHaveBeenCalled();
  });

  it('should toggle from light to dark and save dark', () => {
    const service = createService();
    const setItemSpy = jest.spyOn(Storage.prototype, 'setItem');

    service.toggleTheme();

    expect(service.isDarkMode()).toBe(true);
    expect(document.body.classList.contains('dark-mode')).toBe(true);
    expect(document.documentElement.classList.contains('dark-mode')).toBe(true);
    expect(setItemSpy).toHaveBeenCalledWith(storageKey, 'dark');
  });

  it('should toggle from dark to light and save light', () => {
    localStorage.setItem(storageKey, 'dark');

    const service = createService();
    const setItemSpy = jest.spyOn(Storage.prototype, 'setItem');

    service.toggleTheme();

    expect(service.isDarkMode()).toBe(false);
    expect(document.body.classList.contains('dark-mode')).toBe(false);
    expect(document.documentElement.classList.contains('dark-mode')).toBe(false);
    expect(setItemSpy).toHaveBeenCalledWith(storageKey, 'light');
  });

  it('should not read browser preferences when platform is server', () => {
    const matchMediaMock = mockMatchMedia(true);

    const service = createService('server');

    expect(service.isDarkMode()).toBe(false);
    expect(matchMediaMock).not.toHaveBeenCalled();
    expect(document.body.classList.contains('dark-mode')).toBe(false);
    expect(document.documentElement.classList.contains('dark-mode')).toBe(false);
  });

  it('should not save in localStorage when platform is server', () => {
    const service = createService('server');
    const setItemSpy = jest.spyOn(Storage.prototype, 'setItem');

    service.setTheme(true);

    expect(service.isDarkMode()).toBe(true);
    expect(document.body.classList.contains('dark-mode')).toBe(true);
    expect(document.documentElement.classList.contains('dark-mode')).toBe(true);
    expect(setItemSpy).not.toHaveBeenCalled();
  });
});