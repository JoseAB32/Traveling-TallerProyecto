import { Component, OnInit, inject } from '@angular/core';
import { HeaderComponent } from "../header/header.component";
import { FooterComponent } from "../footer/footer.component";
import { PlaceCardComponent } from '../components/place-card/place-card.component';
import { FavoriteService } from '../services/favorite/favorite.service';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [HeaderComponent, FooterComponent, PlaceCardComponent],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent {

  Favoritos: any[] =[]; 
  
  private favoriteService = inject(FavoriteService);

  ngOnInit(): void {
    this.cargarFavoritos();
  }

  cargarFavoritos() {
    const userId = 1; // ID de prueba

    this.favoriteService.getUserFavorites(userId).subscribe({
      next: (data) => {
        if (data) {
          this.Favoritos = data.map(favorito => favorito.place);
        }
      },
      error: (error) => {
        console.error('Error al obtener los favoritos', error);
      }
    });
  }
}
