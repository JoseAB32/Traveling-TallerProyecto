import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { City } from '../../models/city/city';
import { CONSTANTS } from '../../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class CityService {

  private baseURL = CONSTANTS.API.BASE_URL + CONSTANTS.API.CITIES;

  constructor(private httpClient: HttpClient) { }

  getCities(): Observable<City[]> {


    return this.httpClient.get<City[]>(this.baseURL).pipe(

      tap((data) => {
        console.log("Cantidad de ciudades:", data.length);
      }),

      catchError((error) => {
        console.error(CONSTANTS.MESSAGES.ERROR.LOAD_CITIES, error);
        return throwError(() => error);
      })

    );
  }
}