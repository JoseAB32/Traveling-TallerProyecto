import { Routes } from '@angular/router';
import { InicioLogueadoComponent } from './inicio-logueado/inicio-logueado.component';
import { LandingComponent } from './landing/landing.component';

export const routes: Routes = [
  {path: 'InicioLogueado', component: InicioLogueadoComponent },
  {
    path: '',
    component: LandingComponent
  },
  {
    path: '**', 
    redirectTo: '', 
    pathMatch: 'full'
  }
];