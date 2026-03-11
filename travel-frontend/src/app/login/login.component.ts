import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="login-container">
      <div class="login-card">
        <h2>Iniciar Sesión</h2>
        
        <div *ngIf="error" class="alert alert-error">
          {{ error }}
        </div>
        
        <div *ngIf="successMessage" class="alert alert-success">
          {{ successMessage }}
        </div>

        <form (ngSubmit)="onSubmit()" #loginForm="ngForm">
          <div class="form-group">
            <label for="username">Usuario</label>
            <input 
              type="text" 
              id="username"
              name="username"
              [(ngModel)]="credentials.username"
              required
              #username="ngModel"
              placeholder="Ingresa tu usuario"
              class="form-control"
              [class.error]="username.invalid && username.touched"
            >
            <div *ngIf="username.invalid && username.touched" class="error-message">
              El usuario es requerido
            </div>
          </div>

          <div class="form-group">
            <label for="password">Contraseña</label>
            <input 
              type="password" 
              id="password"
              name="password"
              [(ngModel)]="credentials.password"
              required
              #password="ngModel"
              placeholder="Ingresa tu contraseña"
              class="form-control"
              [class.error]="password.invalid && password.touched"
            >
            <div *ngIf="password.invalid && password.touched" class="error-message">
              La contraseña es requerida
            </div>
          </div>

          <button 
            type="submit" 
            [disabled]="loginForm.invalid || loading"
            class="btn btn-primary"
          >
            <span *ngIf="loading" class="spinner"></span>
            {{ loading ? 'Iniciando sesión...' : 'Iniciar Sesión' }}
          </button>
        </form>

        <p class="text-center">
          ¿No tienes cuenta? 
          <a routerLink="/register" class="link">Regístrate aquí</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: calc(100vh - 200px);
      padding: 1rem;
    }

    .login-card {
      background: white;
      padding: 2rem;
      border-radius: 10px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.1);
      width: 100%;
      max-width: 400px;
    }

    h2 {
      text-align: center;
      margin-bottom: 2rem;
      color: #333;
      font-size: 1.8rem;
    }

    .form-group {
      margin-bottom: 1.5rem;
    }

    label {
      display: block;
      margin-bottom: 0.5rem;
      color: #555;
      font-weight: 500;
    }

    .form-control {
      width: 100%;
      padding: 0.75rem;
      border: 2px solid #e0e0e0;
      border-radius: 5px;
      font-size: 1rem;
      transition: border-color 0.3s;
    }

    .form-control:focus {
      outline: none;
      border-color: #667eea;
    }

    .form-control.error {
      border-color: #f56565;
    }

    .error-message {
      color: #f56565;
      font-size: 0.875rem;
      margin-top: 0.25rem;
    }

    .alert {
      padding: 1rem;
      border-radius: 5px;
      margin-bottom: 1.5rem;
      text-align: center;
    }

    .alert-error {
      background: #fff5f5;
      color: #c53030;
      border: 1px solid #feb2b2;
    }

    .alert-success {
      background: #f0fff4;
      color: #276749;
      border: 1px solid #9ae6b4;
    }

    .btn {
      width: 100%;
      padding: 0.75rem;
      border: none;
      border-radius: 5px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
      position: relative;
    }

    .btn-primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .btn-primary:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
    }

    .btn:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }

    .spinner {
      display: inline-block;
      width: 1rem;
      height: 1rem;
      border: 2px solid rgba(255,255,255,0.3);
      border-radius: 50%;
      border-top-color: white;
      animation: spin 1s ease-in-out infinite;
      margin-right: 0.5rem;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .text-center {
      text-align: center;
      margin-top: 1.5rem;
      color: #666;
    }

    .link {
      color: #667eea;
      text-decoration: none;
      font-weight: 500;
    }

    .link:hover {
      text-decoration: underline;
    }
  `]
})
export class LoginComponent {
  credentials = {
    username: '',
    password: ''
  };
  
  loading = false;
  error = '';
  successMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.authService.login(this.credentials.username, this.credentials.password)
      .subscribe({
        next: () => {
          this.successMessage = '¡Login exitoso! Redirigiendo...';
          setTimeout(() => {
            this.router.navigate(['/']);
          }, 1500);
        },
        error: (err) => {
          console.error('Error en login:', err);
          this.error = err.error || 'Error al iniciar sesión. Verifica tus credenciales.';
          this.loading = false;
        }
      });
  }
}