import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';
import { Place } from '../place';
import { PlaceService } from '../place.service';

@Component({
  selector: 'app-search-places',
  standalone: true,
  imports: [CommonModule, HeaderComponent, FooterComponent],
  templateUrl: './search-places.component.html',
  styleUrl: './search-places.component.css'
})
export class SearchPlacesComponent {
  places: Place[] = [];
  isLoading = true;

  constructor(private placeService: PlaceService) {}

  ngOnInit(): void {
    this.placeService.getPlacesOrdenado().subscribe({
      next: (data) => {
        this.places = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error cargando lugares:', err);
        this.isLoading = false;
      }
    });
  }


  getStars(rating: number): string {
    const filledStars = Math.max(0, Math.min(Math.floor(rating), 5));
    return '★'.repeat(filledStars) + '☆'.repeat(5 - filledStars);
  }
}
