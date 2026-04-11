import { Routes } from '@angular/router';
import { InicioLogueadoComponent } from './inicio-logueado/inicio-logueado.component';
import { LandingComponent } from './landing/landing.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { LoginComponent } from './login/login.component';
import { WishlistComponent} from './wishlist/wishlist.component'
import { authGuard } from './guards/auth.guard';
import { SuccessSignupComponent } from './components/success-signup/success-signup.component';
import { SearchPlacesComponent } from './search-places/search-places.component';
import { adminGuard } from './guards/admin.guard';
import { AdminViewComponent } from './admin-view/admin-view.component';
import { DepartmentComponent } from './department/department.component'
export const routes: Routes = [
  { path: 'sign-up', component: SignUpComponent },
  {
    path: 'SearchPlace', component: SearchPlacesComponent
  },
  { path: 'InicioLogueado', canActivate: [authGuard], component: InicioLogueadoComponent },
  { path: 'login', component: LoginComponent },
  { path: 'success-signup', component: SuccessSignupComponent },
  { path: '', component: LandingComponent },
  { path: 'wishlist', canActivate: [authGuard], component: WishlistComponent },
  { path: 'admin-view', canActivate: [adminGuard], component: AdminViewComponent },
  {
    path: 'place/:id',
    loadComponent: () => import('./place-detail/place-detail.component')
      .then(m => m.PlaceDetailComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'department/:id', 
    component: DepartmentComponent,canActivate: [authGuard]
  },
  { path: '**', redirectTo: '', pathMatch: 'full' }
];