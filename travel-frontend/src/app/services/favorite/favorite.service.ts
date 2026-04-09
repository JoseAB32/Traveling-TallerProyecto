import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CONSTANTS } from '../../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class FavoriteService {

  private apiUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.FAVORITES;

  constructor(private httpClient: HttpClient) { }


  //Método para obtener los favoritos de un usuario
  getUserFavorites(userId: number): Observable<any[]> {
    return this.httpClient.get<any[]>(`${this.apiUrl}/user/${userId}`);
  }

  removeFavorite(userId: number, placeId: number): Observable<any> {
    return this.httpClient.delete(`${this.apiUrl}/user/${userId}/place/${placeId}`);
  }
}
