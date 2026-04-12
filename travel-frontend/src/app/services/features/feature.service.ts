import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FeatureService {
  private http = inject(HttpClient);
  // Reemplaza esto con tu CONSTANTS.API_URL si lo tienes
  private baseUrl = 'http://localhost:8080/api/features'; 

  getFeatures(): Observable<any> {
    return this.http.get(this.baseUrl);
  }

  updateFeatures(features: any): Observable<any> {
    return this.http.put(this.baseUrl, features);
  }
}