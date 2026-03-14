import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-inicio-logueado',
  standalone: true,
  imports: [],
  templateUrl: './inicio-logueado.component.html',
  styleUrl: './inicio-logueado.component.css'
})
export class InicioLogueadoComponent implements OnInit{
  constructor(private authService: AuthService, private router: Router) {

  }

  ngOnInit(): void {
      
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login'])
  }
}
