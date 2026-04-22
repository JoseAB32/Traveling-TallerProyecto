import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { MapComponent } from '../../components/map/map.component';

import { CityService } from '../../services/city/city.service';
import { PlaceService } from '../../services/place/place.service';
import {
  ItineraryService,
  ItineraryDraftRequest,
  ItineraryDraftResponse
} from '../../services/itinerary/itinerary.service';

import { City } from '../../models/city/city';
import { Place } from '../../models/place/place';

@Component({
  selector: 'app-create-itinerary',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, FooterComponent, MapComponent],
  templateUrl: './create-itinerary.component.html',
  styleUrl: './create-itinerary.component.css'
})
export class CreateItineraryComponent implements OnInit {
  private readonly sessionKey = 'itineraryDraftUiState';

  cities: City[] = [];
  selectedCityId: number | null = null;

  cityPlaces: Place[] = [];
  selectedPlace: Place | null = null;
  selectedPlaces: Place[] = [];

  startDate: string = '';
  endDate: string = '';

  isLoadingCities = true;
  isLoadingPlaces = false;
  isSavingDraft = false;
  hasPendingChanges = false;
  saveMessage = 'Sin cambios por guardar';

  private cityService = inject(CityService);
  private placeService = inject(PlaceService);
  private itineraryService = inject(ItineraryService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  ngOnInit(): void {
    const hasSessionState = this.restoreUiState();

    this.loadCities();
    if (!hasSessionState) {
      this.loadDraft();
    }

    this.route.queryParamMap.subscribe(params => {
      const cityIdParam = params.get('cityId');
      if (!cityIdParam) {
        return;
      }

      const cityId = Number(cityIdParam);
      if (!Number.isNaN(cityId) && cityId > 0) {
        this.selectedCityId = cityId;
        this.loadPlacesByCity();
        this.persistUiState();
      }
    });
  }

  loadCities(): void {
    this.cityService.getCities().subscribe({
      next: (cities) => {
        this.cities = cities;
        this.isLoadingCities = false;
      },
      error: () => {
        this.isLoadingCities = false;
      }
    });
  }

  loadDraft(): void {
    this.itineraryService.getMyDraft().subscribe({
      next: (draft: ItineraryDraftResponse) => {
        if (!draft) {
          return;
        }

        this.selectedPlaces = draft.places || [];
        this.startDate = draft.startDate || '';
        this.endDate = draft.endDate || '';
        this.hasPendingChanges = false;
        this.saveMessage = 'Borrador cargado';

        if (this.selectedPlaces.length > 0) {
          const cityId = this.selectedPlaces[0]?.city?.id;
          if (cityId) {
            this.selectedCityId = cityId;
            this.loadPlacesByCity();
          }
        }
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 401 || error.status === 403) {
          this.saveMessage = 'Tu sesion expiro. Vuelve a iniciar sesion.';
          return;
        }

        this.saveMessage = 'No existe borrador previo';
      }
    });
  }

  onCityChange(): void {
    this.loadPlacesByCity();
    this.selectedPlace = null;
    this.persistUiState();
  }

  loadPlacesByCity(): void {
    if (!this.selectedCityId) {
      this.cityPlaces = [];
      return;
    }

    this.isLoadingPlaces = true;
    this.placeService.getPlacesByDepartment(this.selectedCityId).subscribe({
      next: (places) => {
        this.cityPlaces = places;
        if (!this.selectedPlace || !this.cityPlaces.some(p => p.id === this.selectedPlace?.id)) {
          this.selectedPlace = null;
        }
        this.isLoadingPlaces = false;
      },
      error: () => {
        this.cityPlaces = [];
        this.selectedPlace = null;
        this.isLoadingPlaces = false;
      }
    });
  }

  onMapPlaceClick(place: Place): void {
    this.selectedPlace = place;
  }

  addSelectedPlace(): void {
    if (!this.selectedPlace) {
      return;
    }

    const alreadyExists = this.selectedPlaces.some(p => p.id === this.selectedPlace?.id);
    if (alreadyExists) {
      return;
    }

    this.selectedPlaces = [...this.selectedPlaces, this.selectedPlace];
    this.markAsDirty();
  }

  removeSelectedPlace(placeId: number): void {
    this.selectedPlaces = this.selectedPlaces.filter(p => p.id !== placeId);
    this.markAsDirty();
  }

  goToPlaceDetail(placeId: number): void {
    this.persistUiState();
    this.router.navigate(['/place', placeId], {
      queryParams: { returnTo: 'itinerarios' }
    });
  }

  onDateChange(): void {
    this.markAsDirty();
  }

  saveSelection(): void {
    if (!this.hasPendingChanges) {
      this.saveMessage = 'No hay cambios para guardar';
      return;
    }

    const payload: ItineraryDraftRequest = {
      name: 'Mi itinerario',
      startDate: this.startDate || null,
      endDate: this.endDate || null,
      placeIds: this.selectedPlaces.map(p => p.id)
    };

    this.isSavingDraft = true;
    this.itineraryService.saveDraft(payload).subscribe({
      next: () => {
        this.isSavingDraft = false;
        this.hasPendingChanges = false;
        this.saveMessage = 'Seleccion guardada en tu perfil';
        this.persistUiState();
      },
      error: (error: HttpErrorResponse) => {
        this.isSavingDraft = false;
        if (error.status === 401 || error.status === 403) {
          this.saveMessage = 'No autorizado. Inicia sesion nuevamente.';
          return;
        }

        this.saveMessage = 'No se pudo guardar la seleccion';
      }
    });
  }

  private markAsDirty(): void {
    this.hasPendingChanges = true;
    this.saveMessage = 'Tienes cambios sin guardar';
    this.persistUiState();
  }

  private persistUiState(): void {
    const snapshot = {
      selectedCityId: this.selectedCityId,
      selectedPlaces: this.selectedPlaces,
      startDate: this.startDate,
      endDate: this.endDate,
      hasPendingChanges: this.hasPendingChanges,
      saveMessage: this.saveMessage
    };

    sessionStorage.setItem(this.sessionKey, JSON.stringify(snapshot));
  }

  private restoreUiState(): boolean {
    const raw = sessionStorage.getItem(this.sessionKey);
    if (!raw) {
      return false;
    }

    try {
      const snapshot = JSON.parse(raw);
      this.selectedCityId = snapshot.selectedCityId ?? null;
      this.selectedPlaces = snapshot.selectedPlaces ?? [];
      this.startDate = snapshot.startDate ?? '';
      this.endDate = snapshot.endDate ?? '';
      this.hasPendingChanges = snapshot.hasPendingChanges ?? false;
      this.saveMessage = snapshot.saveMessage ?? 'Borrador recuperado';

      if (this.selectedCityId) {
        this.loadPlacesByCity();
      }

      return true;
    } catch {
      sessionStorage.removeItem(this.sessionKey);
      return false;
    }
  }
}
