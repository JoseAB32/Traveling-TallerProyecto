import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { CONSTANTS } from '../../utils/constants';

export type Role = 'USER' | 'ADMIN' | 'SUPERADMIN';

export interface User {
  id?: number;
  correo: string;
  userName: string;
  role: Role;
}

export interface LoginResponse {
  token: string;
  type: string;
  userName: string;
  correo: string;
  message: string;
  id: number;
  role: Role;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private BASE_URL = CONSTANTS.API.BASE_URL;

  private LOGIN_URL = this.BASE_URL + CONSTANTS.API.LOGIN;
  private PASSWORD_URL = this.BASE_URL + '/api/password';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadStoredUser();
  }


  login(userName: string, pass: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.LOGIN_URL, { userName, pass })
      .pipe(
        tap(response => {
          const user: User = {
            id: response.id,
            correo: response.correo,
            userName: response.userName,
            role: response.role
          };

          localStorage.setItem('token', response.token);
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        }),
        catchError(this.handleError)
      );
  }

  forgotPassword(correo: string): Observable<string> {
    return this.http.post(`${this.PASSWORD_URL}/forgot`, { correo }, { responseType: 'text' })
      .pipe(catchError(this.handleError));
  }

  resetPassword(token: string, password: string): Observable<string> {
    return this.http.post(`${this.PASSWORD_URL}/reset`, { token, password }, { responseType: 'text' })
      .pipe(catchError(this.handleError));
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

  getRole(): Role | null {
    const token = this.getToken();

    if (!token) {
      return null;
    }

    try {
      const parts = token.split('.');

      if (parts.length < 2) {
        return null;
      }

      const base64 = parts[1]
        .replace(/-/g, '+')
        .replace(/_/g, '/');

      const paddedBase64 = base64.padEnd(
        base64.length + (4 - base64.length % 4) % 4,
        '='
      );

      const payload = JSON.parse(atob(paddedBase64));
      const role = payload.role;

      if (role === 'USER' || role === 'ADMIN' || role === 'SUPERADMIN') {
        return role;
      }

      return null;
    } catch {
      return null;
    }
  }

  isAdmin(): boolean {
    const role = this.getRole();

    return role === 'ADMIN' || role === 'SUPERADMIN';
  }

  isSuperAdmin(): boolean {
    return this.getRole() === 'SUPERADMIN';
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

  private handleError(error: HttpErrorResponse) {

    console.error('❌ Error:', error);

    let errorMessage = 'Usuario o contraseña incorrectos.';

    if (error.status === 0) {
      errorMessage = 'No se pudo conectar al servidor';
    } 
    else if (error.status === 401) {
      errorMessage = 'Credenciales incorrectas';
    } 
    else if (error.status === 400) {
      errorMessage = error.error?.error || 'Solicitud inválida';
    } 
    else if (typeof error.error === 'string') {
      errorMessage = error.error;
    } 
    else if (error.error?.message) {
      errorMessage = error.error.message;
    }

    return throwError(() => errorMessage);
  }

  validateResetToken(token: string): Observable<string> {
  return this.http.get(`${this.PASSWORD_URL}/validate?token=${token}`, { responseType: 'text' })
    .pipe(catchError(this.handleError));
  }
}