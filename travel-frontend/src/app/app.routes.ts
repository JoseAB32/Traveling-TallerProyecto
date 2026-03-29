import { Routes } from '@angular/router';
import { InicioLogueadoComponent } from './inicio-logueado/inicio-logueado.component';
import { PlaceDetailComponent } from './place-detail/place-detail.component';

export const routes: Routes = [
  { path: '', component: InicioLogueadoComponent },
  { path: 'place/:id', component: PlaceDetailComponent },
  { path: '**', redirectTo: '' }
];