import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnDestroy {
  credentials = {
    userName: '',
    pass: ''
  };
  
  loading = false;
  error = '';
  successMessage = '';
  private subscription: Subscription | null = null;

  constructor(
    private authService: AuthService, 
    private router: Router
  ) {}

  onSubmit(): void {
    // Limpiar espacios
    this.credentials.userName = this.credentials.userName.trim();
    this.credentials.pass = this.credentials.pass.trim();
    
    // Validación
    if (!this.credentials.userName) {
      this.error = 'El usuario es requerido';
      return;
    }
    
    if (!this.credentials.pass) {
      this.error = 'La contraseña es requerida';
      return;
    }

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.subscription = this.authService.login(
      this.credentials.userName,
      this.credentials.pass
    ).subscribe({
      next: (response) => {
        this.successMessage = '¡Login exitoso!';
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (errorMessage: string) => {
        this.error = errorMessage;
        this.loading = false;
      }
    });
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}