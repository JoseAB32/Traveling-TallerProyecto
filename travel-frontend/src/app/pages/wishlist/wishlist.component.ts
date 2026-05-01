import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { HeaderComponent } from "../../components/header/header.component";
import { FooterComponent } from "../../components/footer/footer.component";
import { PlaceCardComponent } from '../../components/place-card/place-card.component';
import { PlaceService } from '../../services/place/place.service';
import { Subscription, forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
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
  private placeService = inject(PlaceService);
  private authService = inject(AuthService);
  private userSub!: Subscription;
  private currentUserId: number | null = null;

  isModalOpen: boolean = false;
  placeToDeleteId: number | null = null;

  ngOnInit(): void {
    this.userSub = this.authService.currentUser$.subscribe(user => {
      if (user?.id) {
        this.currentUserId = user.id;
        this.cargarFavoritosLocal(user.id);
        return;
      }

      this.currentUserId = null;
      this.Favoritos = [];
      this.isLoading = false;
    });
  }

  private getWishlistKey(userId: number): string {
    return `wishList_${userId}`;
  }

  cargarFavoritosLocal(userId: number) {
    this.isLoading = true;
    const wishListIds: number[] = JSON.parse(localStorage.getItem(this.getWishlistKey(userId)) || '[]');

    if (wishListIds.length === 0) {
      this.Favoritos = [];
      this.isLoading = false;
      return;
    }

    const requests = wishListIds.map(id =>
      this.placeService.getPlaceById(id).pipe(
        catchError(error => {
          console.error(`Error cargando lugar con ID ${id}`, error);
          return of(null);
        })
      )
    );

    forkJoin(requests).pipe(
      map(places => places.filter((place): place is Place => place !== null))
    ).subscribe({
      next: (places) => {
        this.Favoritos = places;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        console.error('Error cargando favoritos');
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
    if (!this.currentUserId) return;

    this.Favoritos = this.Favoritos.filter(lugar => lugar.id !== placeId);

    let wishListIds: number[] = JSON.parse(localStorage.getItem(this.getWishlistKey(this.currentUserId)) || '[]');
    wishListIds = wishListIds.filter(id => id !== placeId);

    localStorage.setItem(this.getWishlistKey(this.currentUserId), JSON.stringify(wishListIds));
  }

  ngOnDestroy(): void {
    if (this.userSub) this.userSub.unsubscribe();
  }
}
