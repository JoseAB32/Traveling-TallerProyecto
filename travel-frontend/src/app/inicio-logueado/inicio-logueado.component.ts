import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

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

  featuredPlaces: any[] = [];
  carouselPlaces: any[] = [];
  currentIndex = 0;
  isLoading = true;

  constructor(private router: Router) {}

  ngOnInit(): void {
    // DATOS DE PRUEBA COMPLETOS (sin backend)
    this.featuredPlaces = [
      {
        id: 1,
        name: 'Lago Titicaca, La Paz',
        description: 'El Lago Titicaca es el lago navegable más alto del mundo, ubicado a más de 3,800 metros sobre el nivel del mar. Es considerado la cuna de la civilización Inca.',
        address: 'Copacabana, Manco Kapac',
        rating: 4.5,
        imageUrl: '/1.png',
        state: true
      },
      {
        id: 2,
        name: 'Parque Nacional Madidi, La Paz',
        description: 'El Parque Nacional Madidi es una de las áreas protegidas con mayor biodiversidad del planeta, ubicado en el norte de La Paz.',
        address: 'Región de Apolo y San Buenaventura',
        rating: 4.8,
        imageUrl: '/2.png',
        state: true
      },
      {
        id: 3,
        name: 'Cristo de la Concordia, Cochabamba',
        description: 'El Cristo de la Concordia es una de las estatuas de Cristo más grandes del mundo, ubicada en la cima del cerro San Pedro en Cochabamba.',
        address: 'Av. de la Concordia',
        rating: 5.0,
        imageUrl: '/3.png',
        state: true
      },
      {
        id: 4,
        name: 'Carnaval de Oruro',
        description: 'El Carnaval de Oruro es una de las festividades culturales más importantes de Bolivia y fue declarado Obra Maestra del Patrimonio Oral e Intangible de la Humanidad por la UNESCO.',
        address: 'Avenida Cívica',
        rating: 5.0,
        imageUrl: '/4.png',
        state: true,
        is_event: true,
        start_date: '2026-02-14',
        end_date: '2026-02-17'
      }
    ];
    
    console.log('✅ Datos de prueba cargados:', this.featuredPlaces.length);
    this.updateCarousel();
    this.isLoading = false;
  }

  updateCarousel() {
    this.carouselPlaces = this.featuredPlaces.slice(this.currentIndex, this.currentIndex + 3);

    if (this.carouselPlaces.length < 3 && this.featuredPlaces.length > 0) {
      const needed = 3 - this.carouselPlaces.length;
      this.carouselPlaces = [
        ...this.carouselPlaces,
        ...this.featuredPlaces.slice(0, needed)
      ];
    }
  }

  next() {
    if (this.featuredPlaces.length > 0) {
      this.currentIndex = (this.currentIndex + 1) % this.featuredPlaces.length;
      this.updateCarousel();
    }
  }

  prev() {
    if (this.featuredPlaces.length > 0) {
      this.currentIndex = (this.currentIndex - 1 + this.featuredPlaces.length) % this.featuredPlaces.length;
      this.updateCarousel();
    }
  }

  goTo(index: number) {
    this.currentIndex = index;
    this.updateCarousel();
  }

  goToDetail(id: number) {
    console.log('🔗 Enviando ID a detalle:', id);
    this.router.navigate(['/place', id]);
  }

  getStars(rating: number): string {
    const score = Math.floor(rating || 0);
    return '★'.repeat(score) + '☆'.repeat(5 - score);
  }
}