import { Injectable } from '@angular/core';
import { CONSTANTS } from './utils/constants';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Review } from './review';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  private baseUrl = CONSTANTS.API.BASE_URL + "/api/reviews";


  constructor(private http: HttpClient) { }

  getTopReviewByPlaceId(placeId: number): Observable<Review> {
    return this.http.get<Review>(`${this.baseUrl}/mejor-resenia?placeId=${placeId}`);
  }
}
