import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';

import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-place-detail',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    FooterComponent
  ],
  templateUrl: './place-detail.component.html',
  styleUrls: ['./place-detail.component.css']
})
export class PlaceDetailComponent implements OnInit {

  place: any = null;
  loading: boolean = true;

  currentImageIndex: number = 0;
  images: string[] = [];

  // DATOS DE PRUEBA COMPLETOS
  private mockPlaces: { [key: number]: any } = {
    1: {
      id: 1,
      name: 'Lago Titicaca, La Paz',
      description: 'El Lago Titicaca es el lago navegable más alto del mundo, ubicado a más de 3,800 metros sobre el nivel del mar. Es considerado la cuna de la civilización Inca y posee una gran importancia cultural e histórica para Bolivia y Perú. Sus aguas albergan una rica biodiversidad, incluyendo especies únicas de flora y fauna. En sus alrededores se encuentran comunidades que conservan tradiciones ancestrales, además de atractivos turísticos como la Isla del Sol y la Isla de la Luna, donde se pueden apreciar ruinas arqueológicas y paisajes impresionantes.',
      address: 'Copacabana, Manco Kapac',
      rating: 4.5,
      imageUrl: '/1.png',
      state: true
    },
    2: {
      id: 2,
      name: 'Parque Nacional Madidi, La Paz',
      description: 'El Parque Nacional Madidi es una de las áreas protegidas con mayor biodiversidad del planeta, ubicado en el norte de La Paz. Este parque alberga una impresionante variedad de ecosistemas que van desde los Andes hasta la Amazonía, incluyendo selvas tropicales, bosques nublados y sabanas. En él habitan miles de especies de plantas, mamíferos, aves y reptiles, muchas de ellas únicas en el mundo.',
      address: 'Región de Apolo y San Buenaventura',
      rating: 4.8,
      imageUrl: '/2.png',
      state: true
    },
    3: {
      id: 3,
      name: 'Cristo de la Concordia, Cochabamba',
      description: 'El Cristo de la Concordia es una de las estatuas de Cristo más grandes del mundo, ubicada en la cima del cerro San Pedro en Cochabamba. Con una altura superior a los 30 metros, ofrece una vista panorámica espectacular de toda la ciudad. Este monumento es un símbolo de paz y unión, y puede ser visitado mediante teleférico o subiendo más de mil escalones.',
      address: 'Av. de la Concordia',
      rating: 5.0,
      imageUrl: '/3.png',
      state: true
    },
    4: {
      id: 4,
      name: 'Carnaval de Oruro',
      description: 'El Carnaval de Oruro es una de las festividades culturales más importantes de Bolivia y fue declarado Obra Maestra del Patrimonio Oral e Intangible de la Humanidad por la UNESCO. Esta celebración combina tradiciones andinas, religiosas y folclóricas en un impresionante desfile lleno de danzas, música y trajes coloridos. Miles de bailarines participan en coreografías que representan historias y creencias ancestrales, siendo la Diablada uno de los bailes más emblemáticos.',
      address: 'Avenida Cívica',
      rating: 5.0,
      imageUrl: '/4.png',
      state: true,
      is_event: true,
      start_date: '2026-02-14',
      end_date: '2026-02-17'
    }
  };

  constructor(
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    
    console.log('🔍 ID recibido de la URL:', id);
    
    if (isNaN(id) || id === 0) {
      console.error('❌ ID inválido');
      this.loading = false;
      return;
    }
    
    this.loadPlaceDetails(id);
  }

  loadPlaceDetails(id: number): void {
    console.log('📡 Buscando lugar con ID:', id);
    
    // Simular tiempo de carga
    setTimeout(() => {
      const place = this.mockPlaces[id];
      
      if (place) {
        console.log('✅ Lugar encontrado:', place);
        this.place = place;
        
        /* 🔥 IMÁGENES DEL CARRUSEL SEGÚN EL LUGAR */
        switch (place.id) {
          case 1:
            this.images = ['/1.png', 'asset_10.png', 'asset_13.png', 'asset_14.png'];
            break;
          case 2:
            this.images = ['/2.png', 'asset_11.png', 'asset_15.png', 'asset_16.png'];
            break;
          case 3:
            this.images = ['/3.png', 'asset_1.png', 'asset_12.png', 'asset_17.png'];
            break;
          case 4:
            this.images = ['/4.png', 'asset_18.png', 'asset_19.png', 'asset_20.png'];
            break;
          default:
            this.images = [place.imageUrl];
        }
        
        console.log('🖼️ Imágenes del carrusel:', this.images);
        this.loading = false;
      } else {
        console.error('❌ Lugar no encontrado para ID:', id);
        this.loading = false;
      }
    }, 500);
  }

  nextImage(): void {
    if (this.images.length > 0) {
      this.currentImageIndex = (this.currentImageIndex + 1) % this.images.length;
    }
  }

  prevImage(): void {
    if (this.images.length > 0) {
      this.currentImageIndex = (this.currentImageIndex - 1 + this.images.length) % this.images.length;
    }
  }

  getStars(rating: number): string {
    const score = Math.floor(rating || 0);
    return '★'.repeat(score) + '☆'.repeat(5 - score);
  }
}