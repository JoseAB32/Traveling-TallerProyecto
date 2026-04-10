import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PlaceService } from '../place.service';
import { MapComponent } from '../components/map/map.component'
import { HeaderComponent } from "../header/header.component";
import { FooterComponent } from "../footer/footer.component";
import { Place } from '../place';
import { CommonModule} from '@angular/common';

@Component({
  selector: 'app-department',
  standalone: true,
  imports: [MapComponent, FooterComponent, HeaderComponent, CommonModule, RouterLink],
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
  private placeService = inject(PlaceService);

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.departmentId = Number(this.route.snapshot.paramMap.get('id'));

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

  onMapInteraction(place: any) {
    this.selectedPlaceFromMap = place;
  }
}