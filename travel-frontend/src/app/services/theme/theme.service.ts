import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly storageKey = 'traveling-theme';

  isDarkMode = signal(false);

  constructor(
    @Inject(DOCUMENT) private document: Document,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    if (!isPlatformBrowser(this.platformId)) return;

    const savedTheme = localStorage.getItem(this.storageKey);
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    const shouldUseDark = savedTheme
      ? savedTheme === 'dark'
      : prefersDark;

    this.setTheme(shouldUseDark, false);
  }

  toggleTheme(): void {
    this.setTheme(!this.isDarkMode());
  }

  setTheme(isDark: boolean, save = true): void {
    this.isDarkMode.set(isDark);

    this.document.body.classList.toggle('dark-mode', isDark);
    this.document.documentElement.classList.toggle('dark-mode', isDark);

    if (save && isPlatformBrowser(this.platformId)) {
      localStorage.setItem(this.storageKey, isDark ? 'dark' : 'light');
    }
  }
}