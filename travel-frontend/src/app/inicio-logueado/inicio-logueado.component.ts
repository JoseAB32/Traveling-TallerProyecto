import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { PlaceService } from '../place.service';

// 🔥 IMPORTA ESTOS
import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-inicio-logueado',
  standalone: true, // 🔥 CLAVE
  imports: [
    CommonModule,   // 🔥 ngIf, ngFor, ngClass
    RouterModule,   // 🔥 routerLink
    HeaderComponent,
    FooterComponent
  ],
  templateUrl: './inicio-logueado.component.html',
  styleUrls: ['./inicio-logueado.component.css']
})
export class InicioLogueadoComponent implements OnInit {

  featuredPlaces: any[] = [];
  isLoading = true;
  currentIndex = 0;

  constructor(private placeService: PlaceService) {}

  ngOnInit(): void {
    this.placeService.getPlacesOrdenado().subscribe(data => {
      this.featuredPlaces = data;
      this.isLoading = false;
    });
  }

  get carouselPlaces(): any[] {
    const len = this.featuredPlaces.length;
    if (len === 0) return [];

    const prev = (this.currentIndex - 1 + len) % len;
    const next = (this.currentIndex + 1) % len;

    return [
      this.featuredPlaces[prev],
      this.featuredPlaces[this.currentIndex],
      this.featuredPlaces[next]
    ];
  }

  prev() {
    this.currentIndex =
      (this.currentIndex - 1 + this.featuredPlaces.length) % this.featuredPlaces.length;
  }

  next() {
    this.currentIndex =
      (this.currentIndex + 1) % this.featuredPlaces.length;
  }

  goTo(index: number) {
    this.currentIndex = index;
  }

  getStars(rating: number): string {
    const score = Math.floor(rating || 0);
    return '★'.repeat(score) + '☆'.repeat(5 - score);
  }
}