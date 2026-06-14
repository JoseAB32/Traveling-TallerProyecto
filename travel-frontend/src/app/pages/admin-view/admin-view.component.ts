import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { LoggerService } from '../../services/logger/logger.service';
import { FeatureService, Features } from '../../services/features/feature.service';
import { TranslationService } from '../../services/translation/translation.service';
import { UserService } from '../../services/user/user.service';
import { CityService } from '../../services/city/city.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Logger } from '../../models/logger/logger';
import { Translation } from '../../models/translation/translation';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-admin-view',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, FooterComponent, TranslocoModule],
  templateUrl: './admin-view.component.html',
  styleUrl: './admin-view.component.css'
})
export class AdminViewComponent implements OnInit, OnDestroy {
  private loggerService = inject(LoggerService);
  private featureService = inject(FeatureService);
  private translationService = inject(TranslationService);
  private translocoService = inject(TranslocoService);
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private cityService = inject(CityService);

  private logSub?: Subscription;
  private translationSub?: Subscription;
  private updateTranslationSub?: Subscription;

  showAdminModal = false;
  isCreatingAdmin = false;
  adminError: string | null = null;
  adminSuccess = false;
  cities: any[] = [];
  isLoadingCities = false;
  
  adminForm = {
    userName: '',
    correo:   '',
    birthday: '',
    cityId:   null as number | null
  };

  isSuperAdmin: boolean =false;
  allLogs: Logger[] = [];
  logs: Logger[] = [];
  activeTab: string = 'logs';

  logsPage: number = 0;
  logsSize: number = 10;
  logsTotalPages: number = 0;
  logsTotalElements: number = 0;
  logsHasNext: boolean = false;

  readonly featuresData = this.featureService.features;

  showErrorModal: boolean = false;
  showTranslationEditModal: boolean = false;
  modalMessage: string = '';

  modules = ['CIUDADES', 'USERS', 'PLACES', 'FAVORITES', 'REVIEWS', 'TRANSLATIONS'];
  levels = ['INFO', 'WARN', 'ERROR', 'DEBUG'];

  filterModule: string = '';
  filterLevel: string = '';
  startDate: string = '';
  endDate: string = '';

  translations: Translation[] = [];
  translationEntityTypes = ['REVIEW', 'PLACE', 'FAVORITES'];
  translationFieldNames = ['comment', 'name', 'description', 'address', 'placeType'];
  translationLanguages = ['en', 'pt', 'fr'];

  translationEntityType: string = '';
  translationLanguage: string = '';
  translationFieldName: string = '';
  translationEntityId: number | null = null;
  translationPage: number = 0;
  translationSize: number = 10;
  translationTotalElements: number = 0;
  translationTotalPages: number = 0;
  translationHasNext: boolean = false;
  translationLoading: boolean = false;

  selectedTranslation: Translation | null = null;
  editedTranslatedText: string = '';

  ngOnInit(): void {
    this.isSuperAdmin = this.authService.isSuperAdmin();

    this.activeTab = this.isSuperAdmin ? 'logs' : 'translations';

    if (this.isSuperAdmin) {
      this.setDefaultDates();
      this.loadAllLogs();
      this.loadFeatures();
    }

    if (this.activeTab === 'translations') {
      this.loadTranslations(0);
    }
  }

  changeAdminTab(tab: string): void {
    const superAdminTabs = ['logs', 'toggles'];

    if (superAdminTabs.includes(tab) && !this.isSuperAdmin) {
      this.activeTab = 'translations';

      if (this.translations.length === 0) {
        this.loadTranslations(0);
      }

      return;
    }

    this.activeTab = tab;

    if (tab === 'translations' && this.translations.length === 0) {
      this.loadTranslations(0);
    }

    if (tab === 'logs' && this.isSuperAdmin && this.allLogs.length === 0) {
      this.setDefaultDates();
      this.loadAllLogs();
    }

    if (tab === 'toggles' && this.isSuperAdmin) {
      this.loadFeatures();
    }
  }

  loadFeatures(): void {
    if (!this.isSuperAdmin) {
      return;
    }
    this.featureService.loadFeatures().subscribe({
      error: (err) => console.error('Error cargando features', err)
    });
  }

  toggleFeature(featureKey: keyof Features): void {
    if (!this.isSuperAdmin) {
      return;
    }

    const updated: Features = {
      ...this.featuresData(),
      [featureKey]: !this.featuresData()[featureKey]
    };

    this.featureService.updateFeatures(updated).subscribe({
      error: () => {
        this.modalMessage = this.translocoService.translate('adminConfiguration.modal.textErrorServer');
        this.showErrorModal = true;
      }
    });
  }

  loadAllLogs(): void {
    this.logSub = this.loggerService.getAllLogs().subscribe({
      next: (data) => {
        this.allLogs = data;
        this.logsPage = 0;
        this.updateVisibleLogs();
      },
      error: (err) => console.error('Error cargando logs', err)
    });
  }

  applyFilters(): void {
    if (!this.isSuperAdmin) {
      return;
    }
    
    if (this.filterModule && this.filterLevel && this.startDate && this.endDate) {
      this.loggerService
        .getFilteredLogs(this.filterModule, this.filterLevel, this.startDate, this.endDate)
        .subscribe({
          next: (data) => {
            this.allLogs = data;
            this.logsPage = 0;
            this.updateVisibleLogs();
          },
          error: (err) => console.error('Error filtrando logs', err)
        });
    } else {
      this.modalMessage = this.translocoService.translate('adminConfiguration.modal.textErrorFilter');
      this.showErrorModal = true;
    }
  }

  updateVisibleLogs(): void {
    const start = this.logsPage * this.logsSize;
    const end = start + this.logsSize;

    this.logs = this.allLogs.slice(start, end);
    this.logsTotalElements = this.allLogs.length;
    this.logsTotalPages = Math.ceil(this.logsTotalElements / this.logsSize);
    this.logsHasNext = this.logsPage + 1 < this.logsTotalPages;
  }

  goToLogsPage(page: number): void {
    if (page < 0 || page >= this.logsTotalPages) {
      return;
    }

    this.logsPage = page;
    this.updateVisibleLogs();
  }

  loadTranslations(page: number = this.translationPage): void {
    this.translationLoading = true;
    this.translationPage = Math.max(0, page);

    this.translationSub?.unsubscribe();
    this.translationSub = this.translationService.getTranslations({
      entityType: this.translationEntityType || undefined,
      language: this.translationLanguage || undefined,
      fieldName: this.translationFieldName || undefined,
      entityId: this.translationEntityId,
      page: this.translationPage,
      size: this.translationSize
    }).subscribe({
      next: (response) => {
        this.translations = response.content;
        this.translationPage = response.page;
        this.translationSize = response.size;
        this.translationTotalElements = response.totalElements;
        this.translationTotalPages = response.totalPages;
        this.translationHasNext = response.hasNext;
        this.translationLoading = false;
      },
      error: () => {
        this.translationLoading = false;
        this.modalMessage = this.translocoService.translate('adminConfiguration.modal.textErrorServer');
        this.showErrorModal = true;
      }
    });
  }

  applyTranslationFilters(): void {
    this.loadTranslations(0);
  }

  clearTranslationFilters(): void {
    this.translationEntityType = '';
    this.translationLanguage = '';
    this.translationFieldName = '';
    this.translationEntityId = null;
    this.translationSize = 10;
    this.loadTranslations(0);
  }

  goToTranslationPage(page: number): void {
    if (page < 0 || page >= this.translationTotalPages) {
      return;
    }

    this.loadTranslations(page);
  }

  openTranslationEditModal(translation: Translation): void {
    this.selectedTranslation = { ...translation };
    this.editedTranslatedText = translation.translatedText;
    this.showTranslationEditModal = true;
  }

  closeTranslationEditModal(): void {
    this.selectedTranslation = null;
    this.editedTranslatedText = '';
    this.showTranslationEditModal = false;
  }

  saveTranslationChanges(): void {
    if (!this.selectedTranslation) {
      return;
    }

    if (!this.editedTranslatedText || !this.editedTranslatedText.trim()) {
      this.modalMessage = this.translocoService.translate('adminConfiguration.translations.edit.emptyText');
      this.showErrorModal = true;
      return;
    }

    this.updateTranslationSub?.unsubscribe();
    this.updateTranslationSub = this.translationService.updateTranslation(this.selectedTranslation.id, {
      translatedText: this.editedTranslatedText.trim()
    }).subscribe({
      next: (updatedTranslation) => {
        this.translations = this.translations.map(translation =>
          translation.id === updatedTranslation.id ? updatedTranslation : translation
        );
        this.closeTranslationEditModal();
      },
      error: () => {
        this.modalMessage = this.translocoService.translate('adminConfiguration.modal.textErrorServer');
        this.showErrorModal = true;
      }
    });
  }

  openAdminModal(): void {
    this.adminForm = { userName: '', correo: '', birthday: '', cityId: null };
    this.adminError = null;
    this.adminSuccess = false;
    this.loadCitiesForAdmin();
    this.showAdminModal = true;
  }

  closeAdminModal(): void {
    this.showAdminModal = false;
    this.adminError = null;
    this.adminSuccess = false;
  }

  loadCitiesForAdmin(): void {
    this.isLoadingCities = true;
    this.cityService.getCities().subscribe({
      next: (data) => { this.cities = data; this.isLoadingCities = false; },
      error: ()   => { this.isLoadingCities = false; }
    });
  }

  submitCreateAdmin(): void {
    if (!this.adminForm.userName.trim()) {
      this.adminError = 'admin.usernameRequired';
      return;
    }
    if (!this.adminForm.correo.trim()) {
      this.adminError = 'admin.emailRequired';
      return;
    }
    if (!this.isValidEmail(this.adminForm.correo)) {
      this.adminError = 'admin.emailInvalid';
      return;
    }

    this.isCreatingAdmin = true;
    this.adminError = null;

    this.userService.createAdmin({
      userName: this.adminForm.userName.trim(),
      correo:   this.adminForm.correo.trim(),
      birthday: this.adminForm.birthday || undefined,
      cityId:   this.adminForm.cityId   || null
    }).subscribe({
      next: () => {
        this.isCreatingAdmin = false;
        this.adminSuccess = true;
        setTimeout(() => this.closeAdminModal(), 2000);
      },
      error: (err: HttpErrorResponse) => {
        this.isCreatingAdmin = false;
        this.adminError = err.status === 400 ? 'admin.fieldTaken' : 'admin.error';
      }
    });
  }

  private isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }
  setDefaultDates(): void {
    const now = new Date();
    const fiveDaysAgo = new Date();
    fiveDaysAgo.setDate(now.getDate() - 5);
    this.endDate = this.formatDateForInput(now);
    this.startDate = this.formatDateForInput(fiveDaysAgo);
  }

  formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  ngOnDestroy(): void {
    this.logSub?.unsubscribe();
    this.translationSub?.unsubscribe();
    this.updateTranslationSub?.unsubscribe();
  }
}