import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { City } from './city';

@Injectable({
  providedIn: 'root'
})
export class CityService {

  private baseURL= "http://localhost:8080/api/cities";

  constructor(private httpClient: HttpClient) { }

  getCities(): Observable<City[]>{
    return this.httpClient.get<City[]>(`${this.baseURL}`);
  }
}
