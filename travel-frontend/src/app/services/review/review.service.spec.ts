/// <reference types="jest" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController
} from '@angular/common/http/testing';

import { ReviewService } from './review.service';
import { CONSTANTS } from '../../utils/constants';
import {
  CreateReviewRequest,
  Review,
  ReviewPageResponse
} from '../../models/review/review';

describe('ReviewService', () => {
  let service: ReviewService;
  let httpMock: HttpTestingController;

  const baseUrl = CONSTANTS.API.BASE_URL + '/api/reviews';

  const mockReview = {
    id: 1,
    comment: 'Muy buen lugar',
    rating: 5,
    placeId: 10
  } as unknown as Review;

  const mockReviewPageResponse = {
    content: [
      {
        id: 1,
        comment: 'Muy buen lugar',
        rating: 5,
        placeId: 10
      },
      {
        id: 2,
        comment: 'Bonito lugar turístico',
        rating: 4,
        placeId: 10
      }
    ],
    totalElements: 2,
    totalPages: 1,
    size: 10,
    number: 0
  } as unknown as ReviewPageResponse;

  const mockCreateReviewPayload = {
    placeId: 10,
    rating: 5,
    comment: 'Muy buen lugar'
  } as unknown as CreateReviewRequest;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ReviewService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ReviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get top review by place id', () => {
    const placeId = 10;

    service.getTopReviewByPlaceId(placeId).subscribe((review) => {
      expect(review).toEqual(mockReview);
    });

    const req = httpMock.expectOne(
      `${baseUrl}/mejor-resenia?placeId=${placeId}`
    );

    expect(req.request.method).toBe('GET');

    req.flush(mockReview);
  });

  it('should get place reviews using default size', () => {
    const placeId = 10;
    const page = 0;

    service.getPlaceReviews(placeId, page).subscribe((response) => {
      expect(response).toEqual(mockReviewPageResponse);
    });

    const req = httpMock.expectOne(
      `${baseUrl}/place/${placeId}?page=${page}&size=10`
    );

    expect(req.request.method).toBe('GET');

    req.flush(mockReviewPageResponse);
  });

  it('should get place reviews using custom size', () => {
    const placeId = 10;
    const page = 1;
    const size = 5;

    service.getPlaceReviews(placeId, page, size).subscribe((response) => {
      expect(response).toEqual(mockReviewPageResponse);
    });

    const req = httpMock.expectOne(
      `${baseUrl}/place/${placeId}?page=${page}&size=${size}`
    );

    expect(req.request.method).toBe('GET');

    req.flush(mockReviewPageResponse);
  });

  it('should get review replies using default size', () => {
    const reviewId = 1;
    const page = 0;

    service.getReviewReplies(reviewId, page).subscribe((response) => {
      expect(response).toEqual(mockReviewPageResponse);
    });

    const req = httpMock.expectOne(
      `${baseUrl}/${reviewId}/replies?page=${page}&size=2`
    );

    expect(req.request.method).toBe('GET');

    req.flush(mockReviewPageResponse);
  });

  it('should get review replies using custom size', () => {
    const reviewId = 1;
    const page = 1;
    const size = 4;

    service.getReviewReplies(reviewId, page, size).subscribe((response) => {
      expect(response).toEqual(mockReviewPageResponse);
    });

    const req = httpMock.expectOne(
      `${baseUrl}/${reviewId}/replies?page=${page}&size=${size}`
    );

    expect(req.request.method).toBe('GET');

    req.flush(mockReviewPageResponse);
  });

  it('should create a review', () => {
    service.createReview(mockCreateReviewPayload).subscribe((review) => {
      expect(review).toEqual(mockReview);
    });

    const req = httpMock.expectOne(baseUrl);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockCreateReviewPayload);

    req.flush(mockReview);
  });
});