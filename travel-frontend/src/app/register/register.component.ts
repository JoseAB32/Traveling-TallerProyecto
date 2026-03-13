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
  user: RegisterData = {
    userName: '',
    correo: '',
    pass: '',
    birthday: '',
    city: ''
  };
  
  loading = false;
  error = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    // Validación
    if (!this.user.userName || !this.user.correo || !this.user.pass) {
      this.error = 'Por favor completa todos los campos obligatorios';
      return;
    }

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.authService.register(this.user).subscribe({
      next: (response: RegisterResponse) => {
        console.log('✅ Registro exitoso:', response);
        this.successMessage = '¡Registro exitoso! Redirigiendo...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (errorMessage: string) => {
        console.error('❌ Error en registro:', errorMessage);
        this.error = errorMessage;
        this.loading = false;
      }
    });
  }
}