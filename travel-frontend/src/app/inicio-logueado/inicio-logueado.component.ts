import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { PlaceService } from '../place.service';
import { Place } from '../place';

import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';

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
        this.featuredPlaces = data;
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
    this.carouselPlaces = this.featuredPlaces.slice(this.currentIndex, this.currentIndex + 3);

    if (this.carouselPlaces.length < 3) {
      this.carouselPlaces = [
        ...this.carouselPlaces,
        ...this.featuredPlaces.slice(0, 3 - this.carouselPlaces.length)
      ];
    }
  }

  next() {
    this.currentIndex = (this.currentIndex + 1) % this.featuredPlaces.length;
    this.updateCarousel();
  }

  prev() {
    this.currentIndex =
      (this.currentIndex - 1 + this.featuredPlaces.length) % this.featuredPlaces.length;
    this.updateCarousel();
  }

  goTo(index: number) {
    this.currentIndex = index;
    this.updateCarousel();
  }

  // 🔥 NAVEGACIÓN
  goToDetail(id: number) {
    this.router.navigate(['/place', id]);
  }

  getStars(rating: number): string {
    const score = Math.floor(rating || 0);
    return '★'.repeat(score) + '☆'.repeat(5 - score);
  }
}