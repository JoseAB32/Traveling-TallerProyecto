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
    this.credentials.userName = this.credentials.userName.trim();
    this.credentials.password = this.credentials.password.trim();
    
    if (!this.credentials.userName) {
      this.error = 'El nombre de usuario es requerido';
      return;
    }
    
    if (!this.credentials.password) {
      this.error = 'La contraseña es requerida';
      return;
    }

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.subscription = this.authService.login(
      this.credentials.userName,
      this.credentials.password
    ).subscribe({
      next: () => {
        this.successMessage = '¡Login exitoso!';
        this.loading = false;
        this.router.navigate(['/InicioLogueado']);
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