import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { HeaderComponent } from "../header/header.component";
import { FooterComponent } from "../footer/footer.component";
import { PlaceCardComponent } from '../components/place-card/place-card.component';
import { PlaceService } from '../place.service';
import { Subscription, forkJoin } from 'rxjs';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [HeaderComponent, FooterComponent, PlaceCardComponent],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent implements OnInit, OnDestroy {

  Favoritos: any[] = [];
  private placeService = inject(PlaceService);
  private userSub!: Subscription;

  isModalOpen: boolean = false;
  placeToDeleteId: number | null = null;

  ngOnInit(): void {
    this.cargarFavoritosLocal();
  }

  cargarFavoritosLocal() {
    const wishListIds: number[] = JSON.parse(localStorage.getItem('wishList') || '[]');

    if (wishListIds.length === 0) {
      this.Favoritos = [];
      return;
    }

    const requests = wishListIds.map(id =>
      this.placeService.getPlaceById(id)
    );

    forkJoin(requests).subscribe({
      next: (places) => this.Favoritos = places,
      error: () => console.error('Error cargando favoritos')
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
    this.Favoritos = this.Favoritos.filter(lugar => lugar.id !== placeId);

    let wishListIds: number[] = JSON.parse(localStorage.getItem('wishList') || '[]');
    wishListIds = wishListIds.filter(id => id !== placeId);

    localStorage.setItem('wishList', JSON.stringify(wishListIds));
  }

  ngOnDestroy(): void {
    if (this.userSub) this.userSub.unsubscribe();
  }
}