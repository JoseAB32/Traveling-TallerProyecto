import { Component } from '@angular/core';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {

  correo: string = '';
  mensaje: string = '';
  loading: boolean = false;

  constructor(private authService: AuthService) {}

  enviar() {

    if (!this.correo) {
      this.mensaje = 'Ingrese un correo válido';
      return;
    }

    this.loading = true;
    this.mensaje = '';

    this.authService.forgotPassword(this.correo).subscribe({
      next: () => {
        this.mensaje = 'Si el correo existe, se envió un enlace de recuperación.';
        this.loading = false;
      },
      error: (err) => {
        this.mensaje = err;
        this.loading = false;
      }
    });
  }
}