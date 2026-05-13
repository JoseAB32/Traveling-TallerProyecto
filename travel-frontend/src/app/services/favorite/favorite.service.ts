import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CONSTANTS } from '../../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class FavoriteService {

  private apiUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.FAVORITES;

  constructor(private httpClient: HttpClient) { }

  getUserFavorites(): Observable<any[]> {
    return this.httpClient.get<any[]>(`${this.apiUrl}/user`);
  }

  addFavorite(placeId: number): Observable<any> {
    return this.httpClient.post(`${this.apiUrl}/user/place/${placeId}`, {});
  }

  removeFavorite(placeId: number): Observable<any> {
    return this.httpClient.delete(`${this.apiUrl}/user/place/${placeId}`);
  }
}