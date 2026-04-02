import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { PlaceService } from '../place.service';
import { Place } from '../place'; 

import { HeaderComponent } from "../header/header.component";
import { FooterComponent } from "../footer/footer.component";

@Component({
  selector: 'app-inicio-logueado',
  standalone: true,
  imports: [CommonModule, HeaderComponent, FooterComponent],
  templateUrl: './inicio-logueado.component.html',
  styleUrls: ['./inicio-logueado.component.css']
})
export class InicioLogueadoComponent implements OnInit {

  featuredPlaces: Place[] = [];
  carouselPlaces: Place[] = [];
  currentIndex = 0;
  isLoading = true;

  constructor(
    private placeService: PlaceService,
    private router: Router 
  ) {}

  ngOnInit(): void {
    this.placeService.getPlacesOrdenado().subscribe({
      next: (data) => {
        console.log('DATA:', data);
        this.featuredPlaces = this.arrangeForCarousel(data); 
        this.updateCarousel();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('ERROR:', err);
        this.isLoading = false;
      }
    });
  }

  updateCarousel() {
    const len = this.featuredPlaces.length;

    if (len === 0) {
      this.carouselPlaces = [];
      return;
    }

    if (len <= 3) {
      this.carouselPlaces = [...this.featuredPlaces];
      return;
    }

    this.carouselPlaces = [
      this.featuredPlaces[(this.currentIndex - 1 + len) % len],
      this.featuredPlaces[this.currentIndex],
      this.featuredPlaces[(this.currentIndex + 1) % len]
    ];
  }

  prev(): void {
    const len = this.featuredPlaces.length;
    if (len > 0) {
      this.currentIndex = (this.currentIndex - 1 + len) % len;
      this.updateCarousel(); 
    }
  }

  next(): void {
    const len = this.featuredPlaces.length;
    if (len > 0) {
      this.currentIndex = (this.currentIndex + 1) % len;
      this.updateCarousel(); 
    }
  }

  goTo(index: number): void {
    if (this.featuredPlaces.length > 0) {
      this.currentIndex = index % this.featuredPlaces.length;
      this.updateCarousel(); 
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

  getStars(rating: number): string {
    const score = Math.floor(rating || 0);
    return '★'.repeat(score) + '☆'.repeat(5 - score);
  }
}