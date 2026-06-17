import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Place } from '../../models/place/place';
import { Observable } from 'rxjs';
import { CONSTANTS } from '../../utils/constants';

export interface CreatePlaceRequest {
  name: string;
  description: string;
  address: string;
  price: number;
  latitude: number;
  longitude: number;
  place_type: string;
  city_id: number;
  is_event: boolean;
  start_date: string | null;
  end_date: string | null;
  image_url?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class PlaceService {

  private baseUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.PLACES;

  constructor(private httpClient: HttpClient) { }

  getPlacesOrdenado(): Observable<Place[]> {
    return this.httpClient.get<Place[]>(`${this.baseUrl}/top-rated`);
  }

  getTopRatedPlaces(): Observable<Place[]> {
    return this.getPlacesOrdenado();
  }

  getPlaces(): Observable<Place[]> {
    return this.httpClient.get<Place[]>(this.baseUrl);
  }

  searchPlaces(term: string): Observable<Place[]> {
    return this.httpClient.get<Place[]>(`${this.baseUrl}/search?q=${term}`);
  }

  getSearchCache(): Observable<Place[]> {
    return this.httpClient.get<Place[]>(`${this.baseUrl}/search/cache`);
  }

  getPlaceById(id: number): Observable<Place> {
    return this.httpClient.get<Place>(`${this.baseUrl}/${id}`);
  }

  getPlacesByDepartment(cityId: number): Observable<Place[]> {
    return this.httpClient.get<Place[]>(`${this.baseUrl}/department/${cityId}`);
  }

  getTopPlacesByDepartment(cityId: number): Observable<Place[]> {
    return this.httpClient.get<Place[]>(`${this.baseUrl}/departmentTop/${cityId}`);
  }

  createPlace(payload: CreatePlaceRequest): Observable<Place> {
    return this.httpClient.post<Place>(this.baseUrl, payload);
  }
}