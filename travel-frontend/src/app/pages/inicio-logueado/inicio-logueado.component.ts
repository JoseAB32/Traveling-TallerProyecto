import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Place } from '../../models/place/place';
import { PlaceService } from '../../services/place/place.service';
import { HeaderComponent } from "../../components/header/header.component";
import { FooterComponent } from "../../components/footer/footer.component";
import { Router } from '@angular/router';
import { NgIf } from '@angular/common';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'app-inicio-logueado',
  standalone: true,
  imports: [CommonModule, HeaderComponent, FooterComponent, NgIf, TranslocoModule],
  templateUrl: './inicio-logueado.component.html',
  styleUrl: './inicio-logueado.component.css'
})
export class InicioLogueadoComponent implements OnInit {
  featuredPlaces: Place[] = [];
  isLoading = true;
  readonly cardsPerPage = 3;
  currentTrackIndex = this.cardsPerPage;
  isTransitionEnabled = true;

  constructor(private placeService: PlaceService, private router: Router) {}

  ngOnInit(): void {
    this.placeService.getPlacesOrdenado().subscribe({
      next: (data) => {
        this.featuredPlaces = data || [];
        this.currentTrackIndex = this.cardsPerPage;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error al conectar con la base de datos:', err);
        this.isLoading = false;
      }
    });
  }

  hoverDepto: string = '';
  tooltipX: number = 0;
  tooltipY: number = 0;

  onMouseMove(event: MouseEvent): void {
    this.tooltipX = event.clientX + 10;
    this.tooltipY = event.clientY + 10;
  }

  get carouselPlaces(): Place[] {
    const len = this.featuredPlaces.length;
    if (len === 0) return [];
    if (len <= this.cardsPerPage) return this.featuredPlaces;

    const leftClones = this.featuredPlaces.slice(-this.cardsPerPage);
    const rightClones = this.featuredPlaces.slice(0, this.cardsPerPage);
    return [...leftClones, ...this.featuredPlaces, ...rightClones];
  }

  get currentIndex(): number {
    const len = this.featuredPlaces.length;
    if (len === 0) return 0;
    return (this.currentTrackIndex - this.cardsPerPage + len) % len;
  }

  get trackTransform(): string {
    const step = 100 / this.cardsPerPage;
    return `translateX(-${this.currentTrackIndex * step}%)`;
  }

  get trackTransition(): string {
    return this.isTransitionEnabled
      ? 'transform 420ms cubic-bezier(0.22, 0.61, 0.36, 1)'
      : 'none';
  }

  prev(): void {
    if (this.featuredPlaces.length === 0) return;
    this.isTransitionEnabled = true;
    this.currentTrackIndex -= 1;
  }

  next(): void {
    if (this.featuredPlaces.length === 0) return;
    this.isTransitionEnabled = true;
    this.currentTrackIndex += 1;
  }

  goTo(index: number): void {
    if (this.featuredPlaces.length === 0) return;
    this.isTransitionEnabled = true;
    this.currentTrackIndex = this.cardsPerPage + index;
  }

  onTrackTransitionEnd(): void {
    const len = this.featuredPlaces.length;
    if (len <= this.cardsPerPage) return;

    if (this.currentTrackIndex >= len + this.cardsPerPage) {
      this.isTransitionEnabled = false;
      this.currentTrackIndex = this.cardsPerPage;
      return;
    }

    if (this.currentTrackIndex < this.cardsPerPage) {
      this.isTransitionEnabled = false;
      this.currentTrackIndex = len + this.currentTrackIndex;
    }
  }

  goToDetail(id: number) {
    this.router.navigate(['/place', id]);
  }

  getStarsArray(rating: number): boolean[] {
    const score = Math.max(0, Math.min(Math.floor(rating), 5));
    return Array(5).fill(false).map((_, i) => i < score);
  }

  goToDepartment(id: number) {
    this.router.navigate(['/department', id]);
  }

  trackByPlaceId(_: number, place: Place): number {
    return place.id;
  }

  isPeachPlace(index: number): boolean {
    if (this.featuredPlaces.length === 0) return false;

    const realIndex =
      (index - this.cardsPerPage + this.featuredPlaces.length) % this.featuredPlaces.length;

    return realIndex % 2 === 0;
  }
}
