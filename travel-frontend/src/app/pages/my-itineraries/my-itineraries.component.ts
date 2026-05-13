import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { Router } from '@angular/router';

import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { MapComponent } from '../../components/map/map.component';

import {
  ItineraryService,
  ItineraryDraftResponse
} from '../../services/itinerary/itinerary.service';

import { RoutingService } from '../../services/routing/routing.service';

import { firstValueFrom } from 'rxjs';

import { Place } from '../../models/place/place';

@Component({
  selector: 'app-my-itineraries',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    TranslocoModule,
    HeaderComponent,
    FooterComponent,
    MapComponent
  ],
  templateUrl: './my-itineraries.component.html',
  styleUrls: ['./my-itineraries.component.css']
})
export class MyItinerariesComponent implements OnInit {

  itineraries: ItineraryDraftResponse[] = [];

  loading = true;

  expandedCards: number[] = [];

  private itineraryService = inject(ItineraryService);

  private routingService = inject(RoutingService);

  constructor(private router: Router) {

  }

  ngOnInit(): void {
    this.loadItineraries();
  }

  async loadItineraries(): Promise<void> {

    this.loading = true;

    this.itineraryService.getMyItineraries().subscribe({

      next: async (data) => {

        this.itineraries = data;

        await this.generateAllRoutes();

        this.loading = false;
      },

      error: (err) => {
        console.error(err);
        this.loading = false;
      }

    });

  }

  async generateAllRoutes(): Promise<void> {

    for (const itinerary of this.itineraries) {

      itinerary.routeCoordinates =
        await this.buildRealRouteCoordinates(
          itinerary.places
        );

    }

  }

  private async buildRealRouteCoordinates(
    places: Place[]
  ): Promise<L.LatLngTuple[]> {

    const fullRoute: L.LatLngTuple[] = [];

    if (places.length < 2) {
      return fullRoute;
    }

    for (let i = 0; i < places.length - 1; i++) {

      try {

        const route = await firstValueFrom(
          this.routingService.getRoute(
            places[i],
            places[i + 1]
          )
        );

        fullRoute.push(
          ...(route.coordinates as L.LatLngTuple[])
        );

      } catch (error) {
        console.error('Error generating route', error);
      }

    }

    return fullRoute;
  }

  getCityName(itinerary: ItineraryDraftResponse): string {

    if (
      itinerary.places &&
      itinerary.places.length > 0 &&
      itinerary.places[0].city &&
      itinerary.places[0].city.name
    ) {
      return itinerary.places[0].city.name;
    }

    return 'Ciudad desconocida';
  }

  formatDate(date: string | null): string {

    if (!date) {
      return '--/--/----';
    }

    const parsedDate = new Date(date);

    return parsedDate.toLocaleDateString('es-BO');
  }

  toggleCard(index: number): void {

    if (this.expandedCards.includes(index)) {

      this.expandedCards =
        this.expandedCards.filter(i => i !== index);

    } else {

      this.expandedCards.push(index);

    }

  }

  isExpanded(index: number): boolean {
    return this.expandedCards.includes(index);
  }

  goToModifyItinerary(id: number) {
    this.router.navigate(['/modify-itinerario', id]);
    // console.log(id);
  }

}