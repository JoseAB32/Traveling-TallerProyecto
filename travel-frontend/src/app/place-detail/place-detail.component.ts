import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

import { PlaceService } from '../place.service';
import { Place } from '../place';

import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-place-detail',
  standalone: true,
  imports: [CommonModule, HeaderComponent, FooterComponent],
  templateUrl: './place-detail.component.html',
  styleUrls: ['./place-detail.component.css']
})
export class PlaceDetailComponent implements OnInit {

  place: Place | null = null;
  loading = true;

  images: string[] = [];
  currentImageIndex = 0;

  constructor(
    private route: ActivatedRoute,
    private placeService: PlaceService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.placeService.getPlaceById(Number(id)).subscribe({
        next: (data) => {
          this.place = data;

          // 🔥 USAMOS LA MISMA IMAGEN 3 VECES PARA EL CARRUSEL
          this.images = [
            data.image_url,
            data.image_url,
            data.image_url
          ];

          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
    }
  }

  nextImage() {
    this.currentImageIndex =
      (this.currentImageIndex + 1) % this.images.length;
  }

  prevImage() {
    this.currentImageIndex =
      (this.currentImageIndex - 1 + this.images.length) % this.images.length;
  }

  getStars(rating: number): string {
    const score = Math.floor(rating || 0);
    return '★'.repeat(score) + '☆'.repeat(5 - score);
  }
}