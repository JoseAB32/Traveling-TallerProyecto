import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
    //   console.log('Error interceptado:', {
    //     status: error.status,
    //     message: error.message,
    //     url: error.url,
    //     error: error.error
    //   });
  
      if (error.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('currentUser');

        router.navigate(['/login'], {
          queryParams: { sessionExpired: 'true' }
        });
      }

      if (error.status === 403) {
        router.navigate(['/InicioLogueado']);
      }

      return throwError(() => error);
    })
  );
};