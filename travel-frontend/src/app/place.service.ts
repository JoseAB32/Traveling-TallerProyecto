import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Place } from './place';
import { of, Observable } from 'rxjs';
import { delay } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class PlaceService {

  private baseUrl = 'http://localhost:8080/api/places';
  constructor(private httClient: HttpClient) { }

  getPlacesOrdenado():Observable<Place[]>{
    const res: Place[] = [
      {
        id: 1,
        name: 'Lago Titicaca, La Paz',
        description: 'El lago navegable más alto del mundo, cuna de la civilización Inca.',
        address: 'Copacabana, Manco Kapac',
        image_url: 'assets/titicaca.png', 
        rating: 4.5,
        city_id: 1, 
        is_event: false,
        start_date: null,
        end_date: null,
        state: true
      },
      {
        id: 2,
        name: 'Parque Nacional Madidi, La Paz',
        description: 'Uno de los parques con mayor biodiversidad del planeta.',
        address: 'Región de Apolo y San Buenaventura',
        image_url: 'assets/madidi.png',
        rating: 4.8,
        city_id: 1, 
        is_event: false,
        start_date: null,
        end_date: null,
        state: true
      },
      {
        id: 3,
        name: 'Cristo de la Concordia, Cochabamba',
        description: 'Estatua monumental situada en el cerro de San Pedro.',
        address: 'Av. de la Concordia',
        image_url: 'assets/cristo.png',
        rating: 5.0,
        city_id: 2, 
        is_event: false,
        start_date: null,
        end_date: null,
        state: true
      },
      {
        id: 4,
        name: 'Carnaval de Oruro',
        description: 'Obra Maestra del Patrimonio Oral e Intangible de la Humanidad.',
        address: 'Avenida Cívica',
        image_url: 'assets/carnaval.png',
        rating: 5.0,
        city_id: 4, 
        is_event: true, 
        start_date: '2026-02-14',
        end_date: '2026-02-17',
        state: true
      }
    ];
    return of(res).pipe(delay(1000));
  }
}
