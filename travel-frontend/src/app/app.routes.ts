import { Routes } from '@angular/router';
import { InicioLogueadoComponent } from './inicio-logueado/inicio-logueado.component';
import { LandingComponent } from './landing/landing.component';
import { SignUpComponent } from './sign-up/sign-up.component';

export const routes: Routes = [
  { path: 'sign-up', component: SignUpComponent },
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