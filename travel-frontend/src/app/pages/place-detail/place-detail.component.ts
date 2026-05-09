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
import { TranslocoService } from '@jsverse/transloco';
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
  currentReviewsPage = 0;
  reviewsSize = 10;
  reviewsHasNext = false;
  reviewsTotalPages = 0;
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
    private reviewService: ReviewService,
    private translocoService: TranslocoService
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
      this.reviewsPage = 0;
      this.currentReviewsPage = 0;
      this.reviewsHasNext = false;
      this.reviewsTotalPages = 0;
      this.reviewsTotal = 0;
    }

    this.reviewsLoading = true;

    this.reviewService.getPlaceReviews(this.place.id, this.reviewsPage, this.reviewsSize).subscribe({
      next: (response) => {
        this.reviews = response.content;
        this.reviewsHasNext = response.hasNext;
        this.reviewsTotalPages = response.totalPages;
        this.reviewsTotal = response.totalElements;
        this.currentReviewsPage = response.page;
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
      this.reviewError = this.translocoService.translate('placeDetail.textLoginToReview');
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
        this.reviewsTotal += 1;
        if (this.currentReviewsPage === 0) {
          this.reviews = [createdReview, ...this.reviews].slice(0, this.reviewsSize);
        }
        this.reviewComment = '';
        this.reviewScore = 0;
        this.publishingReview = false;
      },
      error: (error) => {
        this.reviewError = typeof error?.error?.message === 'string'
          ? error.error.message
          : this.translocoService.translate('placeDetail.textPublishReviewError');
        this.publishingReview = false;
      }
    });
  }

  goToReviewsPage(page: number): void {
    if (page < 0 || page === this.currentReviewsPage || this.reviewsLoading || !this.place) {
      return;
    }

    if (this.reviewsTotalPages > 0 && page >= this.reviewsTotalPages) {
      return;
    }

    this.reviewsPage = page;
    this.loadPlaceReviews(false);
  }

  get reviewsPageNumbers(): number[] {
    if (this.reviewsTotalPages <= 1) {
      return [];
    }

    return Array.from({ length: this.reviewsTotalPages }, (_, index) => index);
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
