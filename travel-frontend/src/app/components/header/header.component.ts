import { Component, OnInit, OnDestroy, ElementRef, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth/auth.service';
import { Router, RouterLink } from '@angular/router';
import { Subscription} from 'rxjs';
import { Place } from '../../models/place/place';
import { PlaceService } from '../../services/place/place.service';
import { FEATURES } from '../../features/features';
import { FeatureService} from '../../services/features/feature.service'
import { TranslocoService, TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslocoModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit, OnDestroy{

  idiomaDef: string = "";
  
  isAdmin: boolean = false;
  isLogsEnabled: boolean = FEATURES.adminLogsEnabled;
  
  featureService = inject(FeatureService);

  isLoggedIn: boolean = false;
  isMenuOpen: boolean = false; // El menú debe empezar cerrado
  isListaOpen: boolean = false;
  private userSub!: Subscription;

  searchTerm: string = '';
  suggestions: Place[] = [];
  allPlaces: Place[] = []; 
  showSuggestions: boolean = false;

  constructor(private authService: AuthService, 
              private router: Router, 
              private placeService: PlaceService, 
              private el: ElementRef,
              private translocoService: TranslocoService) {
                this.idiomaDef = this.translocoService.getActiveLang().toUpperCase();
              }
  
  ngOnInit(): void {
    const savedLang = localStorage.getItem('lang') || 'es';
    this.translocoService.setActiveLang(savedLang);
    this.idiomaDef = this.translocoService.getActiveLang().toUpperCase();
    this.isAdmin = this.authService.isAdmin();
    //Para ver al user actual  
    this.userSub = this.authService.currentUser$.subscribe(user => {
      this.isLoggedIn = !!user; // true si existe usuario, false si es null
      // Si el usuario se desloguea,  cerrar menú
      if (!this.isLoggedIn) {
        this.isMenuOpen = false;
      }
    });

    this.placeService.getPlaces().subscribe(data => this.allPlaces = data);
  }

  changeLang(lang: string) {
    this.translocoService.setActiveLang(lang);
    localStorage.setItem('lang', lang);
    this.idiomaDef = this.translocoService.getActiveLang().toUpperCase();
  }

  toggleLista() {
    this.isListaOpen = !this.isListaOpen;
  }

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login'])
  }

  ngOnDestroy(): void {
    // Cancelar la suscripción al destruir el componente para evitar fugas de memoria
    if (this.userSub) {
      this.userSub.unsubscribe();
    }
  }


  // Funciones para el buscador

  onSearch(event: any) {
    this.searchTerm = event.target.value.toLowerCase();
    
    if (this.searchTerm.length < 2) {
      this.suggestions = [];
      this.showSuggestions = false;
      return;
    }

    // Filtrado por Nombre, Dirección o Ciudad (Triple coincidencia)
    this.suggestions = this.allPlaces.filter(p => 
      p.name.toLowerCase().includes(this.searchTerm) ||
      p.address.toLowerCase().includes(this.searchTerm) ||
      p.city?.name?.toLowerCase().includes(this.searchTerm) || false
    ).slice(0, 5); // Solo 5 sugerencias para no saturar

    this.showSuggestions = true;
}

selectSuggestion(place: Place) {
    this.searchTerm = place.name;
    this.showSuggestions = false;
    // this.router.navigate(['/lugar', place.id]);  PARA NAVEGAR DIRECTO AL LUGAR SLEECCIONADO AL DAR CLICK REVISAR
}

goToSearch() {
    if (this.searchTerm.trim()) {
      this.showSuggestions = false;
      // Navegamos al componente de búsqueda pasando el término por queryParams
      this.router.navigate(['/SearchPlace'], { queryParams: { q: this.searchTerm } });
    }
}

// Detectar clic fuera del buscador para cerrar sugerencias
@HostListener('document:click', ['$event'])
  clickout(event: any) {
    if (!this.el.nativeElement.contains(event.target)) {
      this.showSuggestions = false;
      this.isListaOpen = !this.isListaOpen;
      this.isMenuOpen = !this.isMenuOpen;
    }
  }


}
