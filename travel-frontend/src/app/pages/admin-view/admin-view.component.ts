import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { LoggerService } from '../../services/logger/logger.service';
import { FeatureService, Features } from '../../services/features/feature.service';
import { TranslationService } from '../../services/translation/translation.service';
import { UserService } from '../../services/user/user.service';
import { CityService } from '../../services/city/city.service';
import { PlaceService, CreatePlaceRequest } from '../../services/place/place.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Logger } from '../../models/logger/logger';
import { Translation } from '../../models/translation/translation';
import { Place } from '../../models/place/place';
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
  private placeService = inject(PlaceService);
  private router = inject(Router);

  private logSub?: Subscription;
  private translationSub?: Subscription;
  private updateTranslationSub?: Subscription;
  private placesSub?: Subscription;
  private createPlaceSub?: Subscription;
  private citiesSub?: Subscription;

  isCreatingAdmin = false;
  adminError: string | null = null;
  adminSuccess = false;
  cities: any[] = [];
  isLoadingCities = false;

  adminForm = {
    userName: '',
    correo: '',
    birthday: '',
    cityId: null as number | null
  };

  isSuperAdmin: boolean = false;
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

  placeTypes: string[] = [
    'Museo',
    'Parque',
    'Sitio histórico',
    'Mirador',
    'Área natural',
    'Centro cultural',
    'Restaurante',
    'Evento'
  ];

  existingPlaces: Place[] = [];

  isCreatingPlace = false;
  createPlaceError: string | null = null;
  createPlaceSuccess = false;
  selectedPlaceImages: File[] = [];

  placeForm = {
    name: '',
    description: '',
    address: '',
    price: 0,
    latitude: 0,
    longitude: 0,
    place_type: '',
    city_id: null as number | null,
    is_event: false,
    start_date: null as string | null,
    end_date: null as string | null
  };

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
    const superAdminTabs = ['logs', 'toggles', 'createAdmin'];

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

    if (tab === 'createAdmin' && this.isSuperAdmin) {
      this.resetAdminForm();
      this.loadCitiesForAdmin();
    }

    if (tab === 'createPlace') {
      this.resetCreatePlaceMessages();
      this.loadCitiesForAdmin();
      this.loadExistingPlaces();
    }
  }

  resetAdminForm(): void {
    this.adminForm = {
      userName: '',
      correo: '',
      birthday: '',
      cityId: null
    };

    this.adminError = null;
    this.adminSuccess = false;
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

  loadCitiesForAdmin(): void {
    if (this.cities.length > 0) {
      return;
    }

    this.isLoadingCities = true;

    this.citiesSub?.unsubscribe();
    this.citiesSub = this.cityService.getCities().subscribe({
      next: (data) => {
        this.cities = data;
        this.isLoadingCities = false;
      },
      error: () => {
        this.isLoadingCities = false;
      }
    });
  }

  submitCreateAdmin(): void {
    if (!this.adminForm.userName.trim()) {
      this.adminError = 'adminConfiguration.adminRegister.usernameRequired';
      return;
    }

    if (!this.adminForm.correo.trim()) {
      this.adminError = 'adminConfiguration.adminRegister.emailRequired';
      return;
    }

    if (!this.isValidEmail(this.adminForm.correo)) {
      this.adminError = 'adminConfiguration.adminRegister.emailInvalid';
      return;
    }

    this.isCreatingAdmin = true;
    this.adminError = null;

    this.userService.createAdmin({
      userName: this.adminForm.userName.trim(),
      correo: this.adminForm.correo.trim(),
      birthday: this.adminForm.birthday || undefined,
      cityId: this.adminForm.cityId || null
    }).subscribe({
      next: () => {
        this.isCreatingAdmin = false;
        this.adminSuccess = true;
        setTimeout(() => this.resetAdminForm(), 2000);
      },
      error: (err: HttpErrorResponse) => {
        this.isCreatingAdmin = false;
        this.adminError = err.status === 400
          ? 'adminConfiguration.adminRegister.fieldTaken'
          : 'adminConfiguration.adminRegister.error';
      }
    });
  }

  loadExistingPlaces(): void {
    this.placesSub?.unsubscribe();

    this.placesSub = this.placeService.getSearchCache().subscribe({
      next: (places) => {
        this.existingPlaces = places;
      },
      error: () => {
        this.createPlaceError = 'No se pudieron cargar los lugares existentes para validar duplicados.';
      }
    });
  }
    onPlaceImagesSelected(event: Event): void {
    this.createPlaceError = null;

    const input = event.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];

    if (files.length === 0) {
      this.selectedPlaceImages = [];
      return;
    }

    if (files.length > 5) {
      this.selectedPlaceImages = [];
      this.createPlaceError = 'Solo se permite subir hasta 5 imágenes por lugar turístico.';
      input.value = '';
      return;
    }

    const maxSize = 5 * 1024 * 1024;

    for (const file of files) {
      if (!file.type.startsWith('image/')) {
        this.selectedPlaceImages = [];
        this.createPlaceError = 'Todos los archivos seleccionados deben ser imágenes.';
        input.value = '';
        return;
      }

      if (file.size > maxSize) {
        this.selectedPlaceImages = [];
        this.createPlaceError = 'Cada imagen no debe superar los 5 MB.';
        input.value = '';
        return;
      }
    }

    this.selectedPlaceImages = files;
  }

  submitCreatePlace(): void {
    this.createPlaceError = null;
    this.createPlaceSuccess = false;

    this.normalizePlaceForm();

    const validationError = this.validatePlaceForm();

    if (validationError) {
      this.createPlaceError = validationError;
      return;
    }

    if (this.isRepeatedPlace()) {
      this.createPlaceError = 'Ya existe un lugar turístico activo con ese nombre en la ciudad seleccionada.';
      return;
    }

    const payload: CreatePlaceRequest = {
      name: this.placeForm.name,
      description: this.placeForm.description,
      address: this.placeForm.address,
      price: Number(this.placeForm.price),
      latitude: Number(this.placeForm.latitude),
      longitude: Number(this.placeForm.longitude),
      place_type: this.placeForm.place_type,
      city_id: Number(this.placeForm.city_id),
      is_event: Boolean(this.placeForm.is_event),
      start_date: this.placeForm.is_event ? this.placeForm.start_date : null,
      end_date: this.placeForm.is_event ? this.placeForm.end_date : null,
      images: this.selectedPlaceImages
  };

    this.isCreatingPlace = true;

    this.createPlaceSub?.unsubscribe();
    this.createPlaceSub = this.placeService.createPlace(payload).subscribe({
      next: (createdPlace) => {
        this.isCreatingPlace = false;
        this.createPlaceSuccess = true;
        this.resetPlaceForm();
        this.router.navigate(['/place', createdPlace.id]);
      },
      error: (error) => {
        this.isCreatingPlace = false;
        this.createPlaceError = error?.error?.message || error?.message || 'No se pudo crear el lugar turístico.';
      }
    });
  }

    resetPlaceForm(): void {
      this.createPlaceError = null;
      this.createPlaceSuccess = false;
      this.selectedPlaceImages = [];

      this.placeForm = {
        name: '',
        description: '',
        address: '',
        price: 0,
        latitude: 0,
        longitude: 0,
        place_type: '',
        city_id: null,
        is_event: false,
        start_date: null,
        end_date: null
      };
   }

  private resetCreatePlaceMessages(): void {
    this.createPlaceError = null;
    this.createPlaceSuccess = false;
  }

  private validatePlaceForm(): string {
    if (!this.placeForm.name || this.placeForm.name.length < 3) {
      return 'El nombre debe tener al menos 3 caracteres.';
    }

    if (!this.placeForm.description || this.placeForm.description.length < 10) {
      return 'La descripción debe tener al menos 10 caracteres.';
    }

    if (!this.placeForm.address || this.placeForm.address.length < 5) {
      return 'La dirección debe tener al menos 5 caracteres.';
    }

    if (!this.placeForm.place_type || this.placeForm.place_type.length < 3) {
      return 'Debe seleccionar un tipo de lugar válido.';
    }

    if (!this.placeForm.city_id) {
      return 'Debe seleccionar una ciudad.';
    }

    if (this.placeForm.price === null || this.placeForm.price === undefined || Number(this.placeForm.price) < 0) {
      return 'El precio debe ser mayor o igual a 0.';
    }

    if (
      this.placeForm.latitude === null ||
      this.placeForm.latitude === undefined ||
      Number(this.placeForm.latitude) < -90 ||
      Number(this.placeForm.latitude) > 90
    ) {
      return 'La latitud debe estar entre -90 y 90.';
    }

    if (
      this.placeForm.longitude === null ||
      this.placeForm.longitude === undefined ||
      Number(this.placeForm.longitude) < -180 ||
      Number(this.placeForm.longitude) > 180
    ) {
      return 'La longitud debe estar entre -180 y 180.';
    }

    if (this.placeForm.is_event) {
      if (!this.placeForm.start_date || !this.placeForm.end_date) {
        return 'Los eventos deben tener fecha de inicio y fecha de fin.';
      }

      if (new Date(this.placeForm.end_date).getTime() <= new Date(this.placeForm.start_date).getTime()) {
        return 'La fecha de fin debe ser posterior a la fecha de inicio.';
      }
    }

    return '';
  }

  private isRepeatedPlace(): boolean {
    const name = this.normalizeForComparison(this.placeForm.name);
    const cityId = Number(this.placeForm.city_id);

    return this.existingPlaces.some(place =>
      place.state &&
      this.normalizeForComparison(place.name) === name &&
      Number(place.city?.id) === cityId
    );
  }

  private normalizePlaceForm(): void {
    this.placeForm.name = this.normalizeSpaces(this.placeForm.name);
    this.placeForm.description = this.normalizeSpaces(this.placeForm.description);
    this.placeForm.address = this.normalizeSpaces(this.placeForm.address);
    this.placeForm.place_type = this.normalizeSpaces(this.placeForm.place_type);
  }

  private normalizeSpaces(value: string): string {
    return (value || '').trim().replace(/\s+/g, ' ');
  }

  private normalizeForComparison(value: string | undefined | null): string {
    return this.normalizeSpaces(value || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
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
    this.placesSub?.unsubscribe();
    this.createPlaceSub?.unsubscribe();
    this.citiesSub?.unsubscribe();
  }
}