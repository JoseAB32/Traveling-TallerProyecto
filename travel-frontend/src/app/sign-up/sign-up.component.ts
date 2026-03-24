import { Component, OnInit } from '@angular/core';
import { User } from '../user';
import { FormsModule, NgForm } from '@angular/forms';
import { UserService } from '../user.service';
import { Router, RouterLink } from '@angular/router';
import { NgForOf, NgIf } from "../../../node_modules/@angular/common";
import { CityService } from '../city.service';
import { City } from '../city';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [FormsModule, NgForOf, NgIf, RouterLink],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent implements OnInit {
  user: User = new User();

  ciudades: City[]= [];

  limiteInferiorFecha = '2000-01-01';
  limiteSuperiorFecha =  new Date().toISOString().split('T')[0];

  confirmarContrasena: string = '';
  errorGeneral: string = '';
  isDateFocused: boolean = false;
  isCityFocused: boolean = false;

  constructor (private userService: UserService, private router: Router, private cityService: CityService) {
    this.cityService.getCities().subscribe(
      cities => this.ciudades = cities,
      err => console.error('Error cargando ciudades', err)
    );
  }

  onDateFocus() {
    this.isDateFocused = true;
  }

  onDateBlur() {
    this.isDateFocused = false;
  }

  onCityFocus() {
    this.isCityFocused = true;
  }

  onCityBlur() {
    this.isCityFocused = false;
  }

  contrasenaCoincide(): boolean {
    return this.user.pass === this.confirmarContrasena;
  }

  saveUser() {
    const payload: any = { ...this.user };

    if (this.user.city_id) {
      payload.city = { id: this.user.city_id };
    } else {
      payload.city = null;
    }

    delete payload.city_id; // backend espera el objeto city

    this.userService.createUser(payload).subscribe(
      data => {
        console.log(data);
        this.goToSuccessSignup();
      },
      error => {
        console.error('Error al crear usuario', error);
        this.errorGeneral = 'No se pudo registrar el usuario. Revise datos e intente de nuevo.';
      }
    );
  }

  goToSuccessSignup() {
    this.router.navigate(['/success-signup']); 
  }

  onSubmit(form: NgForm) {
    this.errorGeneral = '';

    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    if (!this.contrasenaCoincide()) {
      this.errorGeneral = 'Las contraseñas no coinciden.';
      return;
    }

    if (this.user.city_id === null || this.user.city_id === 0) {
      this.errorGeneral = 'Debes seleccionar una ciudad válida.';
      return;
    }

    console.log(this.user);
    this.saveUser();
  }

  ngOnInit(): void {
      
  }
}
