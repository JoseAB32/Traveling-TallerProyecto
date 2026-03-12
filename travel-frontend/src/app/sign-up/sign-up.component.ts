import { Component, OnInit } from '@angular/core';
import { User } from '../user';
import { FormsModule } from '@angular/forms';
import { UserService } from '../user.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent implements OnInit {
  user: User = new User();

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
    this.router.navigate(['/inicio']);
  }

  onSubmit() {
    console.log(this.user);
    this.saveUser();
  }
}
