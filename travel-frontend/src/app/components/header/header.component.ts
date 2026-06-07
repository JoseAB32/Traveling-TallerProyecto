import { Component, OnInit, OnDestroy, ElementRef, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth/auth.service';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { Place } from '../../models/place/place';
import { User } from '../../models/user/user';
import { PlaceService } from '../../services/place/place.service';
import { UserService } from '../../services/user/user.service';
import { FEATURES } from '../../features/features';
import { FeatureService } from '../../services/features/feature.service';
import { TranslocoService, TranslocoModule } from '@jsverse/transloco';
import { HttpErrorResponse } from '@angular/common/http'

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslocoModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit, OnDestroy {

  idiomaDef: string = '';

  profile: User | null = null;

  isAdmin: boolean = false;
  isLogsEnabled: boolean = FEATURES.adminLogsEnabled;

  featureService = inject(FeatureService);
  private userService = inject(UserService);

  isLoggedIn: boolean = false;
  isMenuOpen: boolean = false;
  isListaOpen: boolean = false;

  private userSub?: Subscription;
  private searchCacheSub?: Subscription;

  searchTerm: string = '';
  suggestions: Place[] = [];
  allPlaces: Place[] = [];
  showSuggestions: boolean = false;

  // Se mantiene porque tu HTML actual lo usa para spinner/loading.
  isSearchLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private placeService: PlaceService,
    private el: ElementRef,
    private translocoService: TranslocoService
  ) {
    this.idiomaDef = this.translocoService.getActiveLang().toUpperCase();
  }

  ngOnInit(): void {
    const savedLang = localStorage.getItem('lang') || 'es';
    this.translocoService.setActiveLang(savedLang);
    this.idiomaDef = this.translocoService.getActiveLang().toUpperCase();
    this.isAdmin = this.authService.isAdmin();
    this.isListaOpen = false;

    this.userSub = this.authService.currentUser$.subscribe(user => {
      this.isLoggedIn = !!user;

      if (this.isLoggedIn) {
        this.loadSearchCache();
        this.userService.getProfile().subscribe({
          next: (data: User) => {
            this.profile = data;
            },
            error: (err: HttpErrorResponse) => {
              console.error('Error cargando perfil', err);
            }
          });
      }

      if (!this.isLoggedIn) {
        this.isMenuOpen = false;
        this.suggestions = [];
        this.allPlaces = [];
        this.showSuggestions = false;
        this.isSearchLoading = false;
      }
    });
  }

  private loadSearchCache(): void {
    if (this.allPlaces.length > 0 || this.isSearchLoading) {
      return;
    }

    this.isSearchLoading = true;

    this.searchCacheSub = this.placeService.getSearchCache().subscribe({
      next: (places) => {
        this.allPlaces = places;
        this.isSearchLoading = false;
      },
      error: (error) => {
        console.error('Error cargando cache de búsqueda:', error);
        this.allPlaces = [];
        this.isSearchLoading = false;
      }
    });
  }

  changeLang(lang: string): void {
    const currentLang = localStorage.getItem('lang') || 'es';

    if (currentLang === lang) {
      return;
    }

    this.translocoService.setActiveLang(lang);
    localStorage.setItem('lang', lang);
    this.idiomaDef = this.translocoService.getActiveLang().toUpperCase();

    window.location.reload();
  }

  toggleLista(): void {
    this.isListaOpen = !this.isListaOpen;
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchTerm = input.value;

    const cleanTerm = this.normalizeSearchText(this.searchTerm);

    if (cleanTerm.length < 2) {
      this.suggestions = [];
      this.showSuggestions = false;
      return;
    }

    if (this.allPlaces.length === 0) {
      this.loadSearchCache();
      this.suggestions = [];
      this.showSuggestions = true;
      return;
    }

    this.suggestions = this.allPlaces
      .filter(place => this.matchesPlaceSearch(place, cleanTerm))
      .slice(0, 5);

    this.showSuggestions = true;
  }

  private matchesPlaceSearch(place: Place, cleanTerm: string): boolean {
    const name = this.normalizeSearchText(place.name);
    const address = this.normalizeSearchText(place.address);
    const cityName = this.normalizeSearchText(place.city?.name || '');

    return name.includes(cleanTerm)
      || address.includes(cleanTerm)
      || cityName.includes(cleanTerm);
  }

  private normalizeSearchText(value: string | undefined | null): string {
    return (value || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();
  }

  selectSuggestion(place: Place): void {
    this.searchTerm = place.name;
    this.showSuggestions = false;
    this.isSearchLoading = false;
    this.router.navigate(['/place', place.id], { queryParams: { returnTo: 'search' } });
  }

  goToSearch(): void {
    if (this.searchTerm.trim()) {
      this.showSuggestions = false;
      this.isSearchLoading = false;
      this.router.navigate(['/SearchPlace'], { queryParams: { q: this.searchTerm.trim() } });
    }
  }

  @HostListener('document:click', ['$event'])
  clickout(event: Event): void {
    if (!this.el.nativeElement.contains(event.target)) {
      this.showSuggestions = false;
      this.isListaOpen = false;
      this.isMenuOpen = false;
    }
  }

  get hasProfilePicture(): boolean {
    return !!this.profile?.profilePictureUrl && this.profile.profilePictureUrl !== 'null';
  }
  
  ngOnDestroy(): void {
    this.userSub?.unsubscribe();
    this.searchCacheSub?.unsubscribe();
  }
}