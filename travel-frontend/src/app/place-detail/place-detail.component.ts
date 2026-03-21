import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-place-detail',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    FooterComponent,
    RouterModule
  ],
  templateUrl: './place-detail.component.html',
  styleUrls: ['./place-detail.component.css']
})
export class PlaceDetailComponent {

  title: string = '';
  description: string = '';
  location: string = '';
  phone: string = '';
  price: string = '';
  category: string = '';

  images: string[] = [];
  currentImageIndex: number = 0;

  loading: boolean = false;
  error: string = '';

  constructor(private route: ActivatedRoute) {
    this.loadPlace();
  }

  loadPlace() {
    const id = this.route.snapshot.paramMap.get('id');

    switch (id) {

        // 🌊 LAGO TITICACA
      case '1':
        this.title = 'Lago Titicaca, La Paz';
        this.description = 'El Lago Titicaca es el lago navegable más alto del mundo y un lugar lleno de historia y cultura andina. Sus aguas albergan islas tradicionales como la Isla del Sol.';
        this.location = 'Copacabana, La Paz';
        this.phone = '71234567';
        this.price = '10$';
        this.category = 'Turístico';
        this.images = [
          'asset_10.png'
        ];
        break;

      // 🌿 MADIDI
      case '2':
        this.title = 'Parque Nacional Madidi, La Paz';
        this.description = 'Madidi es una de las áreas protegidas más importantes del mundo por su enorme biodiversidad...';
        this.location = 'Ucumarí, La Paz';
        this.phone = '72513843';
        this.price = '15$';
        this.category = 'Natural';
        this.images = [
          'asset_11.png'
        ];
        break;

      // 🏔️ CRISTO DE LA CONCORDIA
      case '3':
        this.title = 'Cristo de la Concordia, Cochabamba';
        this.description = 'El Cristo de la Concordia es una de las estatuas de Cristo más grandes del mundo y ofrece una vista panorámica impresionante de toda la ciudad de Cochabamba.';
        this.location = 'Cochabamba, Bolivia';
        this.phone = '79876543';
        this.price = '5$';
        this.category = 'Cultural';
        this.images = [
          'asset_1.png'
        ];
        break;

      default:
        this.error = 'Lugar no encontrado';
    }
  }

  nextImage() {
    this.currentImageIndex =
      (this.currentImageIndex + 1) % this.images.length;
  }

  prevImage() {
    this.currentImageIndex =
      (this.currentImageIndex - 1 + this.images.length) % this.images.length;
  }

  setImage(index: number) {
    this.currentImageIndex = index;
  }
}