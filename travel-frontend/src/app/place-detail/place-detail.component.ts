import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';

import { PlaceService } from '../place.service';
import { Place } from '../place';

// Componentes
import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-place-detail',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    FooterComponent
  ],
  templateUrl: './place-detail.component.html',
  styleUrls: ['./place-detail.component.css']
})
export class PlaceDetailComponent implements OnInit {

  place: Place | null = null;
  loading: boolean = true;

  currentImageIndex: number = 0;
  images: string[] = [];

  constructor(
    private route: ActivatedRoute,
    private placeService: PlaceService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = Number(idParam);

    // 🔥 Validación extra (pro)
    if (!id || isNaN(id)) {
      console.error('ID inválido:', idParam);
      this.loading = false;
      return;
    }

    this.placeService.getPlaceById(id).subscribe({
      next: (data: Place | undefined) => {

        if (!data) {
          console.warn('Lugar no encontrado');
          this.loading = false;
          return;
        }

        this.place = data;

        // 🔥 Preparar imágenes (escala a futuro)
        this.images = data.image_url
          ? [data.image_url]
          : [];

        this.loading = false;
      },

      error: (err) => {
        console.error('Error al obtener el lugar:', err);
        this.loading = false;
      }
    });
  }

  nextImage(): void {
    if (this.images.length === 0) return;

    this.currentImageIndex =
      (this.currentImageIndex + 1) % this.images.length;
  }

  prevImage(): void {
    if (this.images.length === 0) return;

    this.currentImageIndex =
      (this.currentImageIndex - 1 + this.images.length) % this.images.length;
  }

  getStars(rating: number): string {
    const score = Math.floor(rating || 0);
    return '★'.repeat(score) + '☆'.repeat(5 - score);
  }
}