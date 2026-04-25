import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PlaceService } from '../../services/place/place.service';
import { MapComponent } from '../../components/map/map.component'
import { HeaderComponent } from "../../components/header/header.component";
import { FooterComponent } from "../../components/footer/footer.component";
import { Place } from '../../models/place/place';
import { CommonModule} from '@angular/common';
import { Review } from '../../models/review/review';
import { ReviewService } from '../../services/review/review.service';
import { FeatureService } from '../../services/features/feature.service';
import { CityService } from '../../services/city/city.service';
import { City } from '../../models/city/city';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'app-department',
  standalone: true,
  imports: [MapComponent, FooterComponent, HeaderComponent, CommonModule, TranslocoModule],
  templateUrl: './department.component.html',
  styleUrl: './department.component.css'
})
export class DepartmentComponent implements OnInit {
  departmentId!: number;
  places: any[] = [];
  placesTop: any[] = [];
  loading: boolean = true;
  loadingTop: boolean = true;
  selectedPlaceFromMap: any = null;
  clickedPlaceFromMap: any = null;
  bestReviews: { [placeId: number]: Review | undefined } = {};
  private placeService = inject(PlaceService);
  private reviewService = inject(ReviewService);
  private cityService = inject(CityService);
  featureService = inject(FeatureService);
  features: any = {};
  city: City | null = null;


  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.featureService.getFeatures().subscribe((data: any) => this.features = data);
    this.departmentId = Number(this.route.snapshot.paramMap.get('id'));

    this.cityService.getCityById(this.departmentId).subscribe({
      next: (data) => {
        this.city = data;
      },
      error: (err) => {
        console.error(err);
        console.log(err.error.message);
      }
    });

    this.placeService.getPlacesByDepartment(this.departmentId).subscribe({
      next: (data: Place[]) => {
        this.places = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando lugares', err);
        this.loading = false;
        }
      });


    this.placeService.getTopPlacesByDepartment(this.departmentId).subscribe({
      next: (data: Place[]) => {
        this.placesTop = data;
        this.loadingTop = false;

        // 2. MAGIA AQUÍ: Apenas llegan los lugares, pedimos sus reseñas
        this.placesTop.forEach(place => {
          this.reviewService.getTopReviewByPlaceId(place.id).subscribe({
            next: (review: Review) => {
              if (review) {
                // Guardamos la reseña en el diccionario usando el ID del lugar
                this.bestReviews[place.id] = review; 
              }
            },
            error: (err) => console.error('Error cargando reseña para lugar', place.id, err)
          });
        });
      },
      error: (err) => {
        console.error('Error cargando lugares', err);
        this.loadingTop = false;
      }
    });

  }

  goToDetail(id: number) {
    this.router.navigate(['/place', id]);
  }

  goToItinerary() {
    this.router.navigate(['/itinerarios'], { queryParams: { cityId: this.departmentId } });
  }

  onMapInteraction(place: any) {
    this.selectedPlaceFromMap = place;
  }

  onMapClicked(place: any) {
    this.clickedPlaceFromMap = place;
    if (this.features.pinRedirection) { 
      this.router.navigate(['/place', this.clickedPlaceFromMap.id]);
    }
  }
}
