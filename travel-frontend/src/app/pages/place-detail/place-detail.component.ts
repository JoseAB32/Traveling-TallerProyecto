import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { PlaceService } from '../../services/place/place.service';
import { Place } from '../../models/place/place';

import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { FeatureService } from '../../services/features/feature.service';
import { TranslocoModule } from '@jsverse/transloco';
import { AuthService } from '../../services/auth/auth.service';
import { FavoriteService } from '../../services/favorite/favorite.service';
import { ReviewService } from '../../services/review/review.service';
import { CreateReviewRequest, Review } from '../../models/review/review';

@Component({
  selector: 'app-place-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, FooterComponent, TranslocoModule],
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
  reviews: Review[] = [];
  reviewsLoading = false;
  reviewsPage = 0;
  reviewsSize = 10;
  reviewsHasNext = false;
  reviewsTotal = 0;
  publishingReview = false;
  reviewComment = '';
  reviewScore = 0;
  reviewError = '';
  readonly stars = [1, 2, 3, 4, 5];

  isFavorite: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private placeService: PlaceService,
    private authService: AuthService,
    private favoriteService: FavoriteService,
    private reviewService: ReviewService
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
          this.loadPlaceReviews(true);
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

  get currentUserName(): string {
    return this.authService.getCurrentUser()?.userName ?? 'Usuario';
  }

  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  get canPublishReview(): boolean {
    return this.authService.isAuthenticated() &&
      this.reviewComment.trim().length > 0 &&
      this.reviewScore >= 1 &&
      this.reviewScore <= 5 &&
      !this.publishingReview;
  }

  loadPlaceReviews(reset: boolean): void {
    if (!this.place || this.reviewsLoading) {
      return;
    }

    if (reset) {
      this.reviews = [];
      this.reviewsPage = 0;
      this.reviewsHasNext = false;
      this.reviewsTotal = 0;
    }

    this.reviewsLoading = true;

    this.reviewService.getPlaceReviews(this.place.id, this.reviewsPage, this.reviewsSize).subscribe({
      next: (response) => {
        this.reviews = [...this.reviews, ...response.content];
        this.reviewsHasNext = response.hasNext;
        this.reviewsTotal = response.totalElements;
        this.reviewsPage = response.page + 1;
        this.reviewsLoading = false;
      },
      error: () => {
        this.reviewsLoading = false;
      }
    });
  }

  selectReviewScore(score: number): void {
    this.reviewScore = score;
  }

  publishReview(): void {
    if (!this.place || !this.canPublishReview) {
      return;
    }

    const user = this.authService.getCurrentUser();
    if (!user?.id) {
      this.reviewError = 'Debes iniciar sesión para publicar una reseña.';
      return;
    }

    this.publishingReview = true;
    this.reviewError = '';

    const payload: CreateReviewRequest = {
      userId: user.id,
      placeId: this.place.id,
      parentId: null,
      comment: this.reviewComment.trim(),
      score: this.reviewScore
    };

    this.reviewService.createReview(payload).subscribe({
      next: (createdReview) => {
        this.reviews = [createdReview, ...this.reviews];
        this.reviewsTotal += 1;
        this.reviewComment = '';
        this.reviewScore = 0;
        this.publishingReview = false;
      },
      error: (error) => {
        this.reviewError = typeof error?.error?.message === 'string'
          ? error.error.message
          : 'No se pudo publicar la reseña. Intenta de nuevo.';
        this.publishingReview = false;
      }
    });
  }

  formatReviewDate(createdAt?: string): string {
    if (!createdAt) {
      return '';
    }

    const date = new Date(createdAt);
    if (Number.isNaN(date.getTime())) {
      return createdAt;
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  getReviewStars(score?: number): string {
    const normalized = Math.max(0, Math.min(5, Math.floor(score ?? 0)));
    return '★'.repeat(normalized) + '☆'.repeat(5 - normalized);
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
