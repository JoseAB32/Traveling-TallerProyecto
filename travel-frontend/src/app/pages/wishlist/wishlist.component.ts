import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { HeaderComponent } from "../../components/header/header.component";
import { FooterComponent } from "../../components/footer/footer.component";
import { PlaceCardComponent } from '../../components/place-card/place-card.component';
import { FavoriteService } from '../../services/favorite/favorite.service';
import { Subscription } from 'rxjs';
import { TranslocoModule } from '@jsverse/transloco';
import { Place } from '../../models/place/place';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [HeaderComponent, FooterComponent, PlaceCardComponent, TranslocoModule],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent implements OnInit, OnDestroy {

  Favoritos: Place[] = [];
  isLoading = true;
  private favoriteService = inject(FavoriteService);
  private authService = inject(AuthService);
  private userSub!: Subscription;

  isModalOpen: boolean = false;
  placeToDeleteId: number | null = null;

  ngOnInit(): void {
    this.userSub = this.authService.currentUser$.subscribe(user => {
      if (user?.id) {
        this.cargarFavoritos();
        return;
      }

      this.Favoritos = [];
      this.isLoading = false;
    });
  }

  cargarFavoritos() {
    this.isLoading = true;
    this.favoriteService.getUserFavorites().subscribe({
      next: (data) => {
        this.Favoritos = data ? data.map(favorito => favorito.place) : [];
        this.isLoading = false;
      },
      error: (error) => {
        this.Favoritos = [];
        this.isLoading = false;
        console.error('Error al obtener los favoritos', error);
      }
    });
  }

  openConfirmModal(placeId: number){
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
    this.favoriteService.removeFavorite(placeId).subscribe({
      next: () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
        this.Favoritos = this.Favoritos.filter(lugar => lugar.id !== placeId);
      },
      error: (err) => {
        console.error('Ocurrio un error al quitar el favorito', err);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.userSub) this.userSub.unsubscribe();
  }
}