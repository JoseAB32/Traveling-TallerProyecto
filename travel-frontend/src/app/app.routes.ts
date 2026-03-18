import { Routes } from '@angular/router';
import { InicioLogueadoComponent } from './inicio-logueado/inicio-logueado.component';
import { LandingComponent } from './landing/landing.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { LoginComponent } from './login/login.component';
import { WishlistComponent} from './wishlist/wishlist.component'
import { authGuard } from './guards/auth.guard';
import { SuccessSignupComponent } from './success-signup/success-signup.component';

export const routes: Routes = [
  { 
    path: 'sign-up', component: SignUpComponent 
  },
  {
    path: 'InicioLogueado', canActivate: [authGuard], component: InicioLogueadoComponent 
  },
  {
    path: '',
    component: LandingComponent
  },
  { path: 'login', component: LoginComponent },
  { path: 'success-signup', component: SuccessSignupComponent },
  { path: 'wishlist', component: WishlistComponent },
  {
    path: '**', 
    redirectTo: '', 
    pathMatch: 'full'
  }
];