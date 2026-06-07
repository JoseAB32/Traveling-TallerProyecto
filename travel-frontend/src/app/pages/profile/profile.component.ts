import { Component, OnInit, inject, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ViewChild, ElementRef } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { User } from '../../models/user/user';
import { AuthService } from '../../services/auth/auth.service';
import { UserService } from '../../services/user/user.service';
import { CityService } from '../../services/city/city.service';
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
  
  @ViewChild('fileInputRef') fileInputRef!: ElementRef<HTMLInputElement>;

  profile: User | null = null;
  isLoading = true;
  error: string | null = null;
  isUploadingPhoto = false;
  photoError: string | null = null;
  
  menuOpen = false;

  showPasswordModal = false;
  isSavingPassword = false;
  passwordError: string | null = null;
  passwordSuccess = false;

  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  toggleCurrentPassword(): void { this.showCurrentPassword = !this.showCurrentPassword; }
  toggleNewPassword(): void { this.showNewPassword = !this.showNewPassword; }
  toggleConfirmPassword(): void { this.showConfirmPassword = !this.showConfirmPassword; }
  
  passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  showEditModal = false;
  isSavingProfile = false;
  editError: string | null = null;
  editSuccess = false;

  editForm: { userName: string; correo: string; birthday: string; cityId: number | null } = {
    userName: '', correo: '', birthday: '', cityId: null
  };

  showPhotoPanel = false;
  isDragging = false;
  previewUrl: string | null = null;
  selectedFile: File | null = null;

  cities: any[] = [];
  isLoadingCities = false;
  private userService = inject(UserService);
  private cityService = inject(CityService);
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
    this.showCurrentPassword = false;
    this.showNewPassword = false;
    this.showConfirmPassword = false;
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
    if (newPassword.includes(' ') || currentPassword.includes(' ')) {
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

  openEditProfile(): void {
    this.menuOpen = false;
    this.editError = null;
    this.editSuccess = false;
    this.editForm = {
      userName:  this.profile?.userName  || '',
      correo:    this.profile?.correo    || '',
      birthday:  this.profile?.birthday  || '',
      cityId:    this.profile?.city?.id  ?? null
    };
    this.loadCities();
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editError = null;
    this.editSuccess = false;
  }

  loadCities(): void {
    this.isLoadingCities = true;
    this.cityService.getCities().subscribe({
      next: (data) => { this.cities = data; this.isLoadingCities = false; },
      error: ()   => { this.isLoadingCities = false; }
    });
  }

  submitEditProfile(): void {
    if (!this.editForm.userName.trim() || !this.editForm.correo.trim()) {
      this.editError = 'profile.editRequiredFields';
      return;
    }

    this.isSavingProfile = true;
    this.editError = null;

    const payload: any = {
      userName: this.editForm.userName.trim(),
      correo:   this.editForm.correo.trim(),
      birthday: this.editForm.birthday || null,
      cityId:   this.editForm.cityId   || null
    };

    this.userService.updateProfile(payload).subscribe({
      next: (updated) => {
        this.profile = updated;
        this.isSavingProfile = false;
        this.editSuccess = true;
        setTimeout(() => this.closeEditModal(), 1800);
      },
      error: (err: HttpErrorResponse) => {
        this.isSavingProfile = false;
        this.editError = err.status === 400 ? 'profile.editFieldTaken' : 'profile.editError';
      }
    });
  }

  onEditProfile(): void {
    this.openEditProfile();
  }

  openPhotoPanel(): void {
    if (this.isUploadingPhoto) return;
    this.previewUrl = null;
    this.selectedFile = null;
    this.photoError = null;
    this.showPhotoPanel = true;
  }

  closePhotoPanel(): void {
    this.showPhotoPanel = false;
    this.isDragging = false;
    this.previewUrl = null;
    this.selectedFile = null;
  }

  triggerFileInput(): void {
    this.fileInputRef.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) this.showPreview(file);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
    const file = event.dataTransfer?.files?.[0];
    if (file) this.showPreview(file);
  }

  showPreview(file: File): void {
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      this.photoError = 'profile.photoTooLarge';
      return;
    }
    if (!file.type.startsWith('image/')) {
      this.photoError = 'profile.photoInvalidType';
      return;
    }
    this.selectedFile = file;
    const reader = new FileReader();
    reader.onload = (e) => { this.previewUrl = e.target?.result as string; };
    reader.readAsDataURL(file);
  }

  confirmUpload(): void {
    if (!this.selectedFile) return;
    const file = this.selectedFile;
    this.closePhotoPanel();
    this.uploadProfilePicture(file);
  }

  clearPreview(): void {
    this.previewUrl = null;
    this.selectedFile = null;
    this.fileInputRef.nativeElement.value = '';
  }

  onChangePhoto(): void {
    this.openPhotoPanel();
  }

  uploadProfilePicture(file: File): void {
    this.isUploadingPhoto = true;
    this.photoError = null;

    this.userService.updateProfilePicture(file).subscribe({
      next: (updated) => {
        this.isUploadingPhoto = false;
        if (updated.profilePictureUrl) {
          this.profile = {
            ...updated,
            profilePictureUrl: updated.profilePictureUrl + '?t=' + Date.now()
          };
        } else {
          this.profile = updated;
        }
      },
      error: (err: HttpErrorResponse) => {
        this.isUploadingPhoto = false;
        this.photoError = err.status === 400
          ? 'profile.photoInvalidType'
          : 'profile.photoError';
      }
    });
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