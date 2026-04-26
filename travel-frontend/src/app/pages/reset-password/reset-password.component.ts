import { Component, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {

  password: string = '';
  confirmPassword: string = '';
  token: string = '';

  loading: boolean = false;
  error: string = '';
  
  // 🔥 NUEVAS VARIABLES
  validatingToken: boolean = true;
  tokenValid: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
      
      // 🔥 VALIDAR TOKEN AL INICIO
      if (this.token) {
        this.validateToken();
      } else {
        this.validatingToken = false;
        this.tokenValid = false;
        this.error = 'Token inválido o inexistente';
      }
    });
  }

  // 🔥 NUEVO MÉTODO
  validateToken() {
    this.authService.validateResetToken(this.token).subscribe({
      next: () => {
        this.tokenValid = true;
        this.validatingToken = false;
      },
      error: (err) => {
        this.tokenValid = false;
        this.validatingToken = false;
        this.error = err || 'El enlace ha expirado o es inválido';
      }
    });
  }

  onSubmit(form: NgForm) {
    // 🔥 VALIDACIÓN ADICIONAL
    if (!this.tokenValid) {
      this.error = 'El enlace ha expirado. Solicita un nuevo restablecimiento.';
      return;
    }

    if (form.invalid) return;

    if (this.password !== this.confirmPassword) {
      this.error = 'Las contraseñas no coinciden';
      return;
    }

    if (!this.token) {
      this.error = 'Token inválido o inexistente';
      return;
    }

    this.loading = true;
    this.error = '';

    this.authService.resetPassword(this.token, this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/reset-success']);
      },
      error: (err) => {
        this.error = err;
        this.loading = false;
      }
    });
  }

  goToLanding() {
    this.router.navigate(['/']);
  }
}