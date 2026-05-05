import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

import { PlaceService } from '../../services/place/place.service';
import { Place } from '../../models/place/place';

import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { FeatureService } from '../../services/features/feature.service';
import { TranslocoModule } from '@jsverse/transloco';
import { AuthService } from '../../services/auth/auth.service';
import { FavoriteService } from '../../services/favorite/favorite.service';

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
  showBackToDepartment = false;
  backDepartmentId: number | null = null;
  featureService = inject(FeatureService);
  images: string[] = [];
  currentImageIndex = 0;

  isFavorite: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private placeService: PlaceService,
    private authService: AuthService,
    private favoriteService: FavoriteService
  ) {}

  ngOnInit(): void {
    this.showBackToItinerary = this.route.snapshot.queryParamMap.get('returnTo') === 'itinerarios';
    this.showBackToDepartment = this.route.snapshot.queryParamMap.get('returnTo') === 'department';

    const cityIdParam = this.route.snapshot.queryParamMap.get('cityId');
    this.backDepartmentId = cityIdParam ? Number(cityIdParam) : null;

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
          this.checkIfFavorite();
        },
        error: () => {
          this.loading = false;
        }
      });
    }
  }

  checkIfFavorite(): void {
    if (!this.place || !this.authService.getCurrentUser()) {
      this.isFavorite = false;
      return;
    }

    // El token ya identifica al usuario — no se necesita userId
    this.favoriteService.getUserFavorites().subscribe({
      next: (favorites) => {
        this.isFavorite = favorites
          ? favorites.some(f => f.place?.id === this.place?.id)
          : false;
      },
      error: () => {
        this.isFavorite = false;
      }
    });
  }

  toggleFavorite(): void {
    if (!this.place || !this.authService.getCurrentUser()) return;

    if (this.isFavorite) {
      this.favoriteService.removeFavorite(this.place.id).subscribe({
        next: () => { this.isFavorite = false; },
        error: (error) => { console.error('Error al quitar favorito', error); }
      });
    } else {
      this.favoriteService.addFavorite(this.place.id).subscribe({
        next: () => { this.isFavorite = true; },
        error: (error) => { console.error('Error al agregar favorito', error); }
      });
    }
  }

  nextImage(): void {
    this.currentImageIndex = (this.currentImageIndex + 1) % this.images.length;
  }

  prevImage(): void {
    this.currentImageIndex = (this.currentImageIndex - 1 + this.images.length) % this.images.length;
  }

  getStars(rating: number): string {
    const score = Math.floor(rating || 0);
    return '★'.repeat(score) + '☆'.repeat(5 - score);
  }

  backToItinerary(): void {
    this.router.navigate(['/itinerarios']);
  }

  backToDepartment(): void {
    if (!this.backDepartmentId) return;
    this.router.navigate(['/department', this.backDepartmentId]);
  }
}