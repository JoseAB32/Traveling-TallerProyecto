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

interface ReviewRepliesState {
  replies: Review[];
  page: number;
  hasNext: boolean;
  totalElements: number;
  totalPages: number;
  loading: boolean;
  expanded: boolean;
  composerOpen: boolean;
  replyComment: string;
  publishing: boolean;
  error: string;
}

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
  showBackToModifyItinerary = false;
  backDepartmentId: number | null = null;
  backItineraryId: number | null = null;
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
  readonly repliesPreviewSize = 2;
  repliesStateMap: Record<number, ReviewRepliesState> = {};

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
    this.showBackToModifyItinerary = this.route.snapshot.queryParamMap.get('returnTo') === 'modify-itinerary';

    const itineraryIdParam = this.route.snapshot.queryParamMap.get('itineraryId');
    this.backItineraryId = itineraryIdParam ? Number(itineraryIdParam) : null;

    const cityIdParam = this.route.snapshot.queryParamMap.get('cityId');
    this.backDepartmentId = cityIdParam ? Number(cityIdParam) : null;

    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.placeService.getPlaceById(Number(id)).subscribe({
        next: (data) => {
          this.place = data;

          this.images = this.buildCarouselImages(data);
          this.currentImageIndex = 0;

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

  private buildCarouselImages(place: Place): string[] {
    const placeImages = place.images
      ?.map(image => image.image_url)
      .filter((url): url is string => !!url && url.trim().length > 0) ?? [];

    if (placeImages.length > 0) {
      return placeImages;
    }

    return ['assets/images/default-place.jpg'];
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
        this.initializeRepliesState(this.reviews);
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

  initializeRepliesState(reviews: Review[]): void {
    const nextState: Record<number, ReviewRepliesState> = {};

    reviews.forEach((review) => {
      const previous = this.repliesStateMap[review.id];
      nextState[review.id] = previous ?? this.createInitialRepliesState();
    });

    this.repliesStateMap = nextState;
    reviews.forEach((review) => this.loadReplies(review.id, true));
  }

  createInitialRepliesState(): ReviewRepliesState {
    return {
      replies: [],
      page: 0,
      hasNext: false,
      totalElements: 0,
      totalPages: 0,
      loading: false,
      expanded: false,
      composerOpen: false,
      replyComment: '',
      publishing: false,
      error: ''
    };
  }

  getRepliesState(reviewId: number): ReviewRepliesState {
    if (!this.repliesStateMap[reviewId]) {
      this.repliesStateMap[reviewId] = this.createInitialRepliesState();
    }

    return this.repliesStateMap[reviewId];
  }

  loadReplies(reviewId: number, reset: boolean): void {
    const state = this.getRepliesState(reviewId);
    if (state.loading) {
      return;
    }

    if (reset) {
      state.replies = [];
      state.page = 0;
      state.hasNext = false;
      state.totalElements = 0;
      state.totalPages = 0;
    }

    state.loading = true;

    this.reviewService.getReviewReplies(reviewId, state.page, this.repliesPreviewSize).subscribe({
      next: (response) => {
        state.replies = reset ? response.content : [...state.replies, ...response.content];
        state.page = response.page + 1;
        state.hasNext = response.hasNext;
        state.totalElements = response.totalElements;
        state.totalPages = response.totalPages;
        state.loading = false;
      },
      error: () => {
        state.loading = false;
      }
    });
  }

  toggleReplyComposer(reviewId: number): void {
    const state = this.getRepliesState(reviewId);
    state.composerOpen = !state.composerOpen;
    state.error = '';
  }

  publishReply(review: Review): void {
    if (!this.place) {
      return;
    }

    const user = this.authService.getCurrentUser();
    const state = this.getRepliesState(review.id);
    if (!user?.id) {
      state.error = this.translocoService.translate('placeDetail.textLoginToReply');
      return;
    }

    const comment = state.replyComment.trim();
    if (!comment || state.publishing) {
      return;
    }

    state.publishing = true;
    state.error = '';

    const payload: CreateReviewRequest = {
      placeId: this.place.id,
      parentId: review.id,
      comment,
      score: null
    };

    this.reviewService.createReview(payload).subscribe({
      next: (createdReply) => {
        state.replyComment = '';
        state.composerOpen = false;
        state.publishing = false;
        state.totalElements += 1;

        if (state.expanded) {
          state.replies = [createdReply, ...state.replies];
        } else {
          state.replies = [createdReply, ...state.replies].slice(0, this.repliesPreviewSize);
        }
      },
      error: (error) => {
        state.error = typeof error?.error?.message === 'string'
          ? error.error.message
          : this.translocoService.translate('placeDetail.textPublishReplyError');
        state.publishing = false;
      }
    });
  }

  viewMoreReplies(reviewId: number): void {
    const state = this.getRepliesState(reviewId);
    state.expanded = true;
    if (state.hasNext) {
      this.loadReplies(reviewId, false);
    }
  }

  collapseReplies(reviewId: number): void {
    const state = this.getRepliesState(reviewId);
    state.expanded = false;
  }

  visibleReplies(reviewId: number): Review[] {
    const state = this.getRepliesState(reviewId);
    return state.expanded ? state.replies : state.replies.slice(0, this.repliesPreviewSize);
  }

  hasHiddenReplies(reviewId: number): boolean {
    const state = this.getRepliesState(reviewId);
    return state.totalElements > this.visibleReplies(reviewId).length;
  }

  hiddenRepliesCount(reviewId: number): number {
    const state = this.getRepliesState(reviewId);
    return Math.max(0, state.totalElements - this.visibleReplies(reviewId).length);
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

    const windowSize = 5;
    const halfWindow = Math.floor(windowSize / 2);
    let start = Math.max(0, this.currentReviewsPage - halfWindow);
    let end = Math.min(this.reviewsTotalPages, start + windowSize);

    if (end - start < windowSize) {
      start = Math.max(0, end - windowSize);
    }

    return Array.from({ length: end - start }, (_, index) => start + index);
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
    if (this.images.length === 0) {
      return;
    }

    this.currentImageIndex = (this.currentImageIndex + 1) % this.images.length;
  }

  prevImage(): void {
    if (this.images.length === 0) {
      return;
    }

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

  backToDepartment(): void {
    if (!this.backDepartmentId) return;
    this.router.navigate(['/department', this.backDepartmentId]);
  }

  backToModifyItinerary(): void {
    if (!this.backItineraryId) {
      this.router.navigate(['/itinerarios']);
      return;
    }

    this.router.navigate(['/modify-itinerario', this.backItineraryId]);
  }
}
