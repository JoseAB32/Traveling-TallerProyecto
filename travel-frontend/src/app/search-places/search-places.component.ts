import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { HeaderComponent } from '../components/header/header.component';
import { FooterComponent } from '../components/footer/footer.component';
import { Place } from '../place';
import { PlaceService } from '../place.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';

@Component({
  selector: 'app-search-places',
  standalone: true,
  imports: [CommonModule, HeaderComponent, FooterComponent, RouterLink],
  templateUrl: './search-places.component.html',
  styleUrl: './search-places.component.css'
})
export class SearchPlacesComponent {
  places: Place[] = [];
  isLoading = true;
  searchTerm: string = '';

  constructor(private placeService: PlaceService, private route: ActivatedRoute) {}

  ngOnInit(): void {
  // Escuchamos los cambios en los parámetros de la URL
    this.route.queryParams.pipe(
      switchMap(params => {
        this.isLoading = true;
        this.searchTerm = params['q'] || ''; // Capturamos el "?q=..."
        
        if (this.searchTerm) {
          return this.placeService.searchPlaces(this.searchTerm);
        } else {
          return this.placeService.getPlacesOrdenado(); // Si está vacío, muestra todos
        }
      })
    ).subscribe({
      next: (data) => {
        this.places = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error en búsqueda:', err);
        this.isLoading = false;
      }
    });
  }


  getStars(rating: number): string {
    const filledStars = Math.max(0, Math.min(Math.floor(rating), 5));
    return '★'.repeat(filledStars) + '☆'.repeat(5 - filledStars);
  }
}
