import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { HeaderComponent } from "../header/header.component";
import { FooterComponent } from "../footer/footer.component";
import { PlaceCardComponent } from '../components/place-card/place-card.component';
import { FavoriteService } from '../services/favorite/favorite.service';
import { AuthService } from '../services/auth.service';        
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [HeaderComponent, FooterComponent, PlaceCardComponent],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent implements OnInit, OnDestroy{

  Favoritos: any[] =[]; 
  private userSub!: Subscription;

  private favoriteService = inject(FavoriteService);
  private authService = inject(AuthService);

  ngOnInit(): void {
    this.userSub = this.authService.currentUser$.subscribe(user => {
      
      if (user && user.id) {
        this.cargarFavoritos(user.id);
      } else {
        console.warn('No hay usuario logueado o no se encontró el ID del usuario.');
        this.Favoritos =[];
      }
    });
  }

  cargarFavoritos(userId: number) {

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
  ngOnDestroy(): void {
    //Limpiar la suscripción para evitar fugas de memoria
    if (this.userSub) {
      this.userSub.unsubscribe();
    }
  }
}
