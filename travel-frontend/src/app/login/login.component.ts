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
    email: '',
    password: ''
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
    this.credentials.email = this.credentials.email.trim();
    this.credentials.password = this.credentials.password.trim();
    
    if (!this.credentials.email) {
      this.error = 'El correo es requerido';
      return;
    }
    
    if (!this.credentials.password) {
      this.error = 'La contraseña es requerida';
      return;
    }

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    // ⚠️ Adaptación: backend espera userName, enviamos email como userName
    this.subscription = this.authService.login(
      this.credentials.email,
      this.credentials.password
    ).subscribe({
      next: () => {
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
    this.subscription?.unsubscribe();
  }
}