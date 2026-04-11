import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Logger } from '../../models/logger/logger';
import { CONSTANTS } from '../../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class LoggerService {

  private baseURL = CONSTANTS.API.BASE_URL + CONSTANTS.API.LOGS;

  constructor(private httpClient: HttpClient) { }

  // 1. Obtener todos los logs (Vista general)
  getAllLogs(): Observable<Logger[]> {
    return this.httpClient.get<Logger[]>(this.baseURL);
  }

  // 2. Obtener logs filtrados (El reto del Admin)
  getFilteredLogs(module: string, level: string, start: string, end: string): Observable<Logger[]> {
    const params = new HttpParams()
      .set('module', module)
      .set('level', level)
      .set('startDate', start)
      .set('endDate', end);

    return this.httpClient.get<Logger[]>(`${this.baseURL}/filter`, { params });
  }
}
