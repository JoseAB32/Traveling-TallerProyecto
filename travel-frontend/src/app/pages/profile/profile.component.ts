import { Component, OnInit, inject, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { User } from '../../models/user/user';
import { AuthService } from '../../services/auth/auth.service';
import { UserService } from '../../services/user/user.service';
import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslocoModule, HeaderComponent, FooterComponent],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  profile: User | null = null;
  isLoading = true;
  error: string | null = null;

  menuOpen = false;

  showPasswordModal = false;
  isSavingPassword = false;
  passwordError: string | null = null;
  passwordSuccess = false;

  passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  private userService = inject(UserService);
  private authService = inject(AuthService);

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.gear-menu-wrap')) {
      this.menuOpen = false;
    }
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading = true;
    this.error = null;

    this.userService.getProfile().subscribe({
      next: (data: User) => {
        this.profile = data;
        this.isLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.error = 'profile.errorLoading';
        this.isLoading = false;
        console.error('Error cargando perfil', err);
      }
    });
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  openChangePassword(): void {
    this.menuOpen = false;
    this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
    this.passwordError = null;
    this.passwordSuccess = false;
    this.showPasswordModal = true;
  }

  closePasswordModal(): void {
    this.showPasswordModal = false;
    this.passwordError = null;
    this.passwordSuccess = false;
  }

  submitChangePassword(): void {
    const { currentPassword, newPassword, confirmPassword } = this.passwordForm;

    if (!currentPassword || !newPassword || !confirmPassword) {
      this.passwordError = 'profile.passwordAllRequired';
      return;
    }
    if (newPassword !== newPassword.trim() || currentPassword !== currentPassword.trim()) {
      this.passwordError = 'profile.passwordNoSpaces';
      return;
    }
    if (newPassword.length < 8) {
      this.passwordError = 'profile.passwordTooShort';
      return;
    }
    if (newPassword !== confirmPassword) {
      this.passwordError = 'profile.passwordMismatch';
      return;
    }

    this.isSavingPassword = true;
    this.passwordError = null;

    this.userService.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.isSavingPassword = false;
        this.passwordSuccess = true;
        this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
        setTimeout(() => this.closePasswordModal(), 1800);
      },
      error: (err: HttpErrorResponse) => {
        this.isSavingPassword = false;
        if (err.status === 400) {
          this.passwordError = 'profile.passwordWrong';
        } else {
          this.passwordError = 'profile.passwordError';
        }
      }
    });
  }

  onEditProfile(): void {
    // TODO: implementar edición de perfil (US pendiente)
    console.log('Editar perfil — pendiente de implementación');
  }

  onChangePhoto(): void {
    // TODO: implementar cambio de foto de perfil (US pendiente)
    console.log('Cambiar foto — pendiente de implementación');
  }

  getInitials(): string {
    if (!this.profile?.userName) return '?';
    return this.profile.userName.charAt(0).toUpperCase();
  }

  formatBirthday(birthday: string | null | undefined): string {
    if (!birthday) return '—';
    try {
      const date = new Date(birthday);
      return date.toLocaleDateString('es-BO', { year: 'numeric', month: 'long', day: 'numeric' });
    } catch {
      return birthday;
    }
  }
}