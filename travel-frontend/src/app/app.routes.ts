import { Routes } from '@angular/router';
import { InicioLogueadoComponent } from './inicio-logueado/inicio-logueado.component';
import { LandingComponent } from './landing/landing.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { LoginComponent } from './login/login.component';
import { authGuard } from './guards/auth.guard';
import { SuccessSignupComponent } from './success-signup/success-signup.component';

export const routes: Routes = [
  { path: 'sign-up', component: SignUpComponent },
  { path: 'InicioLogueado', canActivate: [authGuard], component: InicioLogueadoComponent },
  { path: '', component: LandingComponent },
  { path: 'login', component: LoginComponent },
  { path: 'success-signup', component: SuccessSignupComponent },

  // 🔥 ESTA ES CLAVE
  {
    path: 'place/:id',
    loadComponent: () => import('./place-detail/place-detail.component')
      .then(m => m.PlaceDetailComponent),
    canActivate: [authGuard]
  },

  { path: '**', redirectTo: '', pathMatch: 'full' }
];