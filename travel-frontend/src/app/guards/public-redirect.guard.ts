// src/app/guards/public-redirect.guard.ts

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  exp: number;
}

function isTokenExpired(token: string): boolean {
  try {
    const decoded = jwtDecode<JwtPayload>(token);
    const expirationDate = decoded.exp * 1000;

    return Date.now() >= expirationDate;
  } catch {
    return true;
  }
}

export const publicRedirectGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = localStorage.getItem('token');

  if (!token) {
    return true;
  }

  if (isTokenExpired(token)) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    return true;
  }

  router.navigate(['/InicioLogueado']);
  return false;
};