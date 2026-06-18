import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';
import { FEATURES } from '../features/features';

export const adminGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isAdminLogsRoute = state.url.startsWith('/admin-view');

  if (isAdminLogsRoute && !FEATURES.adminLogsEnabled) {
    router.navigate(['/InicioLogueado']);
    return false;
  }

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  if (authService.isAdmin()) {
    return true;
  }

  router.navigate(['/InicioLogueado']);
  return false;
};