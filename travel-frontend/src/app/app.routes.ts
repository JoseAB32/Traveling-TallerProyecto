import { Routes } from '@angular/router';
import { InicioLogueadoComponent } from './inicio-logueado/inicio-logueado.component';
import { LandingComponent } from './landing/landing.component';

export const routes: Routes = [
  {
    path: '',
    component: LandingComponent
  },
  {
    path: '**', 
    redirectTo: '', 
    pathMatch: 'full'
  },
  {path: 'InicioLogueado', component: InicioLogueadoComponent }
];