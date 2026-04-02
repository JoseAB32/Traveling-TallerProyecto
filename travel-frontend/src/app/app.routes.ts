import { Routes } from '@angular/router';
import { InicioLogueadoComponent } from './inicio-logueado/inicio-logueado.component';
import { LandingComponent } from './landing/landing.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { LoginComponent } from './login/login.component';
import { WishlistComponent } from './wishlist/wishlist.component';
import { authGuard } from './guards/auth.guard';
import { SuccessSignupComponent } from './success-signup/success-signup.component';
import { SearchPlacesComponent } from './search-places/search-places.component';

export const routes: Routes = [

  { path: '', component: LandingComponent },

  { path: 'login', component: LoginComponent },

  { path: 'sign-up', component: SignUpComponent },

  { path: 'success-signup', component: SuccessSignupComponent },

  // 🔐 protegidas
  {
    path: 'inicio-logueado',
    canActivate: [authGuard],
    component: InicioLogueadoComponent
  },

  {
    path: 'wishlist',
    canActivate: [authGuard],
    component: WishlistComponent
  },

  {
    path: 'search',
    component: SearchPlacesComponent
  },

  {
    path: '**',
    redirectTo: '',
    pathMatch: 'full'
  }

];