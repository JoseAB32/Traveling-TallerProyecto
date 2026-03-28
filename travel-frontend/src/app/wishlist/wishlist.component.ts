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
  private currentUserId: number | null = null; //variable para guardar el ID

  private favoriteService = inject(FavoriteService);
  private authService = inject(AuthService);

  isModalOpen: boolean = false;
  placeToDeleteId: number | null = null;

  ngOnInit(): void {
    this.userSub = this.authService.currentUser$.subscribe(user => {
      if (user && user.id) {
        this.currentUserId = user.id;
        this.cargarFavoritos(user.id);
      } else {
        this.currentUserId = null;
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

  openConfirmModal(placeId: number) {
    this.placeToDeleteId = placeId;
    this.isModalOpen = true;
  }

  closeConfirmModal() {
    this.isModalOpen = false;
    this.placeToDeleteId = null;
  }

  confirmDelete() {
    if (this.placeToDeleteId !== null) {
      this.deleteFavorite(this.placeToDeleteId); 
      this.closeConfirmModal(); 
    }
  }

  deleteFavorite(placeId: number) {
    if (!this.currentUserId) {
      console.warn('No hay usuario logueado.');
      return;
    }

    this.favoriteService.removeFavorite(this.currentUserId, placeId).subscribe({
      next: () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
        this.Favoritos = this.Favoritos.filter(lugar => lugar.id !== placeId);
        console.log('Lugar eliminado de favoritos');
      },
      error: (err) => {
        console.error('Ocurrió un error al quitar el favorito', err);
      }
    });
  }

  ngOnDestroy(): void {
    // Limpiar la suscripción para evitar fugas de memoria
    if (this.userSub) {
      this.userSub.unsubscribe();
    }
  }
}