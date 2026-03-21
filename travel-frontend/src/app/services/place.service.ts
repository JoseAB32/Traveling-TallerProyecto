import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Place } from '../models/place.model';

@Injectable({
  providedIn: 'root'
})
export class PlaceService {
  
  // Datos mock de los lugares mejor calificados
  private places: Place[] = [
    {
      id: 1,
      name: 'Lago Titicaca',
      location: 'La Paz',
      description: 'El lago navegable más alto del mundo, con aguas cristalinas y cultura viva.',
      fullDescription: 'El Lago Titicaca es uno de los lagos más importantes de Sudamérica. Compartido entre Bolivia y Perú, es conocido por sus aguas cristalinas, sus islas flotantes de los Uros y la Isla del Sol, cuna de la civilización inca.',
      rating: 4,
      imageUrl: '1.png',
      images: ['1.png', 'lago2.jpg', 'lago3.jpg'],
      phone: '72513842',
      price: 20,
      type: 'Natural',
      schedule: '8:00 AM - 6:00 PM',
      ministerio: 'MINISTERIO DE CULTURAS Y TURISMO',
      quote: 'El Lago Titicaca guarda los secretos de nuestras civilizaciones ancestrales.'
    },
    {
      id: 2,
      name: 'Parque Nacional Madidi',
      location: 'La Paz',
      description: 'Una de las áreas protegidas más biodiversas del mundo.',
      fullDescription: 'Madidi es una de las áreas protegidas más importantes de Bolivia y del mundo por su extraordinaria riqueza biológica. Contiene 12 grandes formaciones vegetales y miles de especies de flora y fauna.',
      rating: 4,
      imageUrl: '2.png',
      images: ['2.png', 'madidi2.jpg', 'madidi3.jpg'],
      phone: '72513843',
      price: 15,
      type: 'Natural',
      schedule: '8:00 AM - 5:00 PM',
      ministerio: 'MINISTERIO DE MEDIO AMBIENTE Y AGUA',
      quote: 'Las acciones que tenemos con la naturaleza determinan el trato que reciban tus hijos mañana.'
    },
    {
      id: 3,
      name: 'Cristo de la Concordia',
      location: 'Cochabamba',
      description: 'La estatua de Cristo más grande del mundo, con vista panorámica de la ciudad.',
      fullDescription: 'El Cristo de la Concordia es una imponente estatua ubicada en el cerro San Pedro, con vistas espectaculares de Cochabamba. Es el monumento más grande de Bolivia y una de las estatuas de Cristo más grandes del mundo.',
      rating: 5,
      imageUrl: '3.png',
      images: ['3.png', 'cristo2.jpg', 'cristo3.jpg'],
      phone: '72513844',
      price: 10,
      type: 'Religioso/Cultural',
      schedule: '9:00 AM - 6:00 PM',
      ministerio: 'GOBIERNO AUTÓNOMO MUNICIPAL DE COCHABAMBA',
      quote: 'Un símbolo de fe y un mirador privilegiado del valle cochabambino.'
    }
  ];

  constructor() { }

  getPlaceById(id: number): Observable<Place | undefined> {
    const place = this.places.find(p => p.id === id);
    return of(place);
  }

  getAllPlaces(): Observable<Place[]> {
    return of(this.places);
  }
}