import { Component, OnInit } from '@angular/core';
import { User } from '../user';
import { FormsModule, NgForm } from '@angular/forms';
import { UserService } from '../user.service';
import { Router, RouterLink } from '@angular/router';
import { NgForOf, NgIf } from "../../../node_modules/@angular/common";

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [FormsModule, NgForOf, NgIf, RouterLink],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent implements OnInit {
  user: User = new User();

  ciudades = ["Cochabamba", "Santa Cruz", "La Paz", "Oruro", "Potosi", "Chuquisaca", "Tarija", "Beni", "Pando"];

  limiteInferiorFecha = '2000-01-01';
  limiteSuperiorFecha =  new Date().toISOString().split('T')[0];

  confirmarContrasena: string = '';
  errorGeneral: string = '';
  isDateFocused: boolean = false;
  isCityFocused: boolean = false;

  constructor (private userService: UserService, private router: Router) {

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
    this.userService.createUser(this.user).subscribe( data => {
        console.log(data);
        this.goToSuccessSignup();
      },
      error => console.log(error)
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

    if (this.user.city === "Selecciona una ciudad") {
      this.errorGeneral = 'Debes seleccionar una ciudad válida.';
      return;
    }

    console.log(this.user);
    this.saveUser();
  }

  ngOnInit(): void {
      
  }
}
