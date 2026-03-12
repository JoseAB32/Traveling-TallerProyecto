import { Component, OnInit } from '@angular/core';
import { UserService } from '../user.service';
import { Router } from '@angular/router';
import { User } from '../user';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [],
  templateUrl: './inicio.component.html',
  styleUrl: './inicio.component.css'
})
export class InicioComponent implements OnInit {
  users: User[] = [];

  constructor(private userService: UserService, private router: Router) {

  }

  ngOnInit(): void {
      this.getUsers();
      console.log(this.users);
  }

  private getUsers() {
    this.userService.getUsersList().subscribe({
      next: (data) => {
        this.users = data;
      },
      error: (error) => {
        console.error('Error fetching users:', error);
      }
    });
  }
}
