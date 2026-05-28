import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { Subscription } from 'rxjs';
import { CONSTANTS } from '../../utils/constants';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslocoModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  sessionExpiredMessage = '';

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
    private router: Router,
    private translocoService: TranslocoService,
    private route: ActivatedRoute
  ) {}

  onSubmit(form: NgForm): void {
    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    this.credentials.userName = this.credentials.userName.trim();
    this.credentials.password = this.credentials.password.trim();

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.subscription = this.authService.login(
      this.credentials.userName,
      this.credentials.password
    ).subscribe({
      next: () => {
        this.successMessage = CONSTANTS.MESSAGES.SUCCESS.LOGIN;
        this.loading = false;
        this.router.navigate(['/InicioLogueado']);
      },
      error: (errorMessage: string) => {
        this.error = errorMessage;
        this.loading = false;
      }
    });
  }

  goToLanding() {
    this.router.navigate(['']);
  }

  ngOnInit(): void {
    const sessionExpired = this.route.snapshot.queryParamMap.get('sessionExpired');

    if (sessionExpired === 'true') {
      this.sessionExpiredMessage = 'Tu sesión expiró. Por favor, vuelve a iniciar sesión.';
    }

    const savedLang = localStorage.getItem('lang') || 'es';
    this.translocoService.setActiveLang(savedLang);
  }

  // ngOnDestroy(): void {
  //   this.subscription?.unsubscribe();
  // }
}