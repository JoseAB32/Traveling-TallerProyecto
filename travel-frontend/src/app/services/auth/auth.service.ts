import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { CONSTANTS } from '../../utils/constants';

export interface User {
  id?: number;
  correo: string;
  userName: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  userName: string;
  correo: string;
  message: string;
  id: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.LOGIN;
  private authUrl = CONSTANTS.API.BASE_URL + '/auth';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadStoredUser();
  }


  private loadStoredUser() {
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      try {
        this.currentUserSubject.next(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem('currentUser');
      }
    }
  }


  login(userName: string, pass: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}`, { userName, pass })
      .pipe(
        tap(response => {
          console.log('✅ Login exitoso:', response);

          const user: User = {
            id: response.id,
            correo: response.correo,
            userName: response.userName
          };

          localStorage.setItem('token', response.token);
          localStorage.setItem('currentUser', JSON.stringify(user));

          this.currentUserSubject.next(user);
        }),
        catchError(this.handleError)
      );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isAdmin(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub === 'admin';
    } catch {
      return false;
    }
  }

  forgotPassword(correo: string): Observable<any> {
    return this.http.post(`${this.authUrl}/forgot-password`, { correo })
      .pipe(
        tap(() => console.log('📧 Solicitud de recuperación enviada')),
        catchError(this.handleError)
      );
  }

  resetPassword(
    token: string,
    password: string,
    confirmPassword: string
  ): Observable<boolean> {

    return this.http.post<boolean>(`${this.authUrl}/reset-password`, {
      token,
      password,
      confirmPassword
    }).pipe(
      tap(res => console.log('🔑 Reset password response:', res)),
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    console.error('❌ Error:', error);

    let errorMessage = 'Error en la autenticación';

    if (error.status === 0) {
      errorMessage = 'No se pudo conectar al servidor. Verifica que el backend esté corriendo.';
    } else if (typeof error.error === 'string') {
      errorMessage = error.error;
    } else if (error.error?.detail) {
      errorMessage = error.error.detail;
    } else if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.error?.error) {
      errorMessage = error.error.error;
    } else if (error.status === 401) {
      errorMessage = 'Usuario o contraseña incorrectos';
    } else if (error.status === 400) {
      errorMessage = error.error?.error || 'Error en la solicitud';
    }

    return throwError(() => errorMessage);
  }
}