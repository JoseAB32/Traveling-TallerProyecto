import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { FeatureService, Features } from '../services/features/feature.service';
import { map, catchError, of } from 'rxjs';

export function featureGuard(featureKey: keyof Features): CanActivateFn {
  return () => {
    const featureService = inject(FeatureService);
    const router = inject(Router);

    return featureService.loadFeatures().pipe(
      map(() => {
        if (featureService.isEnabled(featureKey)) {
          return true;
        }
        return router.createUrlTree(['/InicioLogueado']);
      }),
      catchError(() => {
        return of(router.createUrlTree(['/InicioLogueado']));
      })
    );
  };
}