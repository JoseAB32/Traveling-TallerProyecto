import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { MapComponent } from '../../components/map/map.component';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom, Subscription } from 'rxjs';

import { Place } from '../../models/place/place';
import { ItineraryService, ItineraryDraftRequest, ItineraryDraftResponse } from '../../services/itinerary/itinerary.service';
import { PlaceService } from '../../services/place/place.service';
import { RoutingService } from '../../services/routing/routing.service';

@Component({
  selector: 'app-modify-itinerary',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, FooterComponent, MapComponent, TranslocoModule],
  templateUrl: './modify-itinerary.component.html',
  styleUrl: './modify-itinerary.component.css'
})
export class ModifyItineraryComponent implements OnInit, OnDestroy {
  private readonly sessionKey = 'UpdateitineraryDraftUiState';
  private routerSubscription?: Subscription;
  
    limiteInferiorFecha = new Date().toISOString().split('T')[0];

    selectedCityId: number | null = null;

    selectedPlace: Place | null = null;
    
    generatedRouteCoordinates: L.LatLngTuple[] = [];
    isGeneratingItinerary = false;
  
    isLoadingCities = true;
    isLoadingPlaces = false;
    isSavingDraft = false;
    hasPendingChanges = false;
    saveMessage = 'Sin cambios por guardar';

    itineraryNameError = '';

    tripId: number = 0;
    itinerary: ItineraryDraftResponse | null = null;
    isLoading = false;
    errorMessage = '';
    cityId: number | null = null;
    cityPlaces: Place[] = [];
    startDate: string = '';
    endDate: string = '';
    selectedPlaces: Place[] = [];
    generatedItinerary: Place[] = [];
    nameItinerary: string = "";
  
    private placeService = inject(PlaceService);
    private itineraryService = inject(ItineraryService);
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private routingService = inject(RoutingService);
    private translocoService = inject(TranslocoService);
  
    ngOnInit(): void {  
      const idParam = this.route.snapshot.paramMap.get('id');

      if (!idParam) {
        console.error('No se encontró el id en la URL');
        return;
      }

      this.tripId = Number(idParam);
      
      this.listenRouteChanges();

      const wasRestored = this.restoreUiState();

      if (!wasRestored) {
        this.loadItineraryById();
      }
    }

    ngOnDestroy(): void {
      this.routerSubscription?.unsubscribe();
    }

    private loadItineraryById(): void {
      if (Number.isNaN(this.tripId) || this.tripId <= 0) {
        this.errorMessage = 'ID de itinerario inválido';
        return;
      }

      this.isLoading = true;
      this.errorMessage = '';

      this.itineraryService.getItineraryById(this.tripId).subscribe({
        next: async (response: ItineraryDraftResponse) => {
          this.itinerary = response;
          this.nameItinerary = response.name;
          this.selectedPlaces = response.places || [];
          this.startDate = response.startDate || '';
          this.endDate = response.endDate || '';
          this.generatedItinerary = response.places || [];
          const cityId = response.places?.[0]?.city?.id;

          if (cityId !== undefined && cityId !== null) {
            this.cityId = cityId;
            this.loadPlacesByCity();
          }

          if (this.generatedItinerary.length >= 2) {
            this.generatedRouteCoordinates = await this.buildRealRouteCoordinates(this.generatedItinerary);
          }

          this.isLoading = false;
        },
        error: (error: HttpErrorResponse) => {
          this.isLoading = false;

          if (error.status === 401 || error.status === 403) {
            this.errorMessage = 'No autorizado. Inicia sesión nuevamente.';
            return;
          }

          if (error.status === 404) {
            this.errorMessage = 'No se encontró el itinerario solicitado.';
            return;
          }

          this.errorMessage = 'No se pudo recuperar el itinerario.';
          console.error('Error al recuperar itinerario:', error);
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
  
    loadPlacesByCity(): void {
      if (this.cityId === null) {
        console.warn('No se puede cargar lugares porque cityId es null');
        return;
      }

      this.isLoadingPlaces = true;
      this.placeService.getPlacesByDepartment(this.cityId).subscribe({
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
      this.generatedItinerary = [];
      this.generatedRouteCoordinates = [];
      this.persistUiState();
    }
  
    removeSelectedPlace(placeId: number): void {
      this.selectedPlaces = this.selectedPlaces.filter(p => p.id !== placeId);
      this.generatedItinerary = [];
      this.generatedRouteCoordinates = [];
      this.persistUiState();
    }
  
    goToPlaceDetail(placeId: number): void {
      this.persistUiState();
      this.router.navigate(['/place', placeId], {
        queryParams: {
          returnTo: 'modify-itinerary',
          itineraryId: this.tripId
        }
      });
    }
  
    onDateChange(): void {
      this.persistUiState();
  
    }

    onNameChange(): void {
      this.persistUiState();
    }
  
    private persistUiState(): void {
      const snapshot = {
        name: this.nameItinerary,
        cityId: this.cityId,
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
        this.nameItinerary = snapshot.name ?? '';
        this.cityId = snapshot.cityId ?? null;
        this.selectedPlaces = snapshot.selectedPlaces ?? [];
        this.startDate = snapshot.startDate ?? '';
        this.endDate = snapshot.endDate ?? '';
        this.hasPendingChanges = snapshot.hasPendingChanges ?? false;
        this.saveMessage = snapshot.saveMessage ?? 'Borrador recuperado';
        this.generatedItinerary = snapshot.generatedItinerary ?? [];
        this.generatedRouteCoordinates = snapshot.generatedRouteCoordinates ?? [];
  
        if (this.cityId) {
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

  
    saveItinerary(): void {
      if (Number.isNaN(this.tripId) || this.tripId <= 0) {
        this.saveMessage = 'ID de itinerario inválido';
        return; 
      }

      if (!this.nameItinerary || this.nameItinerary.trim() === '') {
        this.itineraryNameError = this.translocoService.translate('createItinerary.textErrorRequiredName');
        return;
      }

      const placesToSave = this.generatedItinerary.length > 0
        ? this.generatedItinerary
        : this.selectedPlaces;

      if (placesToSave.length === 0) {
        this.saveMessage = 'Debes seleccionar al menos un lugar';
        return;
      }

      const payload: ItineraryDraftRequest = {
        name: this.nameItinerary.trim(),
        startDate: this.startDate || null,
        endDate: this.endDate || null,
        placeIds: placesToSave.map(place => place.id)
      };

      this.isSavingDraft = true;

      this.itineraryService.updateItinerary(this.tripId, payload).subscribe({
        next: () => {
          this.isSavingDraft = false;

          sessionStorage.removeItem(this.sessionKey);
          sessionStorage.setItem('justSaved', 'true');

          this.clearScreenAfterSave();
          this.router.navigate(['/my-itineraries'])
        },
        error: (error: HttpErrorResponse) => {
          this.isSavingDraft = false;

          if (error.status === 401 || error.status === 403) {
            this.saveMessage = 'No autorizado. Inicia sesión nuevamente.';
            return;
          }

          if (error.status === 404) {
            this.saveMessage = 'No se encontró el itinerario a modificar.';
            return;
          }

          this.saveMessage = 'No se pudo actualizar el itinerario';
          console.error('Error al actualizar itinerario:', error);
        }
      });
    }

    private clearScreenAfterSave(): void {
      this.itinerary = null;
      this.nameItinerary = '';
      this.itineraryNameError = '';

      this.cityId = null;
      this.selectedCityId = null;
      this.cityPlaces = [];

      this.selectedPlace = null;
      this.selectedPlaces = [];

      this.generatedItinerary = [];
      this.generatedRouteCoordinates = [];

      this.startDate = '';
      this.endDate = '';

      this.hasPendingChanges = false;
      this.isGeneratingItinerary = false;
      this.isLoadingPlaces = false;

      this.saveMessage = 'Itinerario actualizado correctamente';
    }

    private listenRouteChanges(): void {
      this.routerSubscription = this.router.events.subscribe((event) => {
        if (!(event instanceof NavigationStart)) {
          return;
        }

        const isGoingToPlaceDetailFromModifyItinerary =
          event.url.startsWith('/place/') &&
          event.url.includes('returnTo=modify-itinerary') &&
          event.url.includes(`itineraryId=${this.tripId}`);

        if (!isGoingToPlaceDetailFromModifyItinerary) {
          sessionStorage.removeItem(this.sessionKey);
        }
      });
    }
}
