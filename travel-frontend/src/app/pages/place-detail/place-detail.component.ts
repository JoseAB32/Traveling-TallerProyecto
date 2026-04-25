import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

import { PlaceService } from '../../services/place/place.service';
import { Place } from '../../models/place/place';

import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'app-place-detail',
  standalone: true,
  imports: [CommonModule, HeaderComponent, FooterComponent, TranslocoModule],
  templateUrl: './place-detail.component.html',
  styleUrls: ['./place-detail.component.css']
})
export class PlaceDetailComponent implements OnInit {

  place: Place | null = null;
  loading = true;
  showBackToItinerary = false;

  images: string[] = [];
  currentImageIndex = 0;

  // ❤️ FAVORITOS
  isFavorite: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private placeService: PlaceService
  ) {}

  ngOnInit(): void {
    this.showBackToItinerary = this.route.snapshot.queryParamMap.get('returnTo') === 'itinerarios';

    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.placeService.getPlaceById(Number(id)).subscribe({
        next: (data) => {
          this.place = data;

          this.images = [
            data.imageUrl,
            data.imageUrl,
            data.imageUrl
          ];

          this.loading = false;

          // 🔥 verificar si ya es favorito
          this.checkIfFavorite();
        },
        error: () => {
          this.loading = false;
        }
      });
    }
  }

  // 🔥 VERIFICAR SI YA ESTÁ EN WISHLIST
  checkIfFavorite() {
    if (!this.place) return;

    const wishList: number[] = JSON.parse(localStorage.getItem('wishList') || '[]');
    this.isFavorite = wishList.includes(this.place.id);
  }

  // ❤️ TOGGLE FAVORITO
  toggleFavorite() {
    if (!this.place) return;

    let wishList: number[] = JSON.parse(localStorage.getItem('wishList') || '[]');

    if (this.isFavorite) {
      wishList = wishList.filter(id => id !== this.place!.id);
      this.isFavorite = false;
    } else {
      wishList.push(this.place.id);
      this.isFavorite = true;
    }

    localStorage.setItem('wishList', JSON.stringify(wishList));
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

  backToItinerary(): void {
    this.router.navigate(['/itinerarios']);
  }
}
