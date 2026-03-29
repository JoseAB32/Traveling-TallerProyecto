import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Place } from './place';

@Injectable({
  providedIn: 'root'
})
export class PlaceService {

  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // 🔥 LISTAR
  getPlacesOrdenado(): Observable<Place[]> {
    return this.http.get<Place[]>(`${this.apiUrl}/places`);
  }

  // 🔥 DETALLE POR ID
  getPlaceById(id: number): Observable<Place> {
    return this.http.get<Place>(`${this.apiUrl}/places/${id}`);
  }
}