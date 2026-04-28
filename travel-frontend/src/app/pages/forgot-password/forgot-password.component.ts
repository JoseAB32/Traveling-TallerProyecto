import { Component, OnInit } from '@angular/core';
import { RouterModule, Router } from '@angular/router'; // 👈 IMPORTANTE
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth/auth.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslocoModule
  ],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {

  correo: string = '';
  loading = false;
  message = '';
  error = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private translocoService: TranslocoService
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

  ngOnInit(): void {
    const savedLang = localStorage.getItem('lang') || 'es';
    this.translocoService.setActiveLang(savedLang);
  }
}