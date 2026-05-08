import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ReviewService } from './review.service';
import { CreateReviewRequest } from '../../models/review/review';

describe('ReviewService', () => {
  let service: ReviewService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
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

  it('should call paginated place reviews endpoint', () => {
    service.getPlaceReviews(3, 0, 10).subscribe(response => {
      expect(response.page).toBe(0);
      expect(response.size).toBe(10);
      expect(response.content.length).toBe(1);
    });

    const req = httpMock.expectOne(r =>
      r.method === 'GET' &&
      r.url.includes('/api/reviews/place/3') &&
      r.urlWithParams.includes('page=0') &&
      r.urlWithParams.includes('size=10')
    );

    req.flush({
      content: [{
        id: 1,
        comment: 'Excelente',
        score: 5,
        state: true,
        user: { id: 1, userName: 'peter', correo: '', pass: '', birthday: '', city_id: null, state: true },
        place: { id: 3 }
      }],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
      hasNext: false
    });
  });

  it('should post create review payload', () => {
    const payload: CreateReviewRequest = {
      userId: 1,
      placeId: 3,
      parentId: null,
      comment: 'Muy bueno',
      score: 4
    };

    service.createReview(payload).subscribe(response => {
      expect(response.comment).toBe('Muy bueno');
      expect(response.score).toBe(4);
    });

    const req = httpMock.expectOne(r => r.method === 'POST' && r.url.includes('/api/reviews'));
    expect(req.request.body).toEqual(payload);
    req.flush({
      id: 11,
      comment: 'Muy bueno',
      score: 4,
      state: true,
      user: { id: 1, userName: 'erika', correo: '', pass: '', birthday: '', city_id: null, state: true },
      place: { id: 3 }
    });
  });

  it('should call review replies endpoint with default size', () => {
    service.getReviewReplies(12, 0).subscribe(response => {
      expect(response.page).toBe(0);
      expect(response.size).toBe(2);
      expect(response.content.length).toBe(1);
    });

    const req = httpMock.expectOne(r =>
      r.method === 'GET' &&
      r.url.includes('/api/reviews/12/replies') &&
      r.urlWithParams.includes('page=0') &&
      r.urlWithParams.includes('size=2')
    );

    req.flush({
      content: [{
        id: 22,
        comment: 'Respuesta',
        state: true,
        user: { id: 2, userName: 'ana', correo: '', pass: '', birthday: '', city_id: null, state: true },
        place: { id: 3 }
      }],
      page: 0,
      size: 2,
      totalElements: 3,
      totalPages: 2,
      hasNext: true
    });
  });
});
