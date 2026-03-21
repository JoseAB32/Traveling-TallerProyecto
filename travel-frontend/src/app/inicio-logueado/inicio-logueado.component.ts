import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-inicio-logueado',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    HeaderComponent,
    FooterComponent
  ],
  templateUrl: './inicio-logueado.component.html', // 👈 CORREGIDO
  styleUrls: ['./inicio-logueado.component.css']
})
export class InicioLogueadoComponent {}