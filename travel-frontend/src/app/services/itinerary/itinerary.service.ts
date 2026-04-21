import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CONSTANTS } from '../../utils/constants';
import { Place } from '../../models/place/place';

export interface ItineraryDraftRequest {
  name?: string;
  startDate?: string | null;
  endDate?: string | null;
  placeIds: number[];
}

export interface ItineraryDraftResponse {
  tripId: number;
  userId: number;
  name: string;
  startDate: string | null;
  endDate: string | null;
  places: Place[];
}

@Injectable({
  providedIn: 'root'
})
export class ItineraryService {
  private apiUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.TRIPS;

  constructor(private http: HttpClient) {}

  getMyDraft(): Observable<ItineraryDraftResponse> {
    return this.http.get<ItineraryDraftResponse>(`${this.apiUrl}/draft/me`);
  }

  getDraftByUser(userId: number): Observable<ItineraryDraftResponse> {
    return this.http.get<ItineraryDraftResponse>(`${this.apiUrl}/draft/user/${userId}`);
  }

  saveDraft(payload: ItineraryDraftRequest): Observable<ItineraryDraftResponse> {
    return this.http.put<ItineraryDraftResponse>(`${this.apiUrl}/draft`, payload);
  }
}
