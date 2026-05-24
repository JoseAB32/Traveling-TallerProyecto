import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
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
  imports: [CommonModule, TranslocoModule, HeaderComponent, FooterComponent],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  profile: User | null = null;
  isLoading = true;
  error: string | null = null;

  private userService = inject(UserService);
  private authService = inject(AuthService);

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

  formatBirthday(birthday: string | undefined): string {
    if (!birthday) return '—';
    try {
      const date = new Date(birthday);
      return date.toLocaleDateString('es-BO', { year: 'numeric', month: 'long', day: 'numeric' });
    } catch {
      return birthday;
    }
  }
}