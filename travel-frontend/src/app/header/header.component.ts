import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { Subscription} from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit, OnDestroy{
  
  isLoggedIn: boolean = false;
  isMenuOpen: boolean = false; // El menú debe empezar cerrado
  private userSub!: Subscription;

  constructor(private authService: AuthService, private router: Router) {}
  
  ngOnInit(): void {
    //Para ver al user actual  
    this.userSub = this.authService.currentUser$.subscribe(user => {
      this.isLoggedIn = !!user; // true si existe usuario, false si es null
      // Si el usuario se desloguea,  cerrar menú
      if (!this.isLoggedIn) {
        this.isMenuOpen = false;
      }
    });
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
}
