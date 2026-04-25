import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { CONSTANTS } from '../../utils/constants';

export interface Features {
  pinRedirection: boolean;
  autoCreateItinerary: boolean;
  showSearchPlaces: boolean;
  showFavorites: boolean;
  [key: string]: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class FeatureService {
  private http = inject(HttpClient);
  private baseUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.FEATURES; 

  // Signal para reactividad - fallback
  private _features = signal<Features>({
    pinRedirection: true,
    autoCreateItinerary: true,
    showSearchPlaces: true,
    showFavorites: true,
  });

readonly features = this._features.asReadonly();

  loadFeatures(): Observable<Features> {
    return this.http.get<Features>(this.baseUrl).pipe(
      tap(data => this._features.set(data))
    );
  }

  // Signal cacheado, sin HTTP
  isEnabled(key: keyof Features): boolean {
    return this._features()[key] ?? false;
  }

  updateFeatures(features: Features): Observable<Features> {
    return this.http.put<Features>(this.baseUrl, features).pipe(
      tap(data => this._features.set(data))
    );
  }
}