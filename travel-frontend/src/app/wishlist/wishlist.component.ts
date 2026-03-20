import { Component } from '@angular/core';
import { HeaderComponent } from "../header/header.component";
import { FooterComponent } from "../footer/footer.component";
import { PlaceCardComponent } from '../components/place-card/place-card.component';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [HeaderComponent, FooterComponent, PlaceCardComponent],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent {
  // DATOS DE PRUEBA 
  Favoritos = [
    {
      id: 1,
      name: 'Cristo de la Concordia',
      address: 'Cochabamba',
      rating: 5,
      description: 'El Cristo de la Concordia es considerada la estatua de Jesús más grande que existe...',
      image_url: '1.png'
    },
    {
      id: 2,
      name: 'Parque',
      address: 'Cochabamba',
      rating: 4,
      description: 'El Parque de la Familia es un espacio turístico destinado a la orientación recreacional...',
      image_url: '2.png'
    },
    {
        id: 3,
        name: 'Salar de Uyuni',
        address: 'Potosí',
        rating: 5,
        description: 'El desierto de sal más grande del mundo, una maravilla natural única...',
        image_url: '3.png'
      }
  ];
}
