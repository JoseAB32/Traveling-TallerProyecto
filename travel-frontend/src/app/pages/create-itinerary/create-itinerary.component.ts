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

import { TranslocoModule } from '@jsverse/transloco';

import { City } from '../../models/city/city';
import { Place } from '../../models/place/place';
import { RoutingService } from '../../services/routing/routing.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-create-itinerary',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, FooterComponent, MapComponent, TranslocoModule],
  templateUrl: './create-itinerary.component.html',
  styleUrl: './create-itinerary.component.css'
})
export class CreateItineraryComponent implements OnInit {
  private readonly sessionKey = 'itineraryDraftUiState';

  limiteInferiorFecha = new Date().toISOString().split('T')[0];

  cities: City[] = [];
  selectedCityId: number | null = null;

  cityPlaces: Place[] = [];
  selectedPlace: Place | null = null;
  selectedPlaces: Place[] = [];
  generatedItinerary: Place[] = [];
  generatedRouteCoordinates: L.LatLngTuple[] = [];
  isGeneratingItinerary = false;

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
  private routingService = inject(RoutingService);

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
    this.selectedPlaces = [];
    this.generatedItinerary = [];
    this.generatedRouteCoordinates = [];
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

    const hasDifferentCity = this.selectedPlaces.some(
      place => place.city?.id !== this.selectedPlace?.city?.id
    );

    if (hasDifferentCity) {
      this.saveMessage = 'No puedes seleccionar lugares de diferentes ciudades';
      return;
    }

    this.selectedPlaces = [...this.selectedPlaces, this.selectedPlace];
    // this.markAsDirty();
    this.generatedItinerary = [];
    this.generatedRouteCoordinates = [];
    this.persistUiState();
  }

  removeSelectedPlace(placeId: number): void {
    this.selectedPlaces = this.selectedPlaces.filter(p => p.id !== placeId);
    this.markAsDirty();
    this.generatedItinerary = [];
    this.generatedRouteCoordinates = [];
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

  // saveSelection(): void {
  //   if (!this.hasPendingChanges) {
  //     this.saveMessage = 'No hay cambios para guardar';
  //     return;
  //   }

  //   const payload: ItineraryDraftRequest = {
  //     name: 'Mi itinerario',
  //     startDate: this.startDate || null,
  //     endDate: this.endDate || null,
  //     placeIds: this.selectedPlaces.map(p => p.id)
  //   };

  //   this.isSavingDraft = true;
  //   this.itineraryService.saveDraft(payload).subscribe({
  //     next: () => {
  //       this.isSavingDraft = false;
  //       this.hasPendingChanges = false;
  //       this.saveMessage = 'Seleccion guardada en tu perfil';
  //       this.persistUiState();
  //     },
  //     error: (error: HttpErrorResponse) => {
  //       this.isSavingDraft = false;
  //       if (error.status === 401 || error.status === 403) {
  //         this.saveMessage = 'No autorizado. Inicia sesion nuevamente.';
  //         return;
  //       }

  //       this.saveMessage = 'No se pudo guardar la seleccion';
  //     }
  //   });
  // }

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
      saveMessage: this.saveMessage,
      generatedItinerary: this.generatedItinerary,
      generatedRouteCoordinates: this.generatedRouteCoordinates
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
      this.generatedItinerary = snapshot.generatedItinerary ?? [];
      this.generatedRouteCoordinates = snapshot.generatedRouteCoordinates ?? [];

      if (this.selectedCityId) {
        this.loadPlacesByCity();
      }

      return true;
    } catch {
      sessionStorage.removeItem(this.sessionKey);
      return false;
    }
  }

  async generateItinerary(): Promise<void> {
    if (this.selectedPlaces.length < 2) {
      this.saveMessage = 'Selecciona al menos dos lugares para generar el itinerario';
      return;
    }

    if (!this.startDate || !this.endDate) {
      this.saveMessage = 'Selecciona fecha de inicio y fin';
      return;
    }

    if (new Date(this.startDate) > new Date(this.endDate)) {
      this.saveMessage = 'La fecha de inicio no puede ser mayor a la fecha fin';
      return;
    }

    try {
      this.isGeneratingItinerary = true;
      this.saveMessage = 'Generando itinerario con rutas reales...';

      const validPlaces = this.selectedPlaces.filter(place =>
        this.hasValidCoordinates(place) &&
        this.hasValidSchedule(place)
      );

      if (validPlaces.length !== this.selectedPlaces.length) {
        this.saveMessage = 'Algunos lugares no tienen coordenadas u horarios válidos';
        return;
      }

      const orderedItinerary = await this.orderPlacesByScheduleAndRealDistance(validPlaces);
      const routeCoordinates = await this.buildRealRouteCoordinates(orderedItinerary);

      this.generatedRouteCoordinates = routeCoordinates;
      this.generatedItinerary = orderedItinerary;

      this.saveMessage = 'Itinerario generado correctamente';
      this.persistUiState();

    } catch (error) {
      console.error('Error generando itinerario', error);
      this.saveMessage = 'No se pudo generar el itinerario con rutas reales';
    } finally {
      this.isGeneratingItinerary = false;
    }
  }

  private async orderPlacesByScheduleAndRealDistance(places: Place[]): Promise<Place[]> {
    const pendingPlaces = [...places].sort((a, b) =>
      this.getMinutesFromDate(a.start_date) - this.getMinutesFromDate(b.start_date)
    );

    const orderedPlaces: Place[] = [];

    const firstPlace = pendingPlaces.shift();

    if (!firstPlace) {
      return [];
    }

    orderedPlaces.push(firstPlace);

    while (pendingPlaces.length > 0) {
      const currentPlace = orderedPlaces[orderedPlaces.length - 1];

      let bestPlaceIndex = 0;
      let bestScore = Number.MAX_VALUE;

      for (let index = 0; index < pendingPlaces.length; index++) {
        const place = pendingPlaces[index];

        const route = await firstValueFrom(
          this.routingService.getRoute(currentPlace, place)
        );

        const distance = route.distanceKm;

        const currentClose = this.getMinutesFromDate(currentPlace.end_date);
        const nextOpen = this.getMinutesFromDate(place.start_date);

        const waitingTime = Math.max(0, nextOpen - currentClose);
        const schedulePenalty = waitingTime * 10;

        const score = distance + schedulePenalty;

        if (score < bestScore) {
          bestScore = score;
          bestPlaceIndex = index;
        }
      }

      const [bestPlace] = pendingPlaces.splice(bestPlaceIndex, 1);
      orderedPlaces.push(bestPlace);
    }

    return orderedPlaces;
  }

  private async buildRealRouteCoordinates(places: Place[]): Promise<L.LatLngTuple[]> {
    const fullRoute: L.LatLngTuple[] = [];

    for (let i = 0; i < places.length - 1; i++) {
      const route = await firstValueFrom(
        this.routingService.getRoute(places[i], places[i + 1])
      );

      fullRoute.push(...route.coordinates as L.LatLngTuple[]);
    }

    return fullRoute;
  }

  private hasValidSchedule(place: Place): boolean {
    return !!place.start_date && !!place.end_date;
  }

  private getMinutesFromDate(dateValue: string | Date | null): number {
    if (!dateValue) {
      return Number.MAX_VALUE;
    }

    if (typeof dateValue === 'string' && /^\d{2}:\d{2}/.test(dateValue)) {
      const [hours, minutes] = dateValue.split(':').map(Number);
      return hours * 60 + minutes;
    }

    const date = new Date(dateValue);
    return date.getHours() * 60 + date.getMinutes();
  }

  private hasValidCoordinates(place: Place): boolean {
    return (
      place.latitude !== null &&
      place.latitude !== undefined &&
      place.longitude !== null &&
      place.longitude !== undefined &&
      !Number.isNaN(Number(place.latitude)) &&
      !Number.isNaN(Number(place.longitude))
    );
  }
}
