import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { FEATURES } from '../features/features'; 

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!FEATURES.adminLogsEnabled) {
    router.navigate(['/InicioLogueado']); 
    return false;
  }

  if (authService.isAuthenticated() && authService.isAdmin()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};