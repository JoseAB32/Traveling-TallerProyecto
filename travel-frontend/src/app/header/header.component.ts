import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  /*
  Flags para probar que se muestre solo cuando hay usuari0 loggeado,
  faltaría conectar con lógica actual
  */
  isLoggedIn: boolean = false; 
  
  // Controla el menú desplegable
  isMenuOpen: boolean = true;

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }
}
