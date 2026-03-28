import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';

import { PlaceService } from '../place.service';
import { Place } from '../place';

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

    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.placeService.getPlaceById(id).subscribe({

      next: (data: Place | undefined) => {

        if (data) {

          this.place = data;

          /* 🔥 IMÁGENES DEL CARRUSEL SEGÚN EL LUGAR */
          switch (data.id) {

            case 1:
              this.images = [
                'asset_10.png',
                'asset_13.png',
                'asset_14.png'
              ];
              break;

            case 2:
              this.images = [
                'asset_11.png',
                'asset_15.png',
                'asset_16.png'
              ];
              break;

            case 3:
              this.images = [
                'asset_1.png',
                'asset_12.png',
                'asset_17.png'
              ];
              break;

            case 4:
              this.images = [
                'asset_18.png',
                'asset_19.png',
                'asset_20.png'
              ];
              break;

            default:
              this.images = [data.image_url];
          }

        }

        this.loading = false;
      },

      error: (err) => {
        console.error(err);
        this.loading = false;
      }

    });

  }

  nextImage(): void {

    if (this.images.length > 0) {
      this.currentImageIndex =
        (this.currentImageIndex + 1) % this.images.length;
    }

  }

  prevImage(): void {

    if (this.images.length > 0) {
      this.currentImageIndex =
        (this.currentImageIndex - 1 + this.images.length) % this.images.length;
    }

  }

  getStars(rating: number): string {

    const score = Math.floor(rating || 0);
    return '★'.repeat(score) + '☆'.repeat(5 - score);

  }

}