import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FavoriteService {

  private apiUrl = 'http://localhost:8080/api/favorites'; 
  
  constructor(private httpClient: HttpClient) { }


  //Método para obtener los favoritos de un usuario
  getUserFavorites(userId: number): Observable<any[]> {
    return this.httpClient.get<any[]>(`${this.apiUrl}/user/${userId}`);
  }
}
