import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CONSTANTS } from '../../utils/constants';
import {
  TranslationFilters,
  TranslationPageResponse,
  Translation,
  UpdateTranslationRequest
} from '../../models/translation/translation';

@Injectable({
  providedIn: 'root'
})
export class TranslationService {

  private baseUrl = CONSTANTS.API.BASE_URL + CONSTANTS.API.TRANSLATIONS;

  constructor(private httpClient: HttpClient) { }

  getTranslations(filters: TranslationFilters): Observable<TranslationPageResponse> {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 0))
      .set('size', String(filters.size ?? 20));

    if (filters.entityType) {
      params = params.set('entityType', filters.entityType);
    }

    if (filters.language) {
      params = params.set('language', filters.language);
    }

    if (filters.fieldName) {
      params = params.set('fieldName', filters.fieldName);
    }

    if (filters.entityId !== null && filters.entityId !== undefined) {
      params = params.set('entityId', String(filters.entityId));
    }

    return this.httpClient.get<TranslationPageResponse>(this.baseUrl, { params });
  }

  updateTranslation(id: number, request: UpdateTranslationRequest): Observable<Translation> {
    return this.httpClient.put<Translation>(`${this.baseUrl}/${id}`, request);
  }
}