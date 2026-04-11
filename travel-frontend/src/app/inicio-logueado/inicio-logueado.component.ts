import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Place } from '../place';
import { PlaceService } from '../place.service';
import { HeaderComponent } from "../header/header.component";
import { FooterComponent } from "../footer/footer.component";
import { Router } from '@angular/router';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-inicio-logueado',
  standalone: true,
  imports: [CommonModule, HeaderComponent, FooterComponent, NgIf],
  templateUrl: './inicio-logueado.component.html',
  styleUrl: './inicio-logueado.component.css'
})
export class InicioLogueadoComponent implements OnInit {
  featuredPlaces: Place[] = [];
  isLoading = true;
  currentIndex = 0;

  constructor(private placeService: PlaceService, private router: Router) {}

  ngOnInit(): void {
    this.placeService.getPlacesOrdenado().subscribe({
      next: (data) => {
        this.featuredPlaces = this.arrangeForCarousel(data);
        this.currentIndex = Math.floor(this.featuredPlaces.length / 2);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error al conectar con la base de datos:', err);
        this.isLoading = false;
      }
    });
  }

  hoverDepto: string = '';
  tooltipX: number = 0;
  tooltipY: number = 0;

  onMouseMove(event: MouseEvent): void {
    this.tooltipX = event.clientX + 10;
    this.tooltipY = event.clientY + 10;
  }

  get carouselPlaces(): Place[] {
    const len = this.featuredPlaces.length;
    if (len === 0) return [];

    const center = this.currentIndex % len;
    const prev = (center - 1 + len) % len;
    const next = (center + 1) % len;

    if (len === 1) return [this.featuredPlaces[center]];
    if (len === 2) return [this.featuredPlaces[prev], this.featuredPlaces[center]];

    return [
      this.featuredPlaces[prev],
      this.featuredPlaces[center],
      this.featuredPlaces[next]
    ];
  }

  prev(): void {
    const len = this.featuredPlaces.length;
    if (len > 0) {
      this.currentIndex = (this.currentIndex - 1 + len) % len;
    }
  }

  next(): void {
    const len = this.featuredPlaces.length;
    if (len > 0) {
      this.currentIndex = (this.currentIndex + 1) % len;
    }
  }

  goTo(index: number): void {
    if (this.featuredPlaces.length > 0) {
      this.currentIndex = index % this.featuredPlaces.length;
    }
  }

  private arrangeForCarousel(places: Place[]): Place[] {
    if (!places || places.length === 0) return [];
    
    const arranged: Place[] = new Array(places.length);
    const center = Math.floor(places.length / 2);
    
    const bestPlace = places[0];
    const otherPlaces = places.slice(1);

    arranged[center] = bestPlace;

    let otherIdx = 0;
    for (let i = 0; i < arranged.length; i++) {
      if (i === center) continue;
      arranged[i] = otherPlaces[otherIdx++];
    }

    return arranged;
  }

  goToDetail(id: number) {
    this.router.navigate(['/place', id]);
  }

  getStarsArray(rating: number): boolean[] {
    const score = Math.max(0, Math.min(Math.floor(rating), 5));
    return Array(5).fill(false).map((_, i) => i < score);
  }

  goToDepartment(id: number) {
    this.router.navigate(['/department', id]);
  }
}
