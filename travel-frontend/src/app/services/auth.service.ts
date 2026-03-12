import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

export interface User {
  id?: number;
  username: string;
  email: string;
  role?: string;
  token?: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  email: string;
  role: string;
  message: string;
  id: number;
}

export interface RegisterData {
  username: string;
  email: string;
  password: string;
  birthday?: string;
  city?: string;
}

export interface RegisterResponse {
  message: string;
  id: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient, 
    private router: Router
  ) {
    this.loadStoredUser();
  }

  private loadStoredUser() {
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      try {
        this.currentUserSubject.next(JSON.parse(storedUser));
      } catch (e) {
        localStorage.removeItem('currentUser');
      }
    }
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { username, password })
      .pipe(
        tap(response => {
          console.log('✅ Login exitoso:', response);
          const user: User = {
            id: response.id,
            username: response.username,
            email: response.email,
            role: response.role,
            token: response.token
          };
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        }),
        catchError(this.handleError)
      );
  }

  // ✅ NUEVO: Método register
  register(userData: RegisterData): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/register`, userData)
      .pipe(
        tap(response => {
          console.log('✅ Registro exitoso:', response);
        }),
        catchError(this.handleError)
      );
  }

  logout() {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.currentUserSubject.value?.token || null;
  }

  isAuthenticated(): boolean {
    return !!this.currentUserSubject.value;
  }

  private handleError(error: HttpErrorResponse) {
    console.error('❌ Error:', error);
    
    let errorMessage = 'Error en la autenticación';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      if (error.error && typeof error.error === 'string') {
        errorMessage = error.error;
      } else if (error.error && error.error.error) {
        errorMessage = error.error.error;
      } else if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else if (error.status === 401) {
        errorMessage = 'Usuario o contraseña incorrectos';
      } else if (error.status === 0) {
        errorMessage = 'No se pudo conectar al servidor';
      }
    }
    
    return throwError(() => errorMessage);
  }
}