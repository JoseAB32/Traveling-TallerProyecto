import { Component } from '@angular/core';
import { RouterModule, Router } from '@angular/router'; // 👈 IMPORTANTE
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule // 👈 AQUÍ ESTÁ LA CLAVE
  ],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {

  correo: string = '';
  loading = false;
  message = '';
  error = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  goToLanding() {
    this.router.navigate(['/']);
  }

  onSubmit(form: any) {
    if (form.invalid) return;

    this.loading = true;
    this.error = '';
    this.message = '';

    this.authService.forgotPassword(this.correo).subscribe({
      next: (res) => {
        this.message = res;
        this.loading = false;
      },
      error: (err) => {
        this.error = err;
        this.loading = false;
      }
    });
  }
}