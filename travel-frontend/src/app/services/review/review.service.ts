import { Injectable } from '@angular/core';
import { CONSTANTS } from '../../utils/constants';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateReviewRequest, Review, ReviewPageResponse } from '../../models/review/review';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  private baseUrl = CONSTANTS.API.BASE_URL + "/api/reviews";


  constructor(private http: HttpClient) { }

  getTopReviewByPlaceId(placeId: number): Observable<Review> {
    return this.http.get<Review>(`${this.baseUrl}/mejor-resenia?placeId=${placeId}`);
  }

  getPlaceReviews(placeId: number, page: number, size: number = 10): Observable<ReviewPageResponse> {
    return this.http.get<ReviewPageResponse>(`${this.baseUrl}/place/${placeId}?page=${page}&size=${size}`);
  }

  createReview(payload: CreateReviewRequest): Observable<Review> {
    return this.http.post<Review>(this.baseUrl, payload);
  }
}
