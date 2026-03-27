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

  getPlacesOrdenado(): Observable<Place[]> {
    const res: Place[] = [
      {
        id: 1,
        name: 'Lago Titicaca, La Paz',
        description: 'El Lago Titicaca es el lago navegable más alto del mundo, ubicado a más de 3,800 metros sobre el nivel del mar. Es considerado la cuna de la civilización Inca y posee una gran importancia cultural e histórica para Bolivia y Perú. Sus aguas albergan una rica biodiversidad, incluyendo especies únicas de flora y fauna. En sus alrededores se encuentran comunidades que conservan tradiciones ancestrales, además de atractivos turísticos como la Isla del Sol y la Isla de la Luna, donde se pueden apreciar ruinas arqueológicas y paisajes impresionantes.',
        address: 'Copacabana, Manco Kapac',
        image_url: '1.png',
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
        description: 'El Parque Nacional Madidi es una de las áreas protegidas con mayor biodiversidad del planeta, ubicado en el norte de La Paz. Este parque alberga una impresionante variedad de ecosistemas que van desde los Andes hasta la Amazonía, incluyendo selvas tropicales, bosques nublados y sabanas. En él habitan miles de especies de plantas, mamíferos, aves y reptiles, muchas de ellas únicas en el mundo. Además, es hogar de comunidades indígenas que mantienen sus costumbres y conocimientos tradicionales, convirtiéndolo en un destino ideal para el ecoturismo y la investigación científica.',
        address: 'Región de Apolo y San Buenaventura',
        image_url: '2.png',
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
        description: 'El Cristo de la Concordia es una de las estatuas de Cristo más grandes del mundo, ubicada en la cima del cerro San Pedro en Cochabamba. Con una altura superior a los 30 metros, ofrece una vista panorámica espectacular de toda la ciudad. Este monumento es un símbolo de paz y unión, y puede ser visitado mediante teleférico o subiendo más de mil escalones. Es uno de los principales atractivos turísticos de Cochabamba y un lugar ideal para disfrutar del paisaje y la cultura local.',
        address: 'Av. de la Concordia',
        image_url: '3.png',
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
        description: 'El Carnaval de Oruro es una de las festividades culturales más importantes de Bolivia y fue declarado Obra Maestra del Patrimonio Oral e Intangible de la Humanidad por la UNESCO. Esta celebración combina tradiciones andinas, religiosas y folclóricas en un impresionante desfile lleno de danzas, música y trajes coloridos. Miles de bailarines participan en coreografías que representan historias y creencias ancestrales, siendo la Diablada uno de los bailes más emblemáticos. Es un evento que atrae a visitantes de todo el mundo cada año.',
        address: 'Avenida Cívica',
        image_url: '/4.png',
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

  // 🔥 ESTE MÉTODO TE FALTABA
  getPlaceById(id: number): Observable<Place | undefined> {
    return this.getPlacesOrdenado().pipe(
      // simple filtro sin backend real
      delay(0),
      (source) => new Observable(observer => {
        source.subscribe(data => {
          const place = data.find(p => p.id === id);
          observer.next(place);
          observer.complete();
        });
      })
    );
  }
}