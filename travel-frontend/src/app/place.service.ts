import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Place } from './place';
import { of, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PlaceService {

  private baseUrl = 'http://localhost:8080/api/places';

  constructor(private httpClient: HttpClient) { }

  getPlacesOrdenado():Observable<Place[]>{
    return this.httpClient.get<Place[]>(`${this.baseUrl}/top-rated`);  
  }

  getPlaces(): Observable<Place[]> {
    return this.httpClient.get<Place[]>(this.baseUrl);
  }

  searchPlaces(term: string): Observable<Place[]> {
  return this.httpClient.get<Place[]>(`${this.baseUrl}/search?q=${term}`);
}
}
