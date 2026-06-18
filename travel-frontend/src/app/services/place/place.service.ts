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
  images?: File[] | null;
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
    const formData = new FormData();

    formData.append('name', payload.name);
    formData.append('description', payload.description);
    formData.append('address', payload.address);
    formData.append('price', String(payload.price));
    formData.append('latitude', String(payload.latitude));
    formData.append('longitude', String(payload.longitude));

    formData.append('placeType', payload.place_type);
    formData.append('cityId', String(payload.city_id));
    formData.append('isEvent', String(payload.is_event));

    if (payload.is_event && payload.start_date) {
      formData.append('startDate', payload.start_date);
    }

    if (payload.is_event && payload.end_date) {
      formData.append('endDate', payload.end_date);
    }

    if (payload.images && payload.images.length > 0) {
      payload.images.forEach((image) => {
        formData.append('images', image);
      });
    }

    return this.httpClient.post<Place>(this.baseUrl, formData);
  }
}