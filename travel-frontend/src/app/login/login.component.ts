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
    username: '',
    password: ''
  };
  
  loading = false;
  error = '';
  successMessage = '';
  private subscription: Subscription | null = null;

  constructor(
    public authService: AuthService, 
    private router: Router
  ) {
    // Si ya está autenticado, redirigir al dashboard
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']); // ✅ CORREGIDO: redirige a dashboard
    }
  }

  onSubmit(): void {
    // Validación
    if (!this.credentials.username || !this.credentials.password) {
      this.error = 'Por favor completa todos los campos';
      return;
    }

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    // Guardar suscripción para poder cancelarla
    this.subscription = this.authService.login(
      this.credentials.username, 
      this.credentials.password
    ).subscribe({
      next: (response) => {
        console.log('✅ Login exitoso:', response);
        this.successMessage = '¡Login exitoso!';
        this.loading = false;
        
        // ✅ CORREGIDO: Redirigir al DASHBOARD, no al login
        this.router.navigate(['/dashboard']).then(() => {
          console.log('✅ Redirección a dashboard completada');
        }).catch(err => {
          console.error('❌ Error en redirección:', err);
        });
      },
      error: (errorMessage) => {
        console.error('❌ Error en login:', errorMessage);
        this.error = errorMessage;
        this.loading = false;
      }
    });
  }

  // Limpiar suscripción al destruir el componente
  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}