import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Place } from './place';
import { Observable, map } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PlaceService {

  private baseUrl = 'http://localhost:8080/api/places';

  constructor(private httpClient: HttpClient) { }

  /**
   * Obtiene todos los lugares ordenados por rating (mejores primero)
   * Filtra solo los lugares con state = 1 (true) y rating >= 4
   */
  getPlacesOrdenado(): Observable<Place[]> {
    return this.httpClient.get<Place[]>(this.baseUrl).pipe(
      map(places => places
        .filter(place => place.state === true && place.rating >= 4)
        .sort((a, b) => b.rating - a.rating)
      )
    );
  }

  /**
   * Obtiene un lugar por su ID
   */
  getPlaceById(id: number): Observable<Place> {
    return this.httpClient.get<Place>(`${this.baseUrl}/${id}`);
  }

  /**
   * Obtiene todos los lugares (sin filtrar)
   */
  getPlaces(): Observable<Place[]> {
    return this.httpClient.get<Place[]>(this.baseUrl);
  }
}