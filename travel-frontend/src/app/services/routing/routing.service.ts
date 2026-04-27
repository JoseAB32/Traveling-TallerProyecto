import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface RouteResult {
  distanceKm: number;
  durationMinutes: number;
  coordinates: [number, number][];
}

@Injectable({
  providedIn: 'root'
})
export class RoutingService {
  private http = inject(HttpClient);

  getRoute(origin: any, destination: any): Observable<RouteResult> {
    const params = {
      originLat: String(origin.latitude),
      originLng: String(origin.longitude),
      destinationLat: String(destination.latitude),
      destinationLng: String(destination.longitude)
    };

    return this.http.get<RouteResult>(`${environment.apiUrl}/api/routes`, { params });
  }
}