import { Routes } from '@angular/router';
import { SignUpComponent } from './sign-up/sign-up.component';
import { InicioComponent } from './inicio/inicio.component';

export const routes: Routes = [
    { path: 'sign-up', component: SignUpComponent },
    { path: 'inicio', component: InicioComponent }
];
