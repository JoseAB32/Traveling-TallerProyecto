import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CONSTANTS } from '../../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class FeatureService {
  private http = inject(HttpClient);
  private baseUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.FEATURES; 

  getFeatures(): Observable<any> {
    return this.http.get(this.baseUrl);
  }

  updateFeatures(features: any): Observable<any> {
    return this.http.put(this.baseUrl, features);
  }
}