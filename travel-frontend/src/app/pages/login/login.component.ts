import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { Subscription } from 'rxjs';
import { CONSTANTS } from '../../utils/constants';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslocoModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
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
  }

  // ngOnDestroy(): void {
  //   this.subscription?.unsubscribe();
  // }
}