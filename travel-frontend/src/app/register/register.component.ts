import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService, RegisterData, RegisterResponse } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  // Tipado explícito
  user: RegisterData = {
    username: '',
    email: '',
    password: ''
  };
  
  loading = false;
  error = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    // Validación manual
    if (!this.user.username || !this.user.email || !this.user.password) {
      this.error = 'Por favor completa todos los campos';
      return;
    }

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.authService.register(this.user)
      .subscribe({
        next: (response: RegisterResponse) => {  // ✅ Tipado explícito
          console.log('✅ Registro exitoso:', response);
          this.successMessage = '¡Registro exitoso! Redirigiendo al login...';
          
          // Redirigir después de 1.5 segundos
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 1500);
        },
        error: (errorMessage: string) => {  // ✅ Tipado explícito
          console.error('❌ Error en registro:', errorMessage);
          this.error = errorMessage;
          this.loading = false;
        }
      });
  }
}