import { Component, OnInit } from '@angular/core';
import { User } from '../user';
import { FormBuilder, FormsModule } from '@angular/forms';
import { UserService } from '../user.service';
import { Router } from '@angular/router';
import { NgForOf } from "../../../node_modules/@angular/common";

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [FormsModule, NgForOf],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent implements OnInit {
  user: User = new User();

  selected = "Selecciona una ciudad";
  ciudades = ["Selecciona una ciudad", "Cochabamba", "Santa Cruz", "La Paz", "Oruro", "Potosi", "Chuquisaca", "Tarija", "Beni", "Pando"];

  limiteInferiorFecha = '2000-01-01';
  limiteSuperiorFecha =  new Date().toISOString().split('T')[0];

  constructor (private userService: UserService, private router: Router) {

  }

  ngOnInit(): void {
      
  }

  saveUser() {
    this.userService.createUser(this.user).subscribe( data => {
        console.log(data);
        this.goToInicio();
      },
      error => console.log(error)
    );
  }

  goToInicio() {
    // this.router.navigate(['/inicio']); 
  }

  onSubmit() {
    console.log(this.user);
    this.saveUser();
  }
}
