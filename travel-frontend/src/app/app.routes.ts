import { Routes } from '@angular/router';
import { InicioLogueadoComponent } from './pages/inicio-logueado/inicio-logueado.component';
import { LandingComponent } from './pages/landing/landing.component';
import { SignUpComponent } from './pages/sign-up/sign-up.component';
import { LoginComponent } from './pages//login/login.component';
import { WishlistComponent} from './pages/wishlist/wishlist.component'
import { authGuard } from './guards/auth.guard';
import { SuccessSignupComponent } from './components/success-signup/success-signup.component';
import { SearchPlacesComponent } from './pages//search-places/search-places.component';
import { adminGuard } from './guards/admin.guard';
import { AdminViewComponent } from './pages/admin-view/admin-view.component';
import { DepartmentComponent } from './pages//department/department.component'
import { CreateItineraryComponent } from './pages/create-itinerary/create-itinerary.component';
import { featureGuard } from './guards/feature.guard';
import { ForgotPasswordComponent } from './pages/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './pages/reset-password/reset-password.component';
import { ResetSuccessComponent } from './pages/reset-success/reset-success.component';
export const routes: Routes = [
  { path: 'sign-up', component: SignUpComponent },
  {
    path: 'SearchPlace', component: SearchPlacesComponent
  },
  { path: 'InicioLogueado', canActivate: [authGuard], component: InicioLogueadoComponent },
  { path: 'login', component: LoginComponent },
  { path: 'success-signup', component: SuccessSignupComponent },
  { path: '', component: LandingComponent },
  { path: 'wishlist', canActivate: [authGuard, featureGuard('showFavorites')], component: WishlistComponent },
  { path: 'itinerarios', canActivate: [authGuard], component: CreateItineraryComponent },
  { path: 'admin-view', canActivate: [adminGuard], component: AdminViewComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'reset-success', component: ResetSuccessComponent },
  {
    path: 'place/:id',
    loadComponent: () => import('./pages/place-detail/place-detail.component')
      .then(m => m.PlaceDetailComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'department/:id', 
    component: DepartmentComponent,canActivate: [authGuard]
  },
  { path: '**', redirectTo: '', pathMatch: 'full' }
];
